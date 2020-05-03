package gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import gui.util.*;
import iot.Environment;
import iot.SimulationRunner;
import iot.networkentity.Mote;
import iot.networkentity.MoteFactory;
import iot.networkentity.MoteSensor;
import iot.networkentity.UserMote;
import org.jxmapviewer.viewer.GeoPosition;
import util.GraphStructure;
import util.Pair;
import util.Path;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class ResultGUI {
    private JPanel mainPanel;
    private JButton OKButton;
    private JRadioButton BoxPlotButton;
    private JRadioButton LineGraphsButton;
    private JRadioButton ScatterPlotButton;
    private JRadioButton VisualiseRunButton;
    private JRadioButton boxplot2RadioButton;
    private JRadioButton boxplot3RadioButton;
    private Integer option;
    private SimulationRunner simulationRunner;

    public ResultGUI(SimulationRunner simulationRunner) {

        this.simulationRunner = simulationRunner;

        ButtonGroup positionButtonGroup = new ButtonGroup();
        positionButtonGroup.add(LineGraphsButton);
        positionButtonGroup.add(BoxPlotButton);
        positionButtonGroup.add(ScatterPlotButton);
        positionButtonGroup.add(VisualiseRunButton);
        positionButtonGroup.add(BoxPlotButton);
        positionButtonGroup.add(boxplot3RadioButton);

        option = 1;
        LineGraphsButton.addActionListener(e ->
            option = 1
        );
        BoxPlotButton.addActionListener(e ->
            option = 2
        );
        ScatterPlotButton.addActionListener(e ->
            option = 3
        );
        VisualiseRunButton.addActionListener(e ->
            option = 4
        );
        boxplot2RadioButton.addActionListener(e ->
            option = 5
        );
        boxplot3RadioButton.addActionListener(e ->
            option = 6
        );


        OKButton.addActionListener((e) -> {
            if (option == 1) {
                Window window = SwingUtilities.getWindowAncestor(mainPanel);
                window.dispose();
                JFrame frame = new JFrame("Configure runs");
                ConfigureRun configureRun = new ConfigureRun(simulationRunner);
                frame.setContentPane(configureRun.getMainPanel());
                frame.setMinimumSize(configureRun.getMainPanel().getMinimumSize());
                frame.setPreferredSize(configureRun.getMainPanel().getPreferredSize());
                frame.setVisible(true);
            } else {
                if (option == 2) {
                    Window window = SwingUtilities.getWindowAncestor(mainPanel);
                    window.dispose();
                    JFrame frame = new JFrame("Configure runs");
                    ConfigureRun2 configureRun = new ConfigureRun2(simulationRunner);
                    frame.setContentPane(configureRun.getMainPanel());
                    frame.setMinimumSize(configureRun.getMainPanel().getMinimumSize());
                    frame.setPreferredSize(configureRun.getMainPanel().getPreferredSize());
                    frame.setVisible(true);
                } else {
                    if (option == 3) {
                        Window window = SwingUtilities.getWindowAncestor(mainPanel);
                        window.dispose();
                        JFrame frame = new JFrame("Configure runs");
                        ConfigureRun3 configureRun = new ConfigureRun3(simulationRunner);
                        frame.setContentPane(configureRun.getMainPanel());
                        frame.setMinimumSize(configureRun.getMainPanel().getMinimumSize());
                        frame.setPreferredSize(configureRun.getMainPanel().getPreferredSize());
                        frame.setVisible(true);
                    } else {
                        if (option == 4) {
                            Window window = SwingUtilities.getWindowAncestor(mainPanel);
                            window.dispose();
                            JFrame frame = new JFrame("Configure runs");
                            ConfigureRun4 configureRun = new ConfigureRun4(simulationRunner);
                            frame.setContentPane(configureRun.getMainPanel());
                            frame.setMinimumSize(configureRun.getMainPanel().getMinimumSize());
                            frame.setPreferredSize(configureRun.getMainPanel().getPreferredSize());
                            frame.setVisible(true);
                        } else {
                            if (option == 5) {
                                Window window = SwingUtilities.getWindowAncestor(mainPanel);
                                window.dispose();
                                JFrame frame = new JFrame("Configure runs");
                                ConfigureRun5 configureRun = new ConfigureRun5(simulationRunner);
                                frame.setContentPane(configureRun.getMainPanel());
                                frame.setMinimumSize(configureRun.getMainPanel().getMinimumSize());
                                frame.setPreferredSize(configureRun.getMainPanel().getPreferredSize());
                                frame.setVisible(true);
                            } else {
                                Window window = SwingUtilities.getWindowAncestor(mainPanel);
                                window.dispose();
                                JFrame frame = new JFrame("Configure runs");
                                ConfigureRun6 configureRun = new ConfigureRun6(simulationRunner);
                                frame.setContentPane(configureRun.getMainPanel());
                                frame.setMinimumSize(configureRun.getMainPanel().getMinimumSize());
                                frame.setPreferredSize(configureRun.getMainPanel().getPreferredSize());
                                frame.setVisible(true);
                            }
                        }
                    }
                }
            }

        });

        // endregion
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
        mainPanel.setLayout(new GridLayoutManager(5, 2, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.setMinimumSize(new Dimension(800, 700));
        mainPanel.setPreferredSize(new Dimension(800, 700));
        LineGraphsButton = new JRadioButton();
        LineGraphsButton.setText("Line graphics");
        mainPanel.add(LineGraphsButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(320, 19), null, 0, false));
        BoxPlotButton = new JRadioButton();
        BoxPlotButton.setText("Boxplot");
        mainPanel.add(BoxPlotButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        OKButton = new JButton();
        OKButton.setText("OK");
        mainPanel.add(OKButton, new GridConstraints(3, 1, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ScatterPlotButton = new JRadioButton();
        ScatterPlotButton.setText("Scatter plot");
        mainPanel.add(ScatterPlotButton, new GridConstraints(1, 0, 3, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(320, 19), null, 0, false));
        VisualiseRunButton = new JRadioButton();
        VisualiseRunButton.setText("Visualise Run");
        mainPanel.add(VisualiseRunButton, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(320, 19), null, 0, false));
        boxplot2RadioButton = new JRadioButton();
        boxplot2RadioButton.setText("Boxplot2");
        mainPanel.add(boxplot2RadioButton, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        boxplot3RadioButton = new JRadioButton();
        boxplot3RadioButton.setText("Boxplot3");
        mainPanel.add(boxplot3RadioButton, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}
