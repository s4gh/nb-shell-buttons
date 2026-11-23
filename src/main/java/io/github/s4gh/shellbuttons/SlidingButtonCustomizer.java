package io.github.s4gh.shellbuttons;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ContainerEvent;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.plaf.ButtonUI;
import org.netbeans.swing.tabcontrol.SlideBarDataModel;
import org.netbeans.swing.tabcontrol.SlidingButton;
import org.netbeans.swing.tabcontrol.TabData;
import org.netbeans.swing.tabcontrol.plaf.BusyTabsSupport;
import org.netbeans.swing.tabcontrol.plaf.TabControlButton;

/**
 * Turns the buttons of minimized ("slided") windows on the left and right
 * edges of the main window into icon-only buttons.
 * <p>
 * The window system re-creates all {@link SlidingButton}s of a side bar
 * whenever that bar changes (window minimized/restored, renamed, busy, ...),
 * so a global container listener is enough to catch every button as soon as
 * it is added to the component hierarchy.
 * <p>
 * Each group of windows minimized from the same mode is preceded by a
 * "Restore Window Group" button ({@link TabControlButton}); in the large
 * size it is scaled together with the window icons.
 * <p>
 * A busy window ({@code TopComponent.makeBusy(true)}) gets a spinner instead
 * of its icon. Some windows are busy only for a moment, many times in a row
 * (e.g. the Navigator on every selection change), which makes an icon-only
 * button flicker. So the spinner is shown only once the window has been busy
 * for {@link #BUSY_ICON_DELAY_MS}; until then the window keeps its own icon.
 */
final class SlidingButtonCustomizer implements AWTEventListener, PreferenceChangeListener {

    /** Original button text, also marks a button as customized. */
    private static final String KEY_ORIGINAL_TEXT = "shellbuttons.originalText"; //NOI18N
    /** Preferred width of the button in its original (icon + text) form. */
    private static final String KEY_BASE_WIDTH = "shellbuttons.baseWidth"; //NOI18N
    /** Original (unscaled) button icon. */
    private static final String KEY_ORIGINAL_ICON = "shellbuttons.originalIcon"; //NOI18N
    /**
     * Client property the window system puts on the "Restore Window Group"
     * buttons of the side bars; holds the {@link SlideBarDataModel} orientation.
     */
    private static final String KEY_RESTORE_BUTTON_ORIENTATION = "NbSlideBar.RestoreButton.Orientation"; //NOI18N
    /** Window system class of the bars holding minimized windows (not public API). */
    private static final String SLIDE_BAR_CLASS = "org.netbeans.core.windows.view.ui.slides.SlideBar"; //NOI18N
    /** How long a window must be busy before its button shows the spinner. */
    private static final int BUSY_ICON_DELAY_MS = 1000;

    /**
     * Since when (ms) a window is busy, keyed by its component. Survives the
     * re-creation of the buttons. Accessed from the EDT only.
     */
    private final Map<Component, Long> busySince = new WeakHashMap<>();

    private volatile IconAreaWidth width = IconAreaWidth.DEFAULT;
    /** {@code true} = standard NetBeans look, buttons are left untouched. */
    private volatile boolean showText;

    void install() {
        loadSettings();
        Settings.preferences().addPreferenceChangeListener(this);
        Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.CONTAINER_EVENT_MASK);
        // buttons that already exist (e.g. module enabled in a running IDE)
        SwingUtilities.invokeLater(this::refreshAll);
    }

    void uninstall() {
        Toolkit.getDefaultToolkit().removeAWTEventListener(this);
        Settings.preferences().removePreferenceChangeListener(this);
        SwingUtilities.invokeLater(this::restoreAll);
    }

    // ------------------------------------------------------------ listeners

    @Override
    public void eventDispatched(AWTEvent event) {
        if (showText || event.getID() != ContainerEvent.COMPONENT_ADDED) {
            return;
        }
        Component child = ((ContainerEvent) event).getChild();
        if (child instanceof SlidingButton button) {
            if (SwingUtilities.isEventDispatchThread()) {
                customize(button);
            } else {
                SwingUtilities.invokeLater(() -> customize(button));
            }
        } else if (child instanceof TabControlButton button && isRestoreButton(button)) {
            // customize right away: doing it later makes the bar lay out and
            // paint once with the small button, then grow - all icons below
            // it jump (the bar is rebuilt on clicks, busy changes, ...)
            if (SwingUtilities.isEventDispatchThread()) {
                customizeRestoreButton(button);
            } else {
                SwingUtilities.invokeLater(() -> customizeRestoreButton(button));
            }
        }
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        String key = evt.getKey();
        if (Settings.KEY_ICON_AREA_WIDTH.equals(key) || Settings.KEY_SHOW_TEXT.equals(key)) {
            loadSettings();
            SwingUtilities.invokeLater(this::refreshAll);
        }
    }

    private void loadSettings() {
        width = Settings.getIconAreaWidth();
        showText = Settings.isShowText();
    }

    // ------------------------------------------------------------ customization

    /** Brings all existing buttons in line with the current settings. */
    private void refreshAll() {
        if (showText) {
            restoreAll();
        } else {
            customizeAll();
        }
    }

    private void customizeAll() {
        for (Window w : Window.getWindows()) {
            forEachSlidingButton(w, this::customize);
            forEachRestoreButton(w, this::customizeRestoreButton);
        }
    }

    private void restoreAll() {
        for (Window w : Window.getWindows()) {
            forEachSlidingButton(w, SlidingButtonCustomizer::restore);
            forEachRestoreButton(w, SlidingButtonCustomizer::restoreRestoreButton);
        }
    }

    private void customize(SlidingButton button) {
        if (showText) {
            return; // standard NetBeans look requested
        }
        int orientation = button.getOrientation();
        if (orientation != SlideBarDataModel.WEST && orientation != SlideBarDataModel.EAST) {
            return; // bottom/top bar keeps the stock look
        }
        if (button.getClientProperty(KEY_ORIGINAL_TEXT) == null) {
            Icon icon = button.getIcon();
            if (!isValid(icon)) {
                return; // nothing to show instead of the text - keep it readable
            }
            String text = button.getText();
            // width of the bar in its stock form: icon + vertical text
            int baseWidth = button.getPreferredSize().width;
            button.putClientProperty(KEY_ORIGINAL_TEXT, text == null ? "" : text);
            button.putClientProperty(KEY_BASE_WIDTH, baseWidth);
            button.putClientProperty(KEY_ORIGINAL_ICON, icon);
            button.getAccessibleContext().setAccessibleName(text);
            button.setText("");
        }
        applyIcon(button);
        applySize(button);
    }

    private void applyIcon(SlidingButton button) {
        Icon icon = iconToShow(button, (Icon) button.getClientProperty(KEY_ORIGINAL_ICON));
        double scale = width.getIconScale();
        button.setIcon(scale == 1.0 ? icon : new ScaledIcon(icon, scale));
    }

    /**
     * @return the icon set by the window system, except for a window that has
     * been busy for less than {@link #BUSY_ICON_DELAY_MS}: that one keeps its
     * own icon and the button is updated once the delay expires
     */
    private Icon iconToShow(SlidingButton button, Icon original) {
        TabData data = button.getData();
        Component window = data.getComponent();
        if (!isBusyIcon(original)) {
            busySince.remove(window);
            return original;
        }
        Icon own = data.getIcon();
        if (!isValid(own)) {
            return original;
        }
        long now = System.currentTimeMillis();
        long remaining = BUSY_ICON_DELAY_MS - (now - busySince.computeIfAbsent(window, k -> now));
        if (remaining <= 0) {
            return original;
        }
        Timer timer = new Timer((int) remaining, e -> {
            // the bar re-creates its buttons when the window stops being busy,
            // so a button that is still in place belongs to a busy window
            if (button.getParent() != null && button.getClientProperty(KEY_ORIGINAL_TEXT) != null) {
                applyIcon(button);
                applySize(button);
            }
        });
        timer.setRepeats(false);
        timer.start();
        return own;
    }

    private static boolean isBusyIcon(Icon icon) {
        BusyTabsSupport busy = BusyTabsSupport.getDefault();
        return icon != null && busy != null
                && (icon == busy.getBusyIcon(false) || icon == busy.getBusyIcon(true));
    }

    private static boolean isValid(Icon icon) {
        // TabData replaces a missing icon with an empty 0x0 placeholder
        return icon != null && icon.getIconWidth() > 0 && icon.getIconHeight() > 0;
    }

    private void applySize(SlidingButton button) {
        int baseWidth = (Integer) button.getClientProperty(KEY_BASE_WIDTH);
        // size the UI needs for the bare icon (UI swaps width/height for vertical bars)
        Dimension iconOnly = button.getUI().getPreferredSize(button);
        int w = Math.max((int) Math.round(baseWidth * width.getFactor()), iconOnly.width);
        // square cells look tidy and give a comfortable click target
        Dimension size = new Dimension(w, Math.max(w, iconOnly.height));
        button.setPreferredSize(size);
        button.setMinimumSize(size);
        button.setMaximumSize(size);
        button.revalidate();
        button.repaint();
    }

    private static void restore(SlidingButton button) {
        Object text = button.getClientProperty(KEY_ORIGINAL_TEXT);
        if (text == null) {
            return;
        }
        button.setText((String) text);
        Object icon = button.getClientProperty(KEY_ORIGINAL_ICON);
        if (icon instanceof Icon original) {
            button.setIcon(original);
        }
        button.setPreferredSize(null);
        button.setMinimumSize(null);
        button.setMaximumSize(null);
        button.putClientProperty(KEY_ORIGINAL_TEXT, null);
        button.putClientProperty(KEY_BASE_WIDTH, null);
        button.putClientProperty(KEY_ORIGINAL_ICON, null);
        button.revalidate();
        button.repaint();
    }

    /**
     * {@code true} for a "Restore Window Group" button: the only kind of
     * {@link TabControlButton} the window system puts into a slide bar.
     */
    private static boolean isRestoreButton(TabControlButton button) {
        Container parent = button.getParent();
        return parent != null && SLIDE_BAR_CLASS.equals(parent.getClass().getName());
    }

    /**
     * {@code true} if the restore button is on the left or right edge. The
     * window system sets the orientation just after adding the button, so
     * this is evaluated whenever the button is laid out or painted.
     */
    private static boolean isOnSideBar(JComponent button) {
        Object orientation = button.getClientProperty(KEY_RESTORE_BUTTON_ORIENTATION);
        return Integer.valueOf(SlideBarDataModel.WEST).equals(orientation)
                || Integer.valueOf(SlideBarDataModel.EAST).equals(orientation);
    }

    /** Scales the "Restore Window Group" button of a left/right side bar. */
    private void customizeRestoreButton(TabControlButton button) {
        double scale = width.getIconScale();
        if (showText || scale == 1.0) {
            restoreRestoreButton(button);
            return;
        }
        ButtonUI ui = button.getUI();
        if (ui instanceof ScaledButtonUI scaled) {
            if (scaled.getScale() == scale) {
                return;
            }
            ui = scaled.getDelegate();
        }
        button.setUI(new ScaledButtonUI(ui, scale, SlidingButtonCustomizer::isOnSideBar));
        button.revalidate();
        button.repaint();
    }

    private static void restoreRestoreButton(TabControlButton button) {
        if (button.getUI() instanceof ScaledButtonUI scaled) {
            button.setUI(scaled.getDelegate());
            button.revalidate();
            button.repaint();
        }
    }

    private static void forEachRestoreButton(Container root, java.util.function.Consumer<TabControlButton> action) {
        for (Component c : root.getComponents()) {
            if (c instanceof TabControlButton button) {
                if (isRestoreButton(button)) {
                    action.accept(button);
                }
            } else if (c instanceof Container container) {
                forEachRestoreButton(container, action);
            }
        }
    }

    private static void forEachSlidingButton(Container root, java.util.function.Consumer<SlidingButton> action) {
        for (Component c : root.getComponents()) {
            if (c instanceof SlidingButton button) {
                action.accept(button);
            } else if (c instanceof Container container) {
                forEachSlidingButton(container, action);
            }
        }
    }
}
