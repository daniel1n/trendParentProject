package cn.how2j.trend.pojo;

import java.io.Serializable;

/**
 * @author qqlin
 * @date 2020-6-15 17:17
 */
public class Trade implements Serializable {

    private static final long serialVersionUID = 3916642514594342236L;

    /**
     * 买入阈值
     */
    private String buyDate;

    /**
     * 卖出阈值
     */
    private String sellDate;

    /**
     * 买入收盘点
     */
    private float buyClosePoint;

    /**
     * 卖出收盘点
     */
    private float sellClosePoint;

    /**
     * 收益率
     */
    private float rate;

    public String getBuyDate() {
        return buyDate;
    }

    public void setBuyDate(String buyDate) {
        this.buyDate = buyDate;
    }

    public String getSellDate() {
        return sellDate;
    }

    public void setSellDate(String sellDate) {
        this.sellDate = sellDate;
    }

    public float getBuyClosePoint() {
        return buyClosePoint;
    }

    public void setBuyClosePoint(float buyClosePoint) {
        this.buyClosePoint = buyClosePoint;
    }

    public float getSellClosePoint() {
        return sellClosePoint;
    }

    public void setSellClosePoint(float sellClosePoint) {
        this.sellClosePoint = sellClosePoint;
    }

    public float getRate() {
        return rate;
    }

    public void setRate(float rate) {
        this.rate = rate;
    }
}
