package io.github.s4gh.shellbuttons.options;

import io.github.s4gh.shellbuttons.IconAreaWidth;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

final class MinimizedWindowsPanel extends JPanel {

    private final JCheckBox cbShowText = new JCheckBox();
    private final JLabel lblWidth = new JLabel();
    private final JRadioButton rbDefault = new JRadioButton();
    private final JRadioButton rbLarge = new JRadioButton();

    MinimizedWindowsPanel(MinimizedWindowsOptionsPanelController controller) {
        super(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        Mnemonics.setLocalizedText(cbShowText, NbBundle.getMessage(MinimizedWindowsPanel.class, "LBL_ShowText"));
        lblWidth.setText(NbBundle.getMessage(MinimizedWindowsPanel.class, "LBL_IconAreaWidth"));
        Mnemonics.setLocalizedText(rbDefault, NbBundle.getMessage(MinimizedWindowsPanel.class, "LBL_Default"));
        Mnemonics.setLocalizedText(rbLarge, NbBundle.getMessage(MinimizedWindowsPanel.class, "LBL_Large"));
        ButtonGroup group = new ButtonGroup();
        group.add(rbDefault);
        group.add(rbLarge);
        rbDefault.addActionListener(e -> controller.changed());
        rbLarge.addActionListener(e -> controller.changed());
        cbShowText.addActionListener(e -> {
            updateEnabledState();
            controller.changed();
        });

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(0, 0, 12, 0);
        add(cbShowText, c);

        c.insets = new Insets(0, 0, 6, 0);
        add(lblWidth, c);

        c.insets = new Insets(0, 12, 0, 0);
        add(rbDefault, c);
        add(rbLarge, c);

        // filler pushes everything to the top-left corner
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        JPanel filler = new JPanel();
        filler.setOpaque(false);
        add(filler, c);
    }

    /** Icon area width only matters in icon-only mode. */
    private void updateEnabledState() {
        boolean iconsOnly = !cbShowText.isSelected();
        lblWidth.setEnabled(iconsOnly);
        rbDefault.setEnabled(iconsOnly);
        rbLarge.setEnabled(iconsOnly);
    }

    void setShowText(boolean showText) {
        cbShowText.setSelected(showText);
        updateEnabledState();
    }

    boolean isShowText() {
        return cbShowText.isSelected();
    }

    void setSelectedWidth(IconAreaWidth width) {
        (width == IconAreaWidth.LARGE ? rbLarge : rbDefault).setSelected(true);
    }

    IconAreaWidth getSelectedWidth() {
        return rbLarge.isSelected() ? IconAreaWidth.LARGE : IconAreaWidth.DEFAULT;
    }
}
