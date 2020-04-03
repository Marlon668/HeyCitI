package gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import iot.Parameters;
import iot.SimulationRunner;

import javax.swing.*;
import java.awt.*;


public class editParameters {
    private Parameters parameters;
    private SimulationRunner simulationRunner;
    private JTabbedPane tabbedPane1;
    private JPanel mainPanel;
    private JButton classSaveButton;
    private JButton updateMoteButton;
    private JSpinner bufferProbHeightSpinner;
    private JSpinner pathProbSpinner;
    private JSpinner synchronisationSpinner;
    private JSpinner runs;
    private JSpinner calculateFirstSpinner;
    private JSpinner removeConn;
    private JLabel removeVisitedConnectionsLabel;
    private JSpinner analysingMethod;
    private JSpinner bufferProbWidthSpinner;

    public editParameters(Parameters parameters, SimulationRunner simulationRunner) {
        this.parameters = parameters;
        this.simulationRunner = simulationRunner;
        refresh();


        classSaveButton.addActionListener(e -> {
            parameters.setBetterpath((Double) pathProbSpinner.getValue());
            parameters.setBufferSizeHeight((Integer) bufferProbHeightSpinner.getValue());
            parameters.setBuffersizeWidth((Integer) bufferProbWidthSpinner.getValue());
            parameters.setSynchronisation((Integer) synchronisationSpinner.getValue());
            parameters.setAmountRuns((Integer) runs.getValue());
            parameters.setSetupFirst((Integer) calculateFirstSpinner.getValue());
            parameters.setRemoveConn((Integer) removeConn.getValue());
            parameters.setAnalysingMethod((Integer) analysingMethod.getValue());
            refresh();
            parameters.updateFile(simulationRunner.getParameterFile());
        });

        updateMoteButton.addActionListener(e -> {
            parameters.setBetterpath((Double) pathProbSpinner.getValue());
            parameters.setBufferSizeHeight((Integer) bufferProbHeightSpinner.getValue());
            parameters.setBuffersizeWidth((Integer) bufferProbWidthSpinner.getValue());
            parameters.setSynchronisation((Integer) synchronisationSpinner.getValue());
            parameters.setAmountRuns((Integer) runs.getValue());
            parameters.setSetupFirst((Integer) calculateFirstSpinner.getValue());
            parameters.setRemoveConn((Integer) removeConn.getValue());
            parameters.setAnalysingMethod((Integer) analysingMethod.getValue());
            refresh();
        });

    }
    private void refresh() {
        bufferProbHeightSpinner.setModel(new SpinnerNumberModel(parameters.getBuffersizeHeight(), 1, Integer.MAX_VALUE, 1));
        bufferProbWidthSpinner.setModel(new SpinnerNumberModel(parameters.getBuffersizeWidth(), 1, Integer.MAX_VALUE, 1));
        synchronisationSpinner.setModel(new SpinnerNumberModel(parameters.getSynchronisation(),0,1,1));
        calculateFirstSpinner.setModel(new SpinnerNumberModel(parameters.getSetupFirst(),0,2,1));
        runs.setModel(new SpinnerNumberModel(parameters.getAmountRuns(),1,Integer.MAX_VALUE,1));
        pathProbSpinner.setModel(new SpinnerNumberModel(parameters.getBetterpath(), 0.001, 1, 0.001));
        removeConn.setModel(new SpinnerNumberModel(parameters.getRemoveConn(), 0, 1, 1));
        analysingMethod.setModel(new SpinnerNumberModel(parameters.getAnalysingMethod(), 0, 2, 1));
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
        panel1.setLayout(new GridLayoutManager(7, 7, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane1.addTab("Configure", panel1);
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("InputProfile:");
        panel1.add(label1, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Number of rounds");
        panel1.add(label2, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel1.add(spacer2, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel1.add(spacer3, new GridConstraints(1, 6, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        panel1.add(spacer4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        updateMoteButton = new JButton();
        updateMoteButton.setText("Update");
        panel1.add(updateMoteButton, new GridConstraints(4, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Activity probability");
        panel1.add(label3, new GridConstraints(4, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Mote");
        panel1.add(label4, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        classSaveButton = new JButton();
        classSaveButton.setText("Save");
        panel1.add(classSaveButton, new GridConstraints(3, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("QoS:");
        panel1.add(label5, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Reliable Communication");
        panel1.add(label6, new GridConstraints(2, 2, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}

