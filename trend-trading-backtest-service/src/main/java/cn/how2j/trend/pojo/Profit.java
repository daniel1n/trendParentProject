package cn.how2j.trend.pojo;

import java.io.Serializable;

/**
 * @author qqlin
 * @date 2020-6-15 14:19
 */
public class Profit implements Serializable {

    private static final long serialVersionUID = 2787659230368197107L;

    /**
     * 收益日期
     */
    private String date;

    /**
     * 收益的值
     */
    private float value;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }
}
