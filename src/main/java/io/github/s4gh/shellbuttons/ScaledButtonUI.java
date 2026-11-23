package io.github.s4gh.shellbuttons;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.function.Predicate;
import javax.accessibility.Accessible;
import javax.swing.JComponent;
import javax.swing.plaf.ButtonUI;

/**
 * Wraps the look-and-feel UI of a button and paints it scaled by a fixed
 * factor around the center of the button.
 * <p>
 * Used for the "Restore Window Group" buttons of the side bars. Those
 * buttons ({@code TabControlButton}) override {@code getIcon()} to fetch the
 * icon from the tab displayer UI, so the icon cannot simply be replaced as
 * it is done for the sliding buttons.
 * <p>
 * The wrapped UI lays out its content centered in the button, i.e. around
 * {@code (w/2, h/2)}. Painting it with a transform that scales by {@code s}
 * and keeps that point fixed gives the same look, only {@code s} times
 * larger. Resolution aware icons (SVG, multi-resolution images) render at
 * the effective size and stay sharp.
 * <p>
 * The scale is applied only while {@code applies} accepts the button, which
 * lets the UI be installed before everything needed for that decision is
 * known; otherwise the wrapped UI is used as is.
 */
final class ScaledButtonUI extends ButtonUI {

    private final ButtonUI delegate;
    private final double scale;
    private final Predicate<JComponent> applies;

    ScaledButtonUI(ButtonUI delegate, double scale, Predicate<JComponent> applies) {
        this.delegate = delegate;
        this.scale = scale;
        this.applies = applies;
    }

    ButtonUI getDelegate() {
        return delegate;
    }

    double getScale() {
        return scale;
    }

    @Override
    public void installUI(JComponent c) {
        delegate.installUI(c);
    }

    @Override
    public void uninstallUI(JComponent c) {
        delegate.uninstallUI(c);
    }

    private double scaleFor(JComponent c) {
        return applies.test(c) ? scale : 1.0;
    }

    @Override
    public void update(Graphics g, JComponent c) {
        if (scaleFor(c) == 1.0) {
            delegate.update(g, c);
            return;
        }
        Graphics2D g2 = scaled(g, c);
        try {
            delegate.update(g2, c);
        } finally {
            g2.dispose();
        }
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        if (scaleFor(c) == 1.0) {
            delegate.paint(g, c);
            return;
        }
        Graphics2D g2 = scaled(g, c);
        try {
            delegate.paint(g2, c);
        } finally {
            g2.dispose();
        }
    }

    private Graphics2D scaled(Graphics g, JComponent c) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // keep the center of the button in place: t + s * (w/2) = w/2
        g2.translate(c.getWidth() * (1 - scale) / 2, c.getHeight() * (1 - scale) / 2);
        g2.scale(scale, scale);
        return g2;
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        return scale(delegate.getPreferredSize(c), scaleFor(c));
    }

    @Override
    public Dimension getMinimumSize(JComponent c) {
        return scale(delegate.getMinimumSize(c), scaleFor(c));
    }

    @Override
    public Dimension getMaximumSize(JComponent c) {
        return scale(delegate.getMaximumSize(c), scaleFor(c));
    }

    private static Dimension scale(Dimension d, double s) {
        if (d == null || s == 1.0) {
            return d;
        }
        return new Dimension(scale(d.width, s), scale(d.height, s));
    }

    private static int scale(int v, double s) {
        // keep "unbounded" maximum sizes unbounded
        return v >= Short.MAX_VALUE ? v : (int) Math.round(v * s);
    }

    @Override
    public boolean contains(JComponent c, int x, int y) {
        return delegate.contains(c, x, y);
    }

    @Override
    public int getAccessibleChildrenCount(JComponent c) {
        return delegate.getAccessibleChildrenCount(c);
    }

    @Override
    public Accessible getAccessibleChild(JComponent c, int i) {
        return delegate.getAccessibleChild(c, i);
    }
}
