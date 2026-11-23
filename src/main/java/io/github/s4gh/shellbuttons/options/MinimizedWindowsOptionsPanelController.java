package io.github.s4gh.shellbuttons.options;

import io.github.s4gh.shellbuttons.IconAreaWidth;
import io.github.s4gh.shellbuttons.Settings;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 * Tools &gt; Options &gt; Appearance &gt; Minimized Windows.
 */
@OptionsPanelController.SubRegistration(
        location = "Appearance",
        id = "MinimizedWindows",
        displayName = "#Options_DisplayName",
        keywords = "#Options_Keywords",
        keywordsCategory = "Appearance/MinimizedWindows")
public final class MinimizedWindowsOptionsPanelController extends OptionsPanelController {

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private MinimizedWindowsPanel panel;
    private IconAreaWidth storedWidth = IconAreaWidth.DEFAULT;
    private boolean storedShowText;

    @Override
    public void update() {
        storedWidth = Settings.getIconAreaWidth();
        storedShowText = Settings.isShowText();
        getPanel().setSelectedWidth(storedWidth);
        getPanel().setShowText(storedShowText);
    }

    @Override
    public void applyChanges() {
        IconAreaWidth width = getPanel().getSelectedWidth();
        if (width != storedWidth) {
            Settings.setIconAreaWidth(width);
            storedWidth = width;
        }
        boolean showText = getPanel().isShowText();
        if (showText != storedShowText) {
            Settings.setShowText(showText);
            storedShowText = showText;
        }
    }

    @Override
    public void cancel() {
        // nothing is applied until applyChanges()
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean isChanged() {
        return panel != null
                && (panel.getSelectedWidth() != storedWidth || panel.isShowText() != storedShowText);
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        return getPanel();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    void changed() {
        pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, null, null);
        pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }

    private MinimizedWindowsPanel getPanel() {
        if (panel == null) {
            panel = new MinimizedWindowsPanel(this);
        }
        return panel;
    }
}
