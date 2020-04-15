package gui.util;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.orsonpdf.PDFDocument;
import com.orsonpdf.PDFGraphics2D;
import com.orsonpdf.Page;
import gui.MainGUI;
import iot.Environment;
import iot.Result;
import iot.networkentity.Mote;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;

/** @see //stackoverflow.com/questions/6844759 */
public class Boxplot {

    private int COLS;
    private int VISIBLE;
    private int ROWS;
    private int VALUES;
    private static final Random rnd = new Random();
    private int index;
    private List<String> columns;
    private List<List<List<Double>>> data;
    private DefaultBoxAndWhiskerCategoryDataset dataset;
    private CategoryPlot plot;
    private ChartPanel chartPanel;
    private JPanel controlPanel;
    private int start = 0;

    public Boxplot(HashMap<Mote,HashMap<Integer, HashMap<Integer,List<Double>>>> airQuality, HashMap<Mote,HashMap<Integer,HashMap<Integer,List<Integer>>>> adaptations,ArrayList<Mote> motes, int index,int data) {
        createDataset(airQuality,adaptations,motes,index,data);
        createChartPanel(motes,index,data);
        createControlPanel(airQuality,adaptations,motes,index,data);
    }

    private void createDataset(HashMap<Mote,HashMap<Integer, HashMap<Integer,List<Double>>>> airQuality, HashMap<Mote,HashMap<Integer,HashMap<Integer,List<Integer>>>> adaptations,ArrayList<Mote> motes,int index,int data) {
        if(data==0){
            Mote mote = motes.get(index);
            dataset = new DefaultBoxAndWhiskerCategoryDataset();
            HashMap<Integer, HashMap<Integer,List<Double>>> resultsMote = airQuality.get(mote);
            for (Map.Entry<Integer, HashMap<Integer, List<Double>>> moteEntry : resultsMote.entrySet()) {
                for (Map.Entry<Integer,List<Double>> moteEntry3 : moteEntry.getValue().entrySet()) {
                    List<Double> finalResults = moteEntry3.getValue();
                    dataset.add(finalResults, "Height : " + moteEntry.getKey() + "Width" + moteEntry3.getKey(),moteEntry.getKey());
                    System.out.println(finalResults.size());
                }
            }
        }
        else{
            Mote mote = motes.get(index);
            dataset = new DefaultBoxAndWhiskerCategoryDataset();
            HashMap<Integer, HashMap<Integer,List<Integer>>> resultsMote = adaptations.get(mote);
            for (Map.Entry<Integer, HashMap<Integer, List<Integer>>> moteEntry : resultsMote.entrySet()) {
                for (Map.Entry<Integer,List<Integer>> moteEntry3 : moteEntry.getValue().entrySet()) {
                    List<Integer> finalResults = moteEntry3.getValue();
                    dataset.add(finalResults, "Height : " + moteEntry.getKey() + "Width" + moteEntry3.getKey(),moteEntry.getKey());
                    System.out.println(finalResults.size());
                }
            }
        }
        System.out.println(dataset.getColumnCount());

    }

    private void createChartPanel(ArrayList<Mote>motes,int index,int data) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis("Air-Quality");
        if(data==0) {
            yAxis = new NumberAxis("Air-Quality");
        }
        else{
            yAxis = new NumberAxis("Amount adaptations");
        }
        BoxAndWhiskerRenderer renderer = new BoxAndWhiskerRenderer();
        renderer.setMaximumBarWidth(0.05);
        System.out.println("Size : " + dataset.getColumnKeys().size());
        plot = new CategoryPlot(dataset, xAxis, yAxis, renderer);
        plot.getDomainAxis(0).setVisible(false);
        JFreeChart chart = new JFreeChart("Mote : " + motes.get(index).getEUI(), plot);
        chartPanel = new ChartPanel(chart);
        chartPanel.setMouseWheelEnabled(true);
    }

    private void createControlPanel(HashMap<Mote,HashMap<Integer, HashMap<Integer,List<Double>>>> airQuality, HashMap<Mote,HashMap<Integer,HashMap<Integer,List<Integer>>>> adaptations,ArrayList<Mote> motes, int index,int data) {
        controlPanel = new JPanel();
        int index2 = index;
        controlPanel.add(new JButton(new AbstractAction("\u22b2Prev") {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (data == 1) {
                    Window window = javax.swing.SwingUtilities.getWindowAncestor(controlPanel);
                    window.dispose();
                    JFrame frame = new JFrame();
                    Boxplot boxplot = new Boxplot(airQuality, adaptations, motes, index2, data - 1);
                    frame.add(boxplot.getChartPanel(), BorderLayout.CENTER);
                    frame.add(boxplot.getControlPanel(), BorderLayout.SOUTH);
                    frame.pack();
                    frame.setLocationRelativeTo(null);
                    frame.setVisible(true);
                } else {
                    if (index2 != 0) {
                        Window window = javax.swing.SwingUtilities.getWindowAncestor(controlPanel);
                        window.dispose();
                        JFrame frame = new JFrame();
                        Boxplot boxplot = new Boxplot(airQuality, adaptations, motes, index2 - 1, data + 1);
                        frame.add(boxplot.getChartPanel(), BorderLayout.CENTER);
                        frame.add(boxplot.getControlPanel(), BorderLayout.SOUTH);
                        frame.pack();
                        frame.setLocationRelativeTo(null);
                        frame.setVisible(true);
                    }
                }
            }
        }));
        controlPanel.add(new JButton(new AbstractAction("Next\u22b3") {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (data == 0) {
                    Window window = javax.swing.SwingUtilities.getWindowAncestor(controlPanel);
                    window.dispose();
                    JFrame frame = new JFrame();
                    Boxplot boxplot = new Boxplot(airQuality, adaptations, motes, index2, 1);
                    frame.add(boxplot.getChartPanel(), BorderLayout.CENTER);
                    frame.add(boxplot.getControlPanel(), BorderLayout.SOUTH);
                    frame.pack();
                    frame.setLocationRelativeTo(null);
                    frame.setVisible(true);
                } else {
                    if (index2 != motes.size() - 1) {
                        Window window = javax.swing.SwingUtilities.getWindowAncestor(controlPanel);
                        window.dispose();
                        JFrame frame = new JFrame();
                        Boxplot boxplot = new Boxplot(airQuality, adaptations, motes, index2 + 1, 0);
                        frame.add(boxplot.getChartPanel(), BorderLayout.CENTER);
                        frame.add(boxplot.getControlPanel(), BorderLayout.SOUTH);
                        frame.pack();
                        frame.setLocationRelativeTo(null);
                        frame.setVisible(true);
                    }
                }
            }
        }));

        controlPanel.add(new JButton(new AbstractAction("Save\u22b3") {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Save output");
                fc.setFileFilter(new FileNameExtensionFilter("pdf output", "pdf"));

                File file = new File(MainGUI.class.getProtectionDomain().getCodeSource().getLocation().getPath());
                file = new File(file.getParent());
                fc.setCurrentDirectory(file);

                int returnVal = fc.showSaveDialog(controlPanel);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    file = GUIUtil.getOutputFile(fc.getSelectedFile(), "pdf");
                    PDFDocument pdfDoc = new PDFDocument();
                    pdfDoc.setTitle("Box Plot");
                    Page page = pdfDoc.createPage(new Rectangle(chartPanel.getWidth(), chartPanel.getHeight()));
                    PDFGraphics2D g2 = page.getGraphics2D();
                    chartPanel.getChart().draw(g2, new Rectangle(0, 0,chartPanel.getWidth(),chartPanel.getHeight()));
                    pdfDoc.writeToFile(file);
                }

            }
        }));

    }

    public ChartPanel getChartPanel() {
        return chartPanel;
    }

    public JPanel getControlPanel() {
        return controlPanel;
    }

}
