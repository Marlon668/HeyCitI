package gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import gui.util.GUIUtil;
import gui.util.RefineryUtilities;
import gui.util.ScatterPlot;
import iot.Parameters;
import iot.SimulationRunner;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.File;
import java.nio.file.Paths;

/**
 * Class used for configuring the testruns for visualising the relationship between the air-quality of a path
 * of a cyclist and the distance of that path
 */
public class ConfigureRun3 {
    private SimulationRunner simulationRunner;
    private JTabbedPane tabbedPane1;
    private JPanel mainPanel;
    private JButton classGenerateButton;
    private JButton openButton;
    private JSpinner bufferProbHeightMinSpinner;
    private JSpinner bufferProbHeightMaxSpinner;
    private JSpinner pathProbSpinner;

    private JSpinner analysingMethod;
    private JSpinner bufferProbWidthMinSpinner;
    private JSpinner bufferProbWidthMaxSpinner;
    private JButton SaveButton;
    private JButton Show;
    private JPanel Normalise;
    private JLabel N;
    private JSpinner normaliseSpinner;
    private int minimumBufferSizeHeight;
    private int maximumBufferSizeHeight;
    private int minimumBufferSizeWidth;
    private int maximumBufferSizeWidth;

    public ConfigureRun3(SimulationRunner simulationRunner) {
        this.simulationRunner = simulationRunner;
        refresh();


        SaveButton.addActionListener(e -> {
            if (simulationRunner.getResults() != null) {
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
                    simulationRunner.saveResultsToFile(file);
                }
            } else {
                String message = "No results available";
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
                    minimumBufferSizeHeight = (Integer) bufferProbHeightMinSpinner.getValue();
                    maximumBufferSizeHeight = (Integer) bufferProbHeightMaxSpinner.getValue();
                    minimumBufferSizeWidth = (Integer) bufferProbWidthMinSpinner.getValue();
                    maximumBufferSizeWidth = (Integer) bufferProbWidthMaxSpinner.getValue();
                    if (minimumBufferSizeWidth <= maximumBufferSizeWidth && minimumBufferSizeHeight <= maximumBufferSizeHeight) {
                        simulationRunner.getParameters().setAnalysingMethod((Integer) analysingMethod.getValue());
                        simulationRunner.getParameters().setBetterpath((Double) pathProbSpinner.getValue());
                        simulationRunner.resultRun(minimumBufferSizeHeight, maximumBufferSizeHeight, minimumBufferSizeWidth, maximumBufferSizeWidth);
                    } else {
                        String message = "minimum buffersize width/height higher than maximum buffersize width/ height";
                        JOptionPane.showMessageDialog(null, message, "Setting error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }


        });

        Show.addActionListener(e -> {
            if (simulationRunner.getResults() != null) {
                Window window = SwingUtilities.getWindowAncestor(mainPanel);
                window.dispose();
                int normaliseValue = (Integer) normaliseSpinner.getValue();
                boolean normalise = false;
                if (normaliseValue == 1) {
                    normalise = true;
                }
                ScatterPlot var1 = new ScatterPlot("Scatter plot", simulationRunner.getResults(), normalise);
                var1.pack();
                RefineryUtilities.centerFrameOnScreen(var1);
                var1.setVisible(true);
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
                    simulationRunner.readResultsFromFile(fc.getSelectedFile());
                } catch (IllegalStateException | ParserConfigurationException f) {
                    String message = "No Valid File selected loaded";
                    JOptionPane.showMessageDialog(null, message, "Setting error", JOptionPane.ERROR_MESSAGE);
                }
                frame.dispose();
            }

        });
    }

    private void refresh() {
        bufferProbHeightMinSpinner.setModel(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
        bufferProbHeightMaxSpinner.setModel(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
        bufferProbWidthMinSpinner.setModel(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
        bufferProbWidthMaxSpinner.setModel(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
        normaliseSpinner.setModel(new SpinnerNumberModel(0, 0, 1, 1));
        pathProbSpinner.setModel(new SpinnerNumberModel(1, 0.001, 1, 0.001));
        analysingMethod.setModel(new SpinnerNumberModel(0, 0, 2, 1));
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
        Normalise = new JPanel();
        Normalise.setLayout(new GridLayoutManager(4, 5, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane1.addTab("Setting", Normalise);
        classGenerateButton = new JButton();
        classGenerateButton.setText("Generate");
        Normalise.add(classGenerateButton, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Min. Buffer size (height)");
        Normalise.add(label1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(119, 28), null, 0, false));
        bufferProbHeightMinSpinner = new JSpinner();
        Normalise.add(bufferProbHeightMinSpinner, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(103, 28), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Min. Buffer size (width)");
        Normalise.add(label2, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(119, 28), null, 0, false));
        bufferProbWidthMinSpinner = new JSpinner();
        Normalise.add(bufferProbWidthMinSpinner, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(103, 28), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Max. Buffer size (height)");
        Normalise.add(label3, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(119, 28), null, 0, false));
        bufferProbHeightMaxSpinner = new JSpinner();
        Normalise.add(bufferProbHeightMaxSpinner, new GridConstraints(2, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(103, 28), null, 0, false));
        bufferProbWidthMaxSpinner = new JSpinner();
        Normalise.add(bufferProbWidthMaxSpinner, new GridConstraints(3, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(103, 28), null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Max. Buffer size (width)");
        Normalise.add(label4, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(119, 28), null, 0, false));
        SaveButton = new JButton();
        SaveButton.setText("Save");
        Normalise.add(SaveButton, new GridConstraints(1, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        openButton = new JButton();
        openButton.setText("Open");
        Normalise.add(openButton, new GridConstraints(2, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        Show = new JButton();
        Show.setText("Show");
        Normalise.add(Show, new GridConstraints(3, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Analysing Method");
        Normalise.add(label5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Better path");
        Normalise.add(label6, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(119, 28), null, 0, false));
        analysingMethod = new JSpinner();
        Normalise.add(analysingMethod, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pathProbSpinner = new JSpinner();
        Normalise.add(pathProbSpinner, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        N = new JLabel();
        N.setText("Normalise");
        Normalise.add(N, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        normaliseSpinner = new JSpinner();
        Normalise.add(normaliseSpinner, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}

