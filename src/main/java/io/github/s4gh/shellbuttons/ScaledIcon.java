package io.github.s4gh.shellbuttons;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.Icon;

/**
 * Paints another icon scaled by a fixed factor.
 * <p>
 * The scale is applied to the {@code Graphics} transform rather than to a
 * pre-rendered image, so NetBeans icons that are resolution aware (SVG icons
 * and multi-resolution PNGs loaded through {@code ImageUtilities}) render at
 * the effective size and stay sharp. Plain bitmaps are interpolated.
 */
final class ScaledIcon implements Icon {

    private final Icon delegate;
    private final double scale;

    ScaledIcon(Icon delegate, double scale) {
        this.delegate = delegate;
        this.scale = scale;
    }

    Icon getDelegate() {
        return delegate;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);
            g2.scale(scale, scale);
            delegate.paintIcon(c, g2, 0, 0);
        } finally {
            g2.dispose();
        }
    }

    @Override
    public int getIconWidth() {
        return (int) Math.round(delegate.getIconWidth() * scale);
    }

    @Override
    public int getIconHeight() {
        return (int) Math.round(delegate.getIconHeight() * scale);
    }
}
