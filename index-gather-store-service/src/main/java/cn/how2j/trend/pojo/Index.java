package cn.how2j.trend.pojo;

import java.io.Serializable;

/**
 * @author qqlin
 * @date 2020-6-13 10:52
 */
public class Index implements Serializable {

    private static final long serialVersionUID = 3826678036589053087L;

    /**
     * 证券的代码
     */
    private String code;

    /**
     * 证券的名称
     */
    private String name;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
