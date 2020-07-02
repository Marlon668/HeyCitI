package gui.util;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import gui.util.orsonpdf.PDFDocument;
import gui.util.orsonpdf.PDFGraphics2D;
import gui.util.orsonpdf.Page;
import gui.MainGUI;
import iot.networkentity.Mote;
import org.jfree.chart.*;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import util.Pair;

/**
 * Class for representing a line graphic for visualising the utility for using an adaptation method
 * for following the best path
 */
public class LinePlot2 extends JFrame {
    public LinePlot2 (String var1, HashMap<Mote, List<Pair<Double,Double>>> visualiseRun,HashMap<Mote,List<Double>> adaptationPoints,HashMap<Mote,List<Pair<Pair<Double,Double>,Pair<Double,Double>>>> alternativeRoutes, ArrayList<Mote> motes, int index,int bufferSizeHeight,int bufferSizeWidth) {
        super(var1);
        JPanel var2 = createPanel(visualiseRun,adaptationPoints,alternativeRoutes,motes,index,bufferSizeHeight,bufferSizeWidth);
        RefineryUtilities.centerFrameOnScreen(this);
        var2.setPreferredSize(new java.awt.Dimension(1000, 500));
        this.setContentPane(var2);
    }

    public static JPanel createPanel(HashMap<Mote, List<Pair<Double,Double>>> visualiseRun,HashMap<Mote,List<Double>> adaptationPoints,HashMap<Mote,List<Pair<Pair<Double,Double>,Pair<Double,Double>>>> alternativeRoutes,ArrayList<Mote> motes,int index,int bufferSizeHeight,int bufferSizeWidth) {
        JFreeChart var0 = createChart(createDataset(visualiseRun,adaptationPoints,alternativeRoutes,motes,index),motes,index,bufferSizeHeight,bufferSizeWidth);
        ChartPanel panel = new ChartPanel(var0);
        panel.setLayout(new FlowLayout((FlowLayout.RIGHT)));
        panel.add(new JButton(new AbstractAction("\u22b2Prev") {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (index != 0) {
                    Window window = javax.swing.SwingUtilities.getWindowAncestor(panel);
                    window.dispose();
                    LinePlot2 linePlot = new LinePlot2(
                        "Mote : " + motes.get(index - 1).getEUI(), visualiseRun,adaptationPoints,alternativeRoutes, motes, index - 1,bufferSizeHeight,bufferSizeWidth);
                    linePlot.pack();
                    linePlot.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    RefineryUtilities.centerFrameOnScreen(linePlot);
                    linePlot.setVisible(true);
                }
                }
        }));
        panel.add(new JButton(new AbstractAction("Next\u22b3") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (index != motes.size() - 1) {
                    Window window = javax.swing.SwingUtilities.getWindowAncestor(panel);
                    window.dispose();
                    LinePlot2 linePlot = new LinePlot2(
                        "Cyclist : " + motes.get(index + 1).getEUI(), visualiseRun,adaptationPoints,alternativeRoutes, motes, index + 1,bufferSizeHeight,bufferSizeWidth);
                    linePlot.pack();
                    linePlot.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    RefineryUtilities.centerFrameOnScreen(linePlot);
                    linePlot.setVisible(true);
                }

            }
        }));
        panel.add(new JButton(new AbstractAction("Save\u22b3") {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Save output");
                fc.setFileFilter(new FileNameExtensionFilter("pdf output", "pdf"));

                File file = new File(MainGUI.class.getProtectionDomain().getCodeSource().getLocation().getPath());
                file = new File(file.getParent());
                fc.setCurrentDirectory(file);

                int returnVal = fc.showSaveDialog(panel);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    file = GUIUtil.getOutputFile(fc.getSelectedFile(), "pdf");
                    PDFDocument pdfDoc = new PDFDocument();
                    pdfDoc.setTitle("Line plot");
                    Page page = pdfDoc.createPage(new Rectangle(panel.getWidth(), panel.getHeight()));
                    PDFGraphics2D g2 = page.getGraphics2D();
                    var0.draw(g2, new Rectangle(0, 0, panel.getWidth(), panel.getHeight()));
                    pdfDoc.writeToFile(file);
                }

            }
        }));
        panel.setMouseWheelEnabled(true);
        return panel;
    }

    /**
     * Translate data in a dataset to show the utility of using an adaptation method
     * @param visualiseRun hashmap containing information what the expected air-quality would be if we
     *                     don't change the path
     * @param adaptationPoints hashmap containing information on which distance we had done an
     *                         adaptation
     * @param motes arraylist containing every mote of the configuration
     * @param index of which mote must we show a window containing boxplots to respresent its data
     */
    private static XYDataset createDataset(HashMap<Mote, List<Pair<Double,Double>>> visualiseRun,HashMap<Mote,List<Double>>adaptationPoints,HashMap<Mote,List<Pair<Pair<Double,Double>,Pair<Double,Double>>>> alternativeRoutes, ArrayList<Mote> motes, int index) {
            Mote mote = motes.get(index);
            XYSeriesCollection dataset = new XYSeriesCollection();
            List<Pair<Double,Double>> resultsMote = visualiseRun.get(mote);
            XYSeries series1 = new XYSeries("Waypoints");
            for (Pair<Double,Double> moteEntry2 : resultsMote) {
                series1.add(moteEntry2.getRight(),moteEntry2.getLeft());
            }
            dataset.addSeries(series1);
            List<Double>adaptation= adaptationPoints.get(mote);
            int i = 1;
            for(double adaptationPoint : adaptation){
                series1 = new XYSeries("Adaptation " + i);
                for(Pair<Pair<Double,Double>,Pair<Double,Double>> adaptationPoint2 : alternativeRoutes.get(mote)) {
                    if(adaptationPoint2.getRight().getRight()==adaptationPoint){
                        series1.add(adaptationPoint2.getLeft().getRight(),adaptationPoint2.getLeft().getLeft());
                        series1.add(adaptationPoint2.getRight().getRight(),adaptationPoint2.getRight().getLeft());
                    }
                }
                series1.add(adaptationPoint,-5.0);
                series1.add(adaptationPoint,100.0);
                dataset.addSeries(series1);
                i++;
            }
            return dataset;

    }

    private static JFreeChart createChart(XYDataset var0, ArrayList<Mote> motes, int index,int bufferSizeHeight,int bufferSizeWidth) {
        String title = "Cyclist : " + motes.get(index).getEUI()  + " with buffer: K=" + bufferSizeWidth + " and horizon =" + bufferSizeHeight;
        JFreeChart var1 = ChartFactory.createXYLineChart(title, "Distance (meter)", "Air Quality", var0, PlotOrientation.VERTICAL, true, true, false);
        XYPlot var2 = (XYPlot) var1.getPlot();
        Font font3 = new Font("Arial", Font.PLAIN, 26);
        var2.getDomainAxis().setLabelFont(font3);
        font3 = new Font("Arial", Font.PLAIN, 30);
        var1.getTitle().setFont(font3);
        font3 = new Font("Arial", Font.PLAIN, 26);
        var2.getRangeAxis().setLabelFont(font3);
        var2.setDomainPannable(true);
        var2.setRangePannable(true);
        var2.setDomainZeroBaselineVisible(true);
        var2.setRangeZeroBaselineVisible(true);
        XYLineAndShapeRenderer var3 = (XYLineAndShapeRenderer) var2.getRenderer();
        var3.setBaseShapesVisible(true);
        var3.setBaseShapesFilled(true);
        LegendTitle legend = var1.getLegend();
        Font labelFont = new Font("Arial", Font.BOLD, 15);
        legend.setItemFont(labelFont);
        LegendItemCollection legend2 = var3.getLegendItems();
        Iterator iterator=legend2.iterator();
        while(iterator.hasNext()){
            LegendItem item=(LegendItem)iterator.next();
                if(item.getLabel().equals("Waypoints"))
                iterator.remove();
        }
        var2.setFixedLegendItems(legend2);
        legend.setWidth(500.0);
        legend.setHeight(500.0);
        var2.getRangeAxis().setRange(0.0,5.0);
        var3.setDrawOutlines(true);
        NumberAxis var4 = (NumberAxis) var2.getRangeAxis();
        var4.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        return var1;
    }
}

