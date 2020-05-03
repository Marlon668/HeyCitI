package gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import gui.util.*;
import iot.Parameters;
import iot.SimulationRunner;
import iot.networkentity.Mote;
import iot.networkentity.UserMote;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Class used for configuring the testrun for visualising the effect of doing adaptations to the path
 * of a cyclist
 */
public class ConfigureRun4 {
    private SimulationRunner simulationRunner;
    private JTabbedPane tabbedPane1;
    private JPanel mainPanel;
    private JButton classGenerateButton;
    private JSpinner bufferProbHeightSpinner;
    private JSpinner bufferProbWidthSpinner;
    private JButton Show;
    private int bufferSizeHeight;
    private int bufferSizeWidth;

    public ConfigureRun4(SimulationRunner simulationRunner) {
        this.simulationRunner = simulationRunner;
        refresh();

        classGenerateButton.addActionListener(e -> {
            if (simulationRunner == null) {
                String message = "No Configuration loaded";
                JOptionPane.showMessageDialog(null, message, "Setting error", JOptionPane.ERROR_MESSAGE);
            } else {
                if (simulationRunner.getSimulation().getInputProfile().isEmpty()) {
                    String message = "No inputprofile selected";
                    JOptionPane.showMessageDialog(null, message, "Setting error", JOptionPane.ERROR_MESSAGE);
                } else {
                    bufferSizeHeight = (Integer) bufferProbHeightSpinner.getValue();
                    bufferSizeWidth = (Integer) bufferProbWidthSpinner.getValue();
                    simulationRunner.visualiseRun(bufferSizeWidth, bufferSizeHeight);

                }
            }


        });

        Show.addActionListener(e -> {
            if (simulationRunner.getVisualiseRun() != null) {
                Window window = SwingUtilities.getWindowAncestor(mainPanel);
                window.dispose();
                ArrayList<Mote> motes = new ArrayList<Mote>();
                simulationRunner.getEnvironment().getMotes().forEach(mote -> {
                    if (mote instanceof UserMote) {
                        motes.add(mote);
                    }
                });
                LinePlot2 var1 = new LinePlot2("Visualise Plot", simulationRunner.getVisualiseRun(), simulationRunner.getAdaptationPoints(), simulationRunner.getAlternativeRoute(), motes, 0, bufferSizeHeight, bufferSizeWidth);
                var1.pack();
                RefineryUtilities.centerFrameOnScreen(var1);
                var1.setVisible(true);
            } else {
                String message = "No results available";
                JOptionPane.showMessageDialog(null, message, "No Results", JOptionPane.ERROR_MESSAGE);
            }

        });

    }

    private void refresh() {
        bufferProbHeightSpinner.setModel(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
        bufferProbWidthSpinner.setModel(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.setMinimumSize(new Dimension(750, 400));
        mainPanel.setPreferredSize(new Dimension(750, 400));
        tabbedPane1 = new JTabbedPane();
        tabbedPane1.setEnabled(true);
        mainPanel.add(tabbedPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(2, 5, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane1.addTab("Setting", panel1);
        Show = new JButton();
        Show.setText("Show");
        panel1.add(Show, new GridConstraints(1, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        bufferProbWidthSpinner = new JSpinner();
        panel1.add(bufferProbWidthSpinner, new GridConstraints(0, 3, 2, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(103, 28), null, 0, false));
        bufferProbHeightSpinner = new JSpinner();
        panel1.add(bufferProbHeightSpinner, new GridConstraints(0, 1, 2, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(103, 28), null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Buffer size (width)");
        panel1.add(label1, new GridConstraints(0, 2, 2, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(119, 28), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Buffer size (height)");
        panel1.add(label2, new GridConstraints(0, 0, 2, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(119, 28), null, 0, false));
        classGenerateButton = new JButton();
        classGenerateButton.setText("Generate");
        panel1.add(classGenerateButton, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}

