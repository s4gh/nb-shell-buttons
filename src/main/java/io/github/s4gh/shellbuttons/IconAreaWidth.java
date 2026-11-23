package io.github.s4gh.shellbuttons;

/**
 * Width of the icon area used for minimized window buttons on the left and
 * right edges of the main window, together with the matching icon size.
 */
public enum IconAreaWidth {

    /** Same width the stock (icon + vertical text) button has, stock icon size. */
    DEFAULT(1.0, 1.0),
    /** 1.5 times the stock width, icon 1.5 times larger. */
    LARGE(1.5, 1.5);

    private final double factor;
    private final double iconScale;

    IconAreaWidth(double factor, double iconScale) {
        this.factor = factor;
        this.iconScale = iconScale;
    }

    public double getFactor() {
        return factor;
    }

    /** Scale applied to the window icon; {@code 1.0} keeps the original icon. */
    public double getIconScale() {
        return iconScale;
    }
}
