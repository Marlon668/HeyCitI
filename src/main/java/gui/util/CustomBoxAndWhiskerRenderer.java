package gui.util;

import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.Outlier;
import org.jfree.chart.renderer.OutlierList;
import org.jfree.chart.renderer.OutlierListCollection;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.chart.renderer.category.CategoryItemRendererState;
import org.jfree.data.general.Dataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import util.Pair;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

// based on code at https://stackoverflow.com/questions/33334206/how-to-remove-outlierssmall-circle-from-box-and-whisker-chart-in-jfreechart/53768672

public class CustomBoxAndWhiskerRenderer extends BoxAndWhiskerRenderer {

    private final List<Color> clut = new ArrayList<Color>();
    private List<Pair<Integer,Integer>> data;
    private int index;
    private List<String> legend;

    public CustomBoxAndWhiskerRenderer(List<String> legend){
        super();
        initClut();
        this.legend = legend;
        data = new ArrayList<>();
    }

    @Override
    public Paint getItemPaint(int row, int col) {
        if(!data.contains(new Pair<>(row,col))){
            data.add(new Pair<>(row,col));
            return clut.get(data.indexOf(new Pair<>(row,col)));
        }
        else{
            return clut.get(data.indexOf(new Pair<>(row,col)));
        }
    }

    private void initClut() {
        clut.add(Color.red);
        clut.add(Color.blue);
        clut.add(Color.green);
        clut.add(Color.yellow);
        clut.add(Color.orange);
        clut.add(Color.cyan);
        clut.add(Color.magenta);
    }

    private Double outlierRadius;

    public Double getOutlierRadius() {
        return outlierRadius;
    }

    public void setOutlierRadius(Double outlierRadius) {
        this.outlierRadius = outlierRadius;
    }

    @Override
    public void drawVerticalItem(Graphics2D g2, CategoryItemRendererState state,
                                 Rectangle2D dataArea, CategoryPlot plot, CategoryAxis domainAxis,
                                 ValueAxis rangeAxis, CategoryDataset dataset, int row, int column) {


        BoxAndWhiskerCategoryDataset bawDataset
            = (BoxAndWhiskerCategoryDataset) dataset;

        double categoryEnd = domainAxis.getCategoryEnd(column,
            getColumnCount(), dataArea, plot.getDomainAxisEdge());
        double categoryStart = domainAxis.getCategoryStart(column,
            getColumnCount(), dataArea, plot.getDomainAxisEdge());
        double categoryWidth = categoryEnd - categoryStart;

        double xx = categoryStart;
        int seriesCount = getRowCount();
        int categoryCount = getColumnCount();

        if (seriesCount > 1) {
            double seriesGap = dataArea.getWidth() * getItemMargin()
                / (categoryCount * (seriesCount - 1));
            double usedWidth = (state.getBarWidth() * seriesCount)
                + (seriesGap * (seriesCount - 1));
            // offset the start of the boxes if the total width used is smaller
            // than the category width
            double offset = (categoryWidth - usedWidth) / 2;
            xx = xx + offset + (row * (state.getBarWidth() + seriesGap));
        }
        else {
            // offset the start of the box if the box width is smaller than the
            // category width
            double offset = (categoryWidth - state.getBarWidth()) / 2;
            xx = xx + offset;
        }

        double yyAverage;
        double yyOutlier;

        Paint itemPaint = getItemPaint(row, column);
        g2.setPaint(itemPaint);
        Stroke s = getItemStroke(row, column);
        g2.setStroke(s);

        double aRadius = 0;                 // average radius

        RectangleEdge location = plot.getRangeAxisEdge();

        Number yQ1 = bawDataset.getQ1Value(row, column);
        Number yQ3 = bawDataset.getQ3Value(row, column);
        Number yMax = bawDataset.getMaxRegularValue(row, column);
        Number yMin = bawDataset.getMinRegularValue(row, column);
        Shape box = null;
        if (yQ1 != null && yQ3 != null && yMax != null && yMin != null) {

            double yyQ1 = rangeAxis.valueToJava2D(yQ1.doubleValue(), dataArea,
                location);
            double yyQ3 = rangeAxis.valueToJava2D(yQ3.doubleValue(), dataArea,
                location);
            double yyMax = rangeAxis.valueToJava2D(yMax.doubleValue(),
                dataArea, location);
            double yyMin = rangeAxis.valueToJava2D(yMin.doubleValue(),
                dataArea, location);
            double xxmid = xx + state.getBarWidth() / 2.0;
            double halfW = (state.getBarWidth() / 2.0) * getWhiskerWidth();

            // draw the body...
            box = new Rectangle2D.Double(xx, Math.min(yyQ1, yyQ3),
                state.getBarWidth(), Math.abs(yyQ1 - yyQ3));
            if (getFillBox()) {
                g2.fill(box);
            }

            Paint outlinePaint = getItemOutlinePaint(row, column);
            if (getUseOutlinePaintForWhiskers()) {
                g2.setPaint(outlinePaint);
            }
            // draw the upper shadow...
            g2.draw(new Line2D.Double(xxmid, yyMax, xxmid, yyQ3));
            g2.draw(new Line2D.Double(xxmid - halfW, yyMax, xxmid + halfW, yyMax));

            // draw the lower shadow...
            g2.draw(new Line2D.Double(xxmid, yyMin, xxmid, yyQ1));
            g2.draw(new Line2D.Double(xxmid - halfW, yyMin, xxmid + halfW, yyMin));

            g2.setStroke(getItemOutlineStroke(row, column));
            g2.setPaint(outlinePaint);
            g2.draw(box);
        }

        g2.setPaint(getArtifactPaint());

        // draw mean - SPECIAL AIMS REQUIREMENT...
        if (isMeanVisible()) {
            Number yMean = bawDataset.getMeanValue(row, column);
            if (yMean != null) {
                yyAverage = rangeAxis.valueToJava2D(yMean.doubleValue(),
                    dataArea, location);
                aRadius = state.getBarWidth() / 10;
                // here we check that the average marker will in fact be
                // visible before drawing it...
                if ((yyAverage > (dataArea.getMinY() - aRadius))
                    && (yyAverage < (dataArea.getMaxY() + aRadius))) {
                    Ellipse2D.Double avgEllipse = new Ellipse2D.Double(
                        xx + state.getBarWidth()/2 , yyAverage , aRadius,
                        aRadius);
                    g2.fill(avgEllipse);
                    g2.draw(avgEllipse);
                }
            }
        }

        // draw median...
        if (isMedianVisible()) {
            Number yMedian = bawDataset.getMedianValue(row, column);
            if (yMedian != null) {
                double yyMedian = rangeAxis.valueToJava2D(
                    yMedian.doubleValue(), dataArea, location);
                g2.draw(new Line2D.Double(xx, yyMedian,
                    xx + state.getBarWidth(), yyMedian));
            }
        }

        // draw yOutliers...
        double maxAxisValue = rangeAxis.valueToJava2D(
            rangeAxis.getUpperBound(), dataArea, location) + aRadius;
        double minAxisValue = rangeAxis.valueToJava2D(
            rangeAxis.getLowerBound(), dataArea, location) - aRadius;

        g2.setPaint(itemPaint);

        // draw outliers
        double oRadius = outlierRadius == null? state.getBarWidth()/3: outlierRadius;    // outlier radius
        java.util.List outliers = new ArrayList();
        OutlierListCollection outlierListCollection
            = new OutlierListCollection();

        // From outlier array sort out which are outliers and put these into a
        // list If there are any farouts, set the flag on the
        // OutlierListCollection
        List yOutliers = bawDataset.getOutliers(row, column);
        if (yOutliers != null) {
            for (int i = 0; i < yOutliers.size(); i++) {
                double outlier = ((Number) yOutliers.get(i)).doubleValue();
                Number minOutlier = bawDataset.getMinOutlier(row, column);
                Number maxOutlier = bawDataset.getMaxOutlier(row, column);
                Number minRegular = bawDataset.getMinRegularValue(row, column);
                Number maxRegular = bawDataset.getMaxRegularValue(row, column);
                if (outlier > maxRegular.doubleValue()) {
                    yyOutlier = rangeAxis.valueToJava2D(outlier, dataArea,
                        location);
                    outliers.add(new Outlier(xx + state.getBarWidth() / 2.0,
                        yyOutlier, oRadius));
                }
                else if (outlier < minRegular.doubleValue()) {
                    yyOutlier = rangeAxis.valueToJava2D(outlier, dataArea,
                        location);
                    outliers.add(new Outlier(xx + state.getBarWidth() / 2.0,
                        yyOutlier, oRadius));
                }
                Collections.sort(outliers);
            }

            // Process outliers. Each outlier is either added to the
            // appropriate outlier list or a new outlier list is made
            for (Iterator iterator = outliers.iterator(); iterator.hasNext();) {
                Outlier outlier = (Outlier) iterator.next();
                outlierListCollection.add(outlier);
            }

            for (Iterator iterator = outlierListCollection.iterator();
                 iterator.hasNext();) {
                OutlierList list = (OutlierList) iterator.next();
                Outlier outlier = list.getAveragedOutlier();
                Point2D point = outlier.getPoint();

                if (list.isMultiple()) {
                    drawMultipleEllipse(point, state.getBarWidth(), oRadius,
                        g2);
                }
                else {
                    drawEllipse(point, oRadius, g2);
                }
            }
        }
        // collect entity and tool tip information...
        if (state.getInfo() != null && box != null) {
            EntityCollection entities = state.getEntityCollection();
            if (entities != null) {
                addItemEntity(entities, dataset, row, column, box);
            }
        }

    }

    @Override
    protected void addItemEntity(EntityCollection entities, CategoryDataset dataset, int row, int column, Shape hotspot) {
        super.addItemEntity(entities, dataset, row, column, hotspot);
    }

    /**
     * Draws two dots to represent the average value of more than one outlier.
     *
     * @param point  the location
     * @param boxWidth  the box width.
     * @param oRadius  the radius.
     * @param g2  the graphics device.
     */
    private void drawMultipleEllipse(Point2D point, double boxWidth,
                                     double oRadius, Graphics2D g2)  {

        Ellipse2D dot1 = new Ellipse2D.Double(point.getX() - (boxWidth / 2)
            + oRadius, point.getY(), oRadius, oRadius);
        Ellipse2D dot2 = new Ellipse2D.Double(point.getX() + (boxWidth / 2),
            point.getY(), oRadius, oRadius);
        g2.draw(dot1);
        g2.draw(dot2);
    }


    /**
     * Draws a dot to represent an outlier.
     *
     * @param point  the location.
     * @param oRadius  the radius.
     * @param g2  the graphics device.
     */
    private void drawEllipse(Point2D point, double oRadius, Graphics2D g2) {
        Ellipse2D dot = new Ellipse2D.Double(point.getX() + oRadius / 2,
            point.getY(), oRadius, oRadius);
        g2.draw(dot);
    }



}
