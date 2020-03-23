package gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import iot.Environment;
import iot.InputProfile;
import iot.networkentity.Mote;

import javax.swing.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.io.StringWriter;
import java.time.temporal.ChronoUnit;


public class EditInputProfileGUI {
    private JTabbedPane tabbedPane1;
    private JPanel mainPanel;
    private JComboBox numberOfRoundsComboBox;
    private JSpinner moteProbSpinner;
    private JButton classSaveButton;
    private JButton updateMoteButton;
    private JComboBox<String> moteNumberComboBox;
    private JLabel QOSLabel;
    private JSpinner durationSpinner;
    private JComboBox timeUnitComboBox;
    private JSpinner bufferProbSpinner;
    private JSpinner alphaProbSpinner;
    private JSpinner pathProbSpinner;
    private JSpinner synchronisationSpinner;
    private JSpinner runs;
    private JSpinner calculateFirstSpinner;
    private InputProfile inputProfile;
    private Environment environment;

    public EditInputProfileGUI(InputProfile inputProfile, Environment environment) {
        this.inputProfile = inputProfile;
        this.environment = environment;
        refresh();

        classSaveButton.addActionListener(e -> {
            if (numberOfRoundsComboBox.getSelectedItem() != null) {
                inputProfile.setNumberOfRuns(Integer.valueOf((String) numberOfRoundsComboBox.getSelectedItem()));
            }
            inputProfile
                .setSimulationDuration(((Double) durationSpinner.getValue()).longValue())
                .setTimeUnit((ChronoUnit) timeUnitComboBox.getSelectedItem());
            refresh();
        });

        updateMoteButton.addActionListener(e -> {
            inputProfile.putProbabilityForMote(moteNumberComboBox.getSelectedIndex(), (Double) moteProbSpinner.getValue());
            inputProfile.setBetterpath((Double) pathProbSpinner.getValue());
            inputProfile.setAlphavalue((Double) alphaProbSpinner.getValue());
            inputProfile.setBufferSize((Integer) bufferProbSpinner.getValue());
            inputProfile.setSynchronisation((Integer) calculateFirstSpinner.getValue());
            inputProfile.setAmountRuns((Integer) runs.getValue());
            inputProfile.setSetupFirst((Integer) calculateFirstSpinner.getValue());
            refresh();
        });

        moteNumberComboBox.addActionListener(e -> {
            if (inputProfile.getProbabilitiesForMotesKeys().contains((moteNumberComboBox.getSelectedIndex()))) {
                moteProbSpinner.setValue(inputProfile.getProbabilityForMote(moteNumberComboBox.getSelectedIndex()));
            } else {
                moteProbSpinner.setValue(1.00);
                inputProfile.putProbabilityForMote(moteNumberComboBox.getSelectedIndex(), (Double) moteProbSpinner.getValue());
                refresh();
            }
        });


    }

    private void refresh() {
        DOMSource DOMSource = new DOMSource(inputProfile.getXmlSource());
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = tf.newTransformer();
            transformer.transform(DOMSource, result);
        } catch (TransformerException e) {
            e.printStackTrace();
        }

        QOSLabel.setText(inputProfile.getName());
        if (environment != null) {
            numberOfRoundsComboBox.setSelectedItem(Integer.toString(inputProfile.getNumberOfRuns()));
        }
        int moteNumValue = 0;

        if (environment != null) {
            moteNumberComboBox.removeAllItems();
            for (Mote mote : environment.getMotes()) {
                moteNumberComboBox.addItem("Mote " + (environment.getMotes().indexOf(mote) + 1));
            }
            moteNumberComboBox.setSelectedIndex(moteNumValue);
        } else {
            moteNumberComboBox.removeAllItems();
        }
        moteProbSpinner.setModel(new SpinnerNumberModel(inputProfile.getProbabilityForMote(moteNumberComboBox.getSelectedIndex()), 0, 1, 0.01));
        alphaProbSpinner.setModel(new SpinnerNumberModel(inputProfile.getAlphavalue(), 0, 1, 0.01));
        pathProbSpinner.setModel(new SpinnerNumberModel(inputProfile.getBetterpath(), 0, 1, 0.01));
        bufferProbSpinner.setModel(new SpinnerNumberModel(inputProfile.getBuffersize(), 0, Integer.MAX_VALUE, 1));
        synchronisationSpinner.setModel(new SpinnerNumberModel(inputProfile.getSynchronisation(),0,1,1));
        calculateFirstSpinner.setModel(new SpinnerNumberModel(inputProfile.getSetupFirst(),0,1,1));
        runs.setModel(new SpinnerNumberModel(inputProfile.getAmountRuns(),1,Integer.MAX_VALUE,1));
        timeUnitComboBox.setSelectedItem(inputProfile.getTimeUnit());
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
        moteProbSpinner = new JSpinner();
        panel1.add(moteProbSpinner, new GridConstraints(4, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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
        QOSLabel = new JLabel();
        QOSLabel.setText("Label");
        panel1.add(QOSLabel, new GridConstraints(1, 2, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        classSaveButton = new JButton();
        classSaveButton.setText("Save");
        panel1.add(classSaveButton, new GridConstraints(3, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("QoS:");
        panel1.add(label5, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Reliable Communication");
        panel1.add(label6, new GridConstraints(2, 2, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        moteNumberComboBox = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        moteNumberComboBox.setModel(defaultComboBoxModel1);
        panel1.add(moteNumberComboBox, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        numberOfRoundsComboBox = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel2 = new DefaultComboBoxModel();
        defaultComboBoxModel2.addElement("1");
        defaultComboBoxModel2.addElement("2");
        defaultComboBoxModel2.addElement("3");
        defaultComboBoxModel2.addElement("4");
        defaultComboBoxModel2.addElement("5");
        defaultComboBoxModel2.addElement("6");
        defaultComboBoxModel2.addElement("7");
        defaultComboBoxModel2.addElement("8");
        defaultComboBoxModel2.addElement("9");
        defaultComboBoxModel2.addElement("10");
        defaultComboBoxModel2.addElement("15");
        defaultComboBoxModel2.addElement("20");
        defaultComboBoxModel2.addElement("30");
        defaultComboBoxModel2.addElement("40");
        defaultComboBoxModel2.addElement("50");
        defaultComboBoxModel2.addElement("100");
        defaultComboBoxModel2.addElement("250");
        defaultComboBoxModel2.addElement("500");
        defaultComboBoxModel2.addElement("1000");
        numberOfRoundsComboBox.setModel(defaultComboBoxModel2);
        panel1.add(numberOfRoundsComboBox, new GridConstraints(3, 2, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Duration of simulation");
        panel1.add(label7, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        durationSpinner = new JSpinner();
        panel1.add(durationSpinner, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("Time unit");
        panel1.add(label8, new GridConstraints(5, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        timeUnitComboBox = new JComboBox();
        panel1.add(timeUnitComboBox, new GridConstraints(5, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}

