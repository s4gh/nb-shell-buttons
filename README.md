# Icon-Only Minimized Windows

NetBeans IDE plugin that changes how minimized windows (e.g. *Navigator*, *Services*)
look on the **left and right** edges of the main window: only the icon is shown, the
vertical window name is hidden. The name is still available as a tooltip and as the
accessible name of the button.

## Settings

**Tools > Options > Appearance > Minimized Windows**

| Option  | Icon area width and icon size                        |
|---------|------------------------------------------------------|
| Default | standard NetBeans button width, standard icon size   |
| Large   | 1.5 times the standard width, icons 1.5 times larger |

**Show window names (standard NetBeans look)**: when checked, the buttons keep their
icon and vertical name exactly as in stock NetBeans and the width option is disabled.
Unchecked (the default) gives icon-only buttons.

Changes are applied immediately, no restart needed.

Notes:
- The bottom edge keeps the standard look.
- With *Large*, the "Restore Window Group" buttons (shown before each group of windows
  minimized from the same place) are scaled 1.5 times as well.
- A window without an icon keeps its text so it stays identifiable.
- A busy window (e.g. the Navigator while it refreshes) shows the spinner only after
  it has been busy for 1 second, so short busy periods don't make the icon flicker.
- Disabling or uninstalling the plugin restores the standard look.

## Build

```sh
mvn clean install
```

The installable module is `target/nb-shell-buttons-1.2.nbm`
(Tools > Plugins > Downloaded > Add Plugins...).

To try it in a separate IDE instance:

```sh
mvn install nbm:cluster nbm:run-ide -Dnetbeans.installation=/path/to/netbeans
```

## How it works

The window system re-creates the `SlidingButton`s of a side bar every time the bar
changes. The plugin registers a global AWT container listener, catches every
`SlidingButton` with `WEST`/`EAST` orientation as it is added, removes its text and
fixes its size to `original width × factor` (square cell, icon centered).
