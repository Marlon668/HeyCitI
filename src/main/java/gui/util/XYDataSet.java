package gui.util;

import org.jfree.data.DomainInfo;
import org.jfree.data.Range;
import org.jfree.data.RangeInfo;
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.XYDataset;

public class XYDataSet extends AbstractXYDataset implements XYDataset, DomainInfo, RangeInfo {
    private static final int DEFAULT_SERIES_COUNT = 4;
    private static final int DEFAULT_ITEM_COUNT = 40;
    private static final double DEFAULT_RANGE = 200.0D;
    private Double[][] xValues;
    private Double[][] yValues;
    private int seriesCount;
    private int itemCount;
    private Number domainMin;
    private Number domainMax;
    private Number rangeMin;
    private Number rangeMax;
    private Range domainRange;
    private Range range;

    public XYDataSet() {
        this(4, 40);
    }

    public XYDataSet(int var1, int var2) {
        this.xValues = new Double[var1][var2];
        this.yValues = new Double[var1][var2];
        this.seriesCount = var1;
        this.itemCount = var2;
        double var3 = 1.0D / 0.0;
        double var5 = -1.0D / 0.0;
        double var7 = 1.0D / 0.0;
        double var9 = -1.0D / 0.0;

        for(int var11 = 0; var11 < var1; ++var11) {
            for(int var12 = 0; var12 < var2; ++var12) {
                double var13 = (Math.random() - 0.5D) * 200.0D;
                this.xValues[var11][var12] = new Double(var13);
                if (var13 < var3) {
                    var3 = var13;
                }

                if (var13 > var5) {
                    var5 = var13;
                }

                double var15 = (Math.random() + 0.5D) * 6.0D * var13 + var13;
                this.yValues[var11][var12] = new Double(var15);
                if (var15 < var7) {
                    var7 = var15;
                }

                if (var15 > var9) {
                    var9 = var15;
                }
            }
        }

        this.domainMin = new Double(var3);
        this.domainMax = new Double(var5);
        this.domainRange = new Range(var3, var5);
        this.rangeMin = new Double(var7);
        this.rangeMax = new Double(var9);
        this.range = new Range(var7, var9);
    }

    public Number getX(int var1, int var2) {
        return this.xValues[var1][var2];
    }

    public Number getY(int var1, int var2) {
        return this.yValues[var1][var2];
    }

    public int getSeriesCount() {
        return this.seriesCount;
    }

    public Comparable getSeriesKey(int var1) {
        return "Sample " + var1;
    }

    public int getItemCount(int var1) {
        return this.itemCount;
    }

    public double getDomainLowerBound() {
        return this.domainMin.doubleValue();
    }

    public double getDomainLowerBound(boolean var1) {
        return this.domainMin.doubleValue();
    }

    public double getDomainUpperBound() {
        return this.domainMax.doubleValue();
    }

    public double getDomainUpperBound(boolean var1) {
        return this.domainMax.doubleValue();
    }

    public Range getDomainBounds() {
        return this.domainRange;
    }

    public Range getDomainBounds(boolean var1) {
        return this.domainRange;
    }

    public Range getDomainRange() {
        return this.domainRange;
    }

    public double getRangeLowerBound() {
        return this.rangeMin.doubleValue();
    }

    public double getRangeLowerBound(boolean var1) {
        return this.rangeMin.doubleValue();
    }

    public double getRangeUpperBound() {
        return this.rangeMax.doubleValue();
    }

    public double getRangeUpperBound(boolean var1) {
        return this.rangeMax.doubleValue();
    }

    public Range getRangeBounds(boolean var1) {
        return this.range;
    }

    public Range getValueRange() {
        return this.range;
    }

    public Number getMinimumDomainValue() {
        return this.domainMin;
    }

    public Number getMaximumDomainValue() {
        return this.domainMax;
    }

    public Number getMinimumRangeValue() {
        return this.domainMin;
    }

    public Number getMaximumRangeValue() {
        return this.domainMax;
    }
}
