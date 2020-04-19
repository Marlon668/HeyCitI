package gui.util;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.orsonpdf.PDFDocument;
import com.orsonpdf.PDFGraphics2D;
import com.orsonpdf.Page;
import gui.MainGUI;
import iot.networkentity.Mote;
import org.jetbrains.annotations.NotNull;
import org.jfree.chart.*;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.text.TextUtilities;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;
import util.Pair;

/**
 * Class for representing a line graphic for visualising the utility for using an adaptation method
 * for following the best path
 */
public class LinePlot2 extends JFrame {
    public LinePlot2 (String var1, HashMap<Mote, List<Pair<Double,Double>>> visualiseRun,HashMap<Mote,List<Double>> adaptationPoints,HashMap<Mote,List<Pair<Pair<Double,Double>,Pair<Double,Double>>>> alternativeRoutes, ArrayList<Mote> motes, int index) {
        super(var1);
        JPanel var2 = createPanel(visualiseRun,adaptationPoints,alternativeRoutes,motes,index);
        RefineryUtilities.centerFrameOnScreen(this);
        var2.setPreferredSize(new java.awt.Dimension(1000, 500));
        this.setContentPane(var2);
    }

    public static JPanel createPanel(HashMap<Mote, List<Pair<Double,Double>>> visualiseRun,HashMap<Mote,List<Double>> adaptationPoints,HashMap<Mote,List<Pair<Pair<Double,Double>,Pair<Double,Double>>>> alternativeRoutes,ArrayList<Mote> motes,int index) {
        JFreeChart var0 = createChart(createDataset(visualiseRun,adaptationPoints,alternativeRoutes,motes,index),motes,index);
        ChartPanel panel = new ChartPanel(var0);
        panel.setLayout(new FlowLayout((FlowLayout.RIGHT)));
        panel.add(new JButton(new AbstractAction("\u22b2Prev") {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (index != 0) {
                    Window window = javax.swing.SwingUtilities.getWindowAncestor(panel);
                    window.dispose();
                    LinePlot2 linePlot = new LinePlot2(
                        "Mote : " + motes.get(index - 1).getEUI(), visualiseRun,adaptationPoints,alternativeRoutes, motes, index - 1);
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
                        "Mote : " + motes.get(index + 1).getEUI(), visualiseRun,adaptationPoints,alternativeRoutes, motes, index + 1);
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
            XYSeries series1 = new XYSeries("WayPoints");
            for (Pair<Double,Double> moteEntry2 : resultsMote) {
                series1.add(moteEntry2.getRight(),moteEntry2.getLeft());
            }
            dataset.addSeries(series1);
            List<Double>adaptation= adaptationPoints.get(mote);
            int i = 2;
            for(double adaptationPoint : adaptation){
                series1 = new XYSeries("Adaptation " + i);
                for(Pair<Pair<Double,Double>,Pair<Double,Double>> adaptationPoint2 : alternativeRoutes.get(mote)) {
                    if(adaptationPoint2.getRight().getRight()==adaptationPoint){
                        System.out.println("ok");
                        series1.add(adaptationPoint2.getLeft().getRight(),adaptationPoint2.getLeft().getLeft());
                        series1.add(adaptationPoint2.getRight().getRight(),adaptationPoint2.getRight().getLeft());
                    }
                }
                series1.add(adaptationPoint,-5.0);
                series1.add(adaptationPoint,8.0);
                dataset.addSeries(series1);
                i++;
            }
            return dataset;

    }


    private static JFreeChart createChart(XYDataset var0, ArrayList<Mote> motes, int index) {
        JFreeChart var1 = ChartFactory.createXYLineChart("Mote : " + motes.get(index).getEUI(), "Distance (meter)", "Air Quality", var0, PlotOrientation.VERTICAL, true, true, false);
        XYPlot var2 = (XYPlot) var1.getPlot();
        Font font3 = new Font("Arial", Font.PLAIN, 18);
        var2.getDomainAxis().setLabelFont(font3);
        font3 = new Font("Arial", Font.PLAIN, 25);
        var1.getTitle().setFont(font3);
        font3 = new Font("Arial", Font.PLAIN, 18);
        var2.getRangeAxis().setLabelFont(font3);
        var2.setDomainPannable(true);
        var2.setRangePannable(true);
        var2.setDomainZeroBaselineVisible(true);
        var2.setRangeZeroBaselineVisible(true);
        XYLineAndShapeRenderer var3 = (XYLineAndShapeRenderer) var2.getRenderer();
        var3.setBaseShapesVisible(true);
        var3.setBaseShapesFilled(true);
        LegendTitle legend = var1.getLegend();
        Font labelFont = new Font("Arial", Font.BOLD, 16);
        legend.setItemFont(labelFont);
        var2.getRangeAxis().setRange(0.0,5.0);
        XYTextAnnotation annotation = new XYTextAnnotation("Air quality improvement compared to shortest path: 86,5%", 2220, 4);
        XYTextAnnotation annotatio2 = new XYTextAnnotation("Extra distance travelled compared to shortest path: 1917 meters", 2300, 4.3);
        var1.addSubtitle(new TextTitle("Height: 2 , Width: 2",font3));
        annotation.setFont(new Font("Arial", Font.BOLD, 20));
        annotatio2.setFont(new Font("Arial", Font.BOLD, 20));
        var2.addAnnotation(annotation);
        var2.addAnnotation(annotatio2);
        var3.setDrawOutlines(true);
        NumberAxis var4 = (NumberAxis) var2.getRangeAxis();
        var4.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        return var1;
    }
}

