package gui.util;

import gui.util.orsonpdf.PDFDocument;
import gui.util.orsonpdf.PDFGraphics2D;
import gui.util.orsonpdf.Page;
import gui.MainGUI;
import iot.networkentity.Mote;
import org.jfree.chart.*;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.BoxAndWhiskerToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class used for plotting a box plot
 */
public class Boxplot2 {

    private DefaultBoxAndWhiskerCategoryDataset dataset;
    private CategoryPlot plot;
    private ChartPanel chartPanel;
    private JPanel controlPanel;
    private List<String> legend;
    private final List<Color> clut = new ArrayList<Color>();
    private int bufferSizeWidth;
    private int bufferSizeHeight;


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

    public Boxplot2(HashMap<Mote,HashMap<Integer, HashMap<Integer,List<Double>>>> airQuality, HashMap<Mote,HashMap<Integer,HashMap<Integer,List<Double>>>> predictions, ArrayList<Mote> motes, int index) {
        initClut();
        createDataset(airQuality,predictions,motes,index);
        createChartPanel(motes,index);
        createControlPanel(airQuality,predictions,motes,index);
    }

    /**
     * Translate data in a dataset to plot the boxplots
     * @param airQuality hashmap containing data about the air quality for every mote for an amount of runs for the same configuration
     * @param motes arraylist containing every mote of the configuration
     * @param index of which mote must we show a window containing boxplots to represent its data
     */
    private void createDataset(HashMap<Mote,HashMap<Integer, HashMap<Integer,List<Double>>>> airQuality, HashMap<Mote,HashMap<Integer,HashMap<Integer,List<Double>>>> predictions,ArrayList<Mote> motes,int index) {
            legend = new ArrayList<>();
            Mote mote = motes.get(index);
            dataset = new DefaultBoxAndWhiskerCategoryDataset();
            HashMap<Integer, HashMap<Integer,List<Double>>> resultsMote2 = predictions.get(mote);
            for (Map.Entry<Integer, HashMap<Integer, List<Double>>> moteEntry : resultsMote2.entrySet()) {
                this.bufferSizeHeight = moteEntry.getKey();
                for (Map.Entry<Integer,List<Double>> moteEntry3 : moteEntry.getValue().entrySet()) {
                    this.bufferSizeWidth = moteEntry.getKey();
                    List<Double> finalResults = moteEntry3.getValue();
                    List<Double> resultsRelativeToNoAdaptation = new ArrayList<>();
                    for (double result : finalResults) {
                        double newResult = result;
                        resultsRelativeToNoAdaptation.add(newResult);
                    }
                    dataset.add(resultsRelativeToNoAdaptation, "2",moteEntry.getKey());
                    legend.add("Predicted value");
                }
            }
            HashMap<Integer, HashMap<Integer,List<Double>>> resultsMote = airQuality.get(mote);
            for (Map.Entry<Integer, HashMap<Integer, List<Double>>> moteEntry : resultsMote.entrySet()) {
                for (Map.Entry<Integer, List<Double>> moteEntry3 : moteEntry.getValue().entrySet()) {
                    List<Double> finalResults = moteEntry3.getValue();
                    List<Double> resultsRelativeToNoAdaptation = new ArrayList<>();
                    for (double result : finalResults) {
                        double newResult = result;
                        resultsRelativeToNoAdaptation.add(newResult);
                    }
                    dataset.add(resultsRelativeToNoAdaptation, "", moteEntry.getKey());
                    legend.add("Effective value");
                }
            }

        }
    private void createChartPanel(ArrayList<Mote>motes,int index) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis("Air Quality");
        CustomBoxAndWhiskerRenderer renderer = new CustomBoxAndWhiskerRenderer(legend);
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
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setLowerMargin(0.15);
        domainAxis.setUpperMargin(0.15);
        domainAxis.setCategoryMargin(0.35);
        Font font3 = new Font("Arial", Font.PLAIN, 28);
        plot.getRangeAxis().setLabelFont(font3);
        font3 = new Font("Arial", Font.PLAIN, 30);
        String title = "Cyclist : " + motes.get(index).getEUI()  + " with buffer: K=" + bufferSizeWidth + " and horizon =" + bufferSizeHeight;
        JFreeChart chart = new JFreeChart(title, plot);
        chart.getTitle().setFont(font3);
        font3 = new Font("Arial", Font.PLAIN, 20);
        chart.getLegend().setItemFont(font3);
        chartPanel = new ChartPanel(chart);
        chartPanel.setMouseWheelEnabled(true);
    }


    private void createControlPanel(HashMap<Mote,HashMap<Integer, HashMap<Integer,List<Double>>>> airQuality, HashMap<Mote,HashMap<Integer,HashMap<Integer,List<Double>>>> predictions,ArrayList<Mote> motes,int index) {
        controlPanel = new JPanel();
        controlPanel.add(new JButton(new AbstractAction("\u22b2Prev") {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (index != 0) {
                    Window window = SwingUtilities.getWindowAncestor(controlPanel);
                    window.dispose();
                    JFrame frame = new JFrame();
                    Boxplot2 boxplot = new Boxplot2(airQuality, predictions, motes, index - 1);
                    frame.add(boxplot.getChartPanel(), BorderLayout.CENTER);
                    frame.add(boxplot.getControlPanel(), BorderLayout.SOUTH);
                    frame.pack();
                    frame.setLocationRelativeTo(null);
                    frame.setVisible(true);
                }
            }
        }));
        controlPanel.add(new JButton(new AbstractAction("Next\u22b3") {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (index != motes.size() - 1) {
                    Window window = SwingUtilities.getWindowAncestor(controlPanel);
                    window.dispose();
                    JFrame frame = new JFrame();
                    Boxplot2 boxplot = new Boxplot2(airQuality, predictions, motes, index + 1);
                    frame.add(boxplot.getChartPanel(), BorderLayout.CENTER);
                    frame.add(boxplot.getControlPanel(), BorderLayout.SOUTH);
                    frame.pack();
                    frame.setLocationRelativeTo(null);
                    frame.setVisible(true);
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
                    Page page = pdfDoc.createPage(new Rectangle(chartPanel.getWidth()/2-20, chartPanel.getHeight()));
                    PDFGraphics2D g2 = page.getGraphics2D();
                    chartPanel.getChart().draw(g2, new Rectangle(0, 0,chartPanel.getWidth()/2-20,chartPanel.getHeight()));
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
