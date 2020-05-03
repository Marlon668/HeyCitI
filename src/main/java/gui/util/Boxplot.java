package gui.util;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.orsonpdf.PDFDocument;
import com.orsonpdf.PDFGraphics2D;
import com.orsonpdf.Page;
import gui.MainGUI;
import iot.networkentity.Mote;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.BoxAndWhiskerToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;

/**
 * Class used for plotting a box plot
 */
public class Boxplot {

    private DefaultBoxAndWhiskerCategoryDataset dataset;
    private CategoryPlot plot;
    private ChartPanel chartPanel;
    private JPanel controlPanel;
    private List<String> legend;
    private final List<Color> clut = new ArrayList<Color>();
    private boolean normalise;

    private void initClut() {
        clut.add(Color.red);
        clut.add(Color.blue);
        clut.add(Color.green);
        clut.add(Color.yellow);
        clut.add(Color.orange);
        clut.add(Color.cyan);
        clut.add(Color.magenta);
        clut.add(Color.blue);
    }

    public Boxplot(HashMap<Mote,HashMap<Integer, HashMap<Integer,List<Double>>>> airQuality, HashMap<Mote,HashMap<Integer,HashMap<Integer,List<Integer>>>> adaptations,ArrayList<Mote> motes, int index,int data,boolean normalise) {
        initClut();
        this.normalise = normalise;
        createDataset(airQuality,adaptations,motes,index,data);
        createChartPanel(motes,index,data);
        createControlPanel(airQuality,adaptations,motes,index,data,normalise);
    }

    /**
     * Translate data in a dataset to plot the boxplots
     * @param airQuality hashmap containing data about the air quality for every mote for an amount of runs for the same configuration
     * @param adaptations hashmap containing data about the amount of adaptations made for every mote for an amount of runs for the same configuration
     * @param motes arraylist containing every mote of the configuration
     * @param index of which mote must we show a window containing boxplots to respresent its data
     * @param data 0 = showing air qualtiy, 1 = showing amount of adaptations
     */
    private void createDataset(HashMap<Mote,HashMap<Integer, HashMap<Integer,List<Double>>>> airQuality, HashMap<Mote,HashMap<Integer,HashMap<Integer,List<Integer>>>> adaptations,ArrayList<Mote> motes,int index,int data) {
        legend = new ArrayList<>();
        if(data==0){
            Mote mote = motes.get(index);
            dataset = new DefaultBoxAndWhiskerCategoryDataset();
            HashMap<Integer, HashMap<Integer,List<Double>>> resultsMote = airQuality.get(mote);
            double resultsNoAdaptation = 1;
            if(normalise) {
                resultsNoAdaptation = resultsMote.get(0).get(0).get(0);
            }
            resultsMote.remove(0);
            for (Map.Entry<Integer, HashMap<Integer, List<Double>>> moteEntry : resultsMote.entrySet()) {
                for (Map.Entry<Integer,List<Double>> moteEntry3 : moteEntry.getValue().entrySet()) {
                    List<Double> finalResults = moteEntry3.getValue();
                    List<Double> resultsRelativeToNoAdaptation = new ArrayList<>();
                    for(double result : finalResults){
                        double newResult = result/resultsNoAdaptation;
                        resultsRelativeToNoAdaptation.add(newResult);
                    }
                    dataset.add(resultsRelativeToNoAdaptation,"" ,moteEntry.getKey());
                    legend.add("Height : " + moteEntry.getKey() + "Width" + moteEntry3.getKey());

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
                    dataset.add(finalResults, "",moteEntry.getKey());
                    legend.add("Height : " + moteEntry.getKey() + "Width" + moteEntry3.getKey());
                }
            }
        }
        System.out.println(dataset.getColumnCount());

    }

    private void createChartPanel(ArrayList<Mote>motes,int index,int data) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = null;
        if(data==0) {
            yAxis = new NumberAxis("Air-Quality");
        }
        else{
            yAxis = new NumberAxis("Number of  adaptations");
        }
        BoxAndWhiskerRenderer renderer = new BoxAndWhiskerRenderer();
        System.out.println(dataset.getMeanValue(0,0));
        renderer.setFillBox(true);
        renderer.setUseOutlinePaintForWhiskers(true);
        yAxis.setAutoRangeIncludesZero(false);
        renderer.setSeriesToolTipGenerator(1, new BoxAndWhiskerToolTipGenerator());
        renderer.setMedianVisible(true);
        renderer.setMeanVisible(false);
        plot = new CategoryPlot(dataset, xAxis, yAxis, renderer);
        plot.getDomainAxis(0).setVisible(false);
        plot.setDrawingSupplier(new ChartDrawingSupplier());
        LegendItemCollection legendList = new LegendItemCollection();
        for(int i=0;i<legend.size();i++){
            LegendItem newLegend = new LegendItem(legend.get(i),clut.get(i));
            legendList.add(newLegend);
        }
        plot.setFixedLegendItems(legendList);
        JFreeChart chart = new JFreeChart("Cyclist : " + motes.get(index).getEUI(), plot);
        chartPanel = new ChartPanel(chart);
        chartPanel.setMouseWheelEnabled(true);
    }


    private void createControlPanel(HashMap<Mote,HashMap<Integer, HashMap<Integer,List<Double>>>> airQuality, HashMap<Mote,HashMap<Integer,HashMap<Integer,List<Integer>>>> adaptations,ArrayList<Mote> motes, int index,int data,boolean normalise) {
        controlPanel = new JPanel();
        int index2 = index;
        controlPanel.add(new JButton(new AbstractAction("\u22b2Prev") {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (data == 1) {
                    Window window = javax.swing.SwingUtilities.getWindowAncestor(controlPanel);
                    window.dispose();
                    JFrame frame = new JFrame();
                    Boxplot boxplot = new Boxplot(airQuality, adaptations, motes, index2, data - 1,normalise);
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
                        Boxplot boxplot = new Boxplot(airQuality, adaptations, motes, index2 - 1, data + 1,normalise);
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
                    Boxplot boxplot = new Boxplot(airQuality, adaptations, motes, index2, 1,normalise);
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
                        Boxplot boxplot = new Boxplot(airQuality, adaptations, motes, index2 + 1, 0,normalise);
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
