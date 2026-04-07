package cn.how2j.trend.dubbo;

import cn.how2j.trend.pojo.Index;

import java.util.List;

/**
 * 指数代码服务接口 (Dubbo RPC)
 *
 * 提供指数代码的查询和管理功能
 *
 * @author qqlin
 */
public interface IndexCodesDubboService {

    /**
     * 获取所有指数代码列表
     *
     * @return 指数代码列表
     */
    List<Index> getCodes();

    /**
     * 获取单个指数代码信息
     *
     * @param code 指数代码
     * @return 指数信息
     */
    Index getIndex(String code);

    /**
     * 刷新指数代码列表
     *
     * @return 刷新后的指数列表
     */
    List<Index> fresh();

    /**
     * 移除指数代码
     *
     * @param code 指数代码
     */
    void remove(String code);

    /**
     * 存储指数代码
     *
     * @param code 指数代码
     * @return 存储后的指数信息
     */
    Index store(String code);
}
