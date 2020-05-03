package gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import gui.util.Boxplot2;
import gui.util.Boxplot3;
import gui.util.GUIUtil;
import iot.Parameters;
import iot.SimulationRunner;
import iot.networkentity.Mote;
import iot.networkentity.UserMote;
import org.apache.commons.collections4.list.SetUniqueList;
import util.Pair;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Class used for configuring the testruns for visualising the diversity of the air quality of the
 * next connection the cyclist would take calculated by the routing application and the route-evaluator
 */
public class ConfigureRun6 {
    private SimulationRunner simulationRunner;
    private JTabbedPane tabbedPane1;
    private JPanel mainPanel;
    private JButton classGenerateButton;
    private JButton openButton;
    private JSpinner pathProbSpinner;
    private JSpinner amountRunsSpinner;
    private JSpinner amountNoiseSpinner;
    private JSpinner analysingMethod;
    private JSpinner bufferProbWidthSpinner;
    private JSpinner bufferProbHeightSpinner;
    private JButton SaveButton;
    private JButton Show;
    private int amountRuns;
    private List<Pair<Integer, Integer>> data = SetUniqueList.setUniqueList(new ArrayList<Pair<Integer, Integer>>());

    public ConfigureRun6(SimulationRunner simulationRunner) {
        this.simulationRunner = simulationRunner;

        refresh();


        SaveButton.addActionListener(e -> {
            if (simulationRunner.getErrorListConnections() != null) {
                refresh();
                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Save output");
                fc.setFileFilter(new FileNameExtensionFilter("xml output", "xml"));

                File file = new File(MainGUI.class.getProtectionDomain().getCodeSource().getLocation().getPath());
                file = new File(file.getParent());
                fc.setCurrentDirectory(file);

                int returnVal = fc.showSaveDialog(mainPanel);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    file = GUIUtil.getOutputFile(fc.getSelectedFile(), "xml");
                    simulationRunner.saveResultsBoxPlotToFile3(file);
                }
            } else {
                String message = "No results available to save";
                JOptionPane.showMessageDialog(null, message, "No Results", JOptionPane.ERROR_MESSAGE);
            }
        });

        classGenerateButton.addActionListener(e -> {
            if (simulationRunner == null) {
                String message = "No Configuration loaded";
                JOptionPane.showMessageDialog(null, message, "Setting error", JOptionPane.ERROR_MESSAGE);
            } else {
                if (simulationRunner.getSimulation().getInputProfile().isEmpty()) {
                    String message = "No inputprofile selected";
                    JOptionPane.showMessageDialog(null, message, "Setting error", JOptionPane.ERROR_MESSAGE);
                } else {
                    amountRuns = (Integer) amountRunsSpinner.getValue();
                    int bufferSizeHeight = (Integer) bufferProbHeightSpinner.getValue();
                    int bufferSizeWidth = (Integer) bufferProbWidthSpinner.getValue();
                    data.add(new Pair<>(bufferSizeHeight, bufferSizeWidth));
                    simulationRunner.getParameters().setAnalysingMethod((Integer) analysingMethod.getValue());
                    simulationRunner.getParameters().setBetterpath((Double) pathProbSpinner.getValue());
                    simulationRunner.getEnvironmentAPI().getSensors().forEach(sensor -> {
                        sensor.setNoiseRatio((Integer) amountNoiseSpinner.getValue());
                    });
                    simulationRunner.resultRunBoxPlot3(amountRuns, data);
                }
            }

        });

        Show.addActionListener(e -> {
            if (simulationRunner.getErrorListConnections() != null) {
                Window window = SwingUtilities.getWindowAncestor(mainPanel);
                window.dispose();
                ArrayList<Mote> motes = new ArrayList<Mote>();
                simulationRunner.getEnvironment().getMotes().forEach(mote -> {
                    if (mote instanceof UserMote) {
                        motes.add(mote);
                    }
                });
                JFrame frame = new JFrame();

                Boxplot3 boxplot = new Boxplot3(simulationRunner.getErrorListConnections(), motes, 0);
                frame.add(boxplot.getChartPanel(), BorderLayout.CENTER);
                frame.add(boxplot.getControlPanel(), BorderLayout.SOUTH);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            } else {
                String message = "No results available";
                JOptionPane.showMessageDialog(null, message, "No Results", JOptionPane.ERROR_MESSAGE);
            }

        });

        openButton.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Load a Pollution Environment");
            fc.setFileFilter(new FileNameExtensionFilter("xml configuration", "xml"));

            File file = new File(MainGUI.class.getProtectionDomain().getCodeSource().getLocation().getPath());
            String basePath = file.getParentFile().getParent();
            fc.setCurrentDirectory(new File(Paths.get(basePath, "settings", "results").toUri()));

            int returnVal = fc.showOpenDialog(mainPanel);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                JFrame frame = new JFrame("Loading Results");
                LoadingGUI loadingGUI = new LoadingGUI();
                frame.setContentPane(loadingGUI.getMainPanel());
                frame.setMinimumSize(new Dimension(300, 300));
                frame.setVisible(true);

                try {
                    simulationRunner.readResultsBoxPlotFromFile3(fc.getSelectedFile());
                } catch (IllegalStateException | ParserConfigurationException f) {
                    String message = "No Valid File selected loaded";
                    JOptionPane.showMessageDialog(null, message, "Setting error", JOptionPane.ERROR_MESSAGE);
                }
                frame.dispose();
            }

        });
    }

    private void refresh() {
        bufferProbHeightSpinner.setModel(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
        bufferProbWidthSpinner.setModel(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
        pathProbSpinner.setModel(new SpinnerNumberModel(1, 0.001, 1, 0.001));
        analysingMethod.setModel(new SpinnerNumberModel(0, 0, 2, 1));
        amountRunsSpinner.setModel(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
        amountNoiseSpinner.setModel(new SpinnerNumberModel(1, 1, 255, 1));
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
        mainPanel.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.setMinimumSize(new Dimension(750, 400));
        mainPanel.setPreferredSize(new Dimension(750, 400));
        tabbedPane1 = new JTabbedPane();
        tabbedPane1.setEnabled(true);
        mainPanel.add(tabbedPane1, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(4, 6, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane1.addTab("Setting", panel1);
        final JLabel label1 = new JLabel();
        label1.setText("Amount Runs");
        panel1.add(label1, new GridConstraints(2, 0, 2, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(119, 28), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Amount Noise");
        panel1.add(label2, new GridConstraints(2, 2, 2, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(119, 28), null, 0, false));
        amountNoiseSpinner = new JSpinner();
        panel1.add(amountNoiseSpinner, new GridConstraints(2, 3, 2, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(103, 28), null, 0, false));
        amountRunsSpinner = new JSpinner();
        panel1.add(amountRunsSpinner, new GridConstraints(2, 1, 2, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(103, 28), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Better path");
        panel1.add(label3, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(119, 28), null, 0, false));
        analysingMethod = new JSpinner();
        panel1.add(analysingMethod, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Analysing Method");
        panel1.add(label4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        classGenerateButton = new JButton();
        classGenerateButton.setText("Generate");
        panel1.add(classGenerateButton, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        bufferProbHeightSpinner = new JSpinner();
        panel1.add(bufferProbHeightSpinner, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(103, 28), null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Buffer size (height)");
        panel1.add(label5, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(119, 28), null, 0, false));
        bufferProbWidthSpinner = new JSpinner();
        panel1.add(bufferProbWidthSpinner, new GridConstraints(1, 3, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(103, 28), null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Buffer size (width)");
        panel1.add(label6, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(119, 28), null, 0, false));
        SaveButton = new JButton();
        SaveButton.setText("Save");
        panel1.add(SaveButton, new GridConstraints(1, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pathProbSpinner = new JSpinner();
        panel1.add(pathProbSpinner, new GridConstraints(0, 3, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        Show = new JButton();
        Show.setText("Show");
        panel1.add(Show, new GridConstraints(3, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        openButton = new JButton();
        openButton.setText("Open");
        panel1.add(openButton, new GridConstraints(2, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}

