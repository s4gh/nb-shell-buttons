package io.github.s4gh.shellbuttons;

import org.openide.modules.ModuleInstall;

/**
 * Hooks the sliding button customizer into the IDE when the module starts
 * and restores the stock look when it is disabled or uninstalled.
 */
public class Installer extends ModuleInstall {

    private static final SlidingButtonCustomizer CUSTOMIZER = new SlidingButtonCustomizer();

    @Override
    public void restored() {
        // runs before the main window is shown, so the listener sees
        // the side bar buttons as they are created
        CUSTOMIZER.install();
    }

    @Override
    public void uninstalled() {
        CUSTOMIZER.uninstall();
    }
}
