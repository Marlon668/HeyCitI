package gui;

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
    private JRadioButton DropBoxButton;
    private JRadioButton LineGraphsButton;
    private JRadioButton ScatterPlotButton;
    private JRadioButton VisualiseRunButton;
    private Integer option;
    private SimulationRunner simulationRunner;

    public ResultGUI(SimulationRunner simulationRunner) {

        this.simulationRunner = simulationRunner;

        ButtonGroup positionButtonGroup = new ButtonGroup();
        positionButtonGroup.add(LineGraphsButton);
        positionButtonGroup.add(DropBoxButton);
        positionButtonGroup.add(ScatterPlotButton);
        positionButtonGroup.add(VisualiseRunButton);

        option = 1;
        LineGraphsButton.addActionListener(e ->
            option = 1
            );
        DropBoxButton.addActionListener(e ->
            option = 2
        );
        ScatterPlotButton.addActionListener(e ->
            option = 3
        );
        VisualiseRunButton.addActionListener(e ->
            option = 4
        );



        OKButton.addActionListener((e) -> {
            if (option == 1) {
                Window window = javax.swing.SwingUtilities.getWindowAncestor(mainPanel);
                window.dispose();
                JFrame frame = new JFrame("Configure runs");
                ConfigureRun configureRun = new ConfigureRun(simulationRunner);
                frame.setContentPane(configureRun.getMainPanel());
                frame.setMinimumSize(configureRun.getMainPanel().getMinimumSize());
                frame.setPreferredSize(configureRun.getMainPanel().getPreferredSize());
                frame.setVisible(true);
            } else {
                if(option == 2) {
                    Window window = javax.swing.SwingUtilities.getWindowAncestor(mainPanel);
                    window.dispose();
                    JFrame frame = new JFrame("Configure runs");
                    ConfigureRun2 configureRun = new ConfigureRun2(simulationRunner);
                    frame.setContentPane(configureRun.getMainPanel());
                    frame.setMinimumSize(configureRun.getMainPanel().getMinimumSize());
                    frame.setPreferredSize(configureRun.getMainPanel().getPreferredSize());
                    frame.setVisible(true);
                }
                else{
                    if(option ==3) {
                        Window window = javax.swing.SwingUtilities.getWindowAncestor(mainPanel);
                        window.dispose();
                        JFrame frame = new JFrame("Configure runs");
                        ConfigureRun3 configureRun = new ConfigureRun3(simulationRunner);
                        frame.setContentPane(configureRun.getMainPanel());
                        frame.setMinimumSize(configureRun.getMainPanel().getMinimumSize());
                        frame.setPreferredSize(configureRun.getMainPanel().getPreferredSize());
                        frame.setVisible(true);
                    }
                    else{
                        Window window = javax.swing.SwingUtilities.getWindowAncestor(mainPanel);
                        window.dispose();
                        JFrame frame = new JFrame("Configure runs");
                        ConfigureRun4 configureRun = new ConfigureRun4(simulationRunner);
                        frame.setContentPane(configureRun.getMainPanel());
                        frame.setMinimumSize(configureRun.getMainPanel().getMinimumSize());
                        frame.setPreferredSize(configureRun.getMainPanel().getPreferredSize());
                        frame.setVisible(true);
                    }
                }
            }

        });

        // endregion
    }


    public JPanel getMainPanel() {
        return mainPanel;
    }


}
