package cn.how2j.trend.service;

import cn.how2j.trend.pojo.Index;

import java.util.List;

/**
 * @author qqlin
 * @date 2020-6-13 23:20
 */
public interface IndexService {

    /**
     * 直接从redis获取数据
     * 如果没有数据，则会返回 “无效指数代码 ”。
     *
     * @return 返回Index的所有值
     */
    List<Index> getIndexes();
}
