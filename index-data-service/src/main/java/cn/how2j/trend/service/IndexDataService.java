package cn.how2j.trend.service;

import cn.how2j.trend.pojo.IndexData;

import java.util.List;

/**
 * @author qqlin
 * @date 2020-6-14 0:01
 */
public interface IndexDataService {

    /**
     * 从redis获取数据
     *
     * @param code 证券代码
     * @return 返回所有的证券数据
     */
    List<IndexData> get(String code);
}
