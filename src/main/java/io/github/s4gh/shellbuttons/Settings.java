package io.github.s4gh.shellbuttons;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 * Persistent plugin settings.
 */
public final class Settings {

    static final String KEY_ICON_AREA_WIDTH = "iconAreaWidth"; //NOI18N
    static final String KEY_SHOW_TEXT = "showText"; //NOI18N

    private Settings() {
    }

    static Preferences preferences() {
        return NbPreferences.forModule(Settings.class);
    }

    /**
     * @return {@code true} if minimized window buttons keep their names,
     * i.e. the standard NetBeans look is used
     */
    public static boolean isShowText() {
        return preferences().getBoolean(KEY_SHOW_TEXT, false);
    }

    public static void setShowText(boolean showText) {
        preferences().putBoolean(KEY_SHOW_TEXT, showText);
    }

    public static IconAreaWidth getIconAreaWidth() {
        String value = preferences().get(KEY_ICON_AREA_WIDTH, IconAreaWidth.DEFAULT.name());
        try {
            return IconAreaWidth.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return IconAreaWidth.DEFAULT;
        }
    }

    public static void setIconAreaWidth(IconAreaWidth width) {
        preferences().put(KEY_ICON_AREA_WIDTH, width.name());
    }
}
