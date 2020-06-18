package cn.how2j.trend.pojo;

import java.io.Serializable;

/**
 * @author qqlin
 * @date 2020-6-15 22:08
 */
public class AnnualProfit implements Serializable {

    private static final long serialVersionUID = -7418682840805152436L;
    /**
     * 当期的年份
     */
    private int year;

    /**
     * 指数投资的收益
     */
    private float indexIncome;

    /**
     * 趋势投资的收益
     */
    private float trendIncome;

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public float getIndexIncome() {
        return indexIncome;
    }

    public void setIndexIncome(float indexIncome) {
        this.indexIncome = indexIncome;
    }

    public float getTrendIncome() {
        return trendIncome;
    }

    public void setTrendIncome(float trendIncome) {
        this.trendIncome = trendIncome;
    }

    @Override
    public String toString() {
        return "AnnualProfit{" +
                "year=" + year +
                ", indexIncome=" + indexIncome +
                ", trendIncome=" + trendIncome +
                '}';
    }
}
