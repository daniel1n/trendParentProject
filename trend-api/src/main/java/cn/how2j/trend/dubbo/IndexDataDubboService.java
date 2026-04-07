package cn.how2j.trend.dubbo;

import cn.how2j.trend.pojo.IndexData;

import java.util.List;

/**
 * 指数数据服务接口 (Dubbo RPC)
 *
 * 提供指数数据的查询和管理功能
 *
 * @author qqlin
 */
public interface IndexDataDubboService {

    /**
     * 获取指定指数的数据
     *
     * @param code 指数代码
     * @return 指数数据列表
     */
    List<IndexData> get(String code);

    /**
     * 刷新指定指数的数据
     *
     * @param code 指数代码
     * @return 刷新后的数据列表
     */
    List<IndexData> fresh(String code);

    /**
     * 从第三方获取指数数据
     *
     * @param code 指数代码
     * @return 指数数据列表
     */
    List<IndexData> fetchFromThirdPart(String code);

    /**
     * 移除指定指数的数据
     *
     * @param code 指数代码
     */
    void remove(String code);

    /**
     * 存储指定指数的数据
     *
     * @param code 指数代码
     * @return 存储后的数据列表
     */
    List<IndexData> store(String code);
}
