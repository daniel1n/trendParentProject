package cn.how2j.trend.pojo;

import java.io.Serializable;

/**
 * @author qqlin
 * @date 2020-6-14 10:52
 */
public class IndexData implements Serializable {

    private static final long serialVersionUID = -9511000068712106L;

    /**
     * 数据的日期
     */
    private String date;

    /**
     * 收盘点
     */
    private float closePoint;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public float getClosePoint() {
        return closePoint;
    }

    public void setClosePoint(float closePoint) {
        this.closePoint = closePoint;
    }
}
