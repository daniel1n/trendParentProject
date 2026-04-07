package cn.how2j.trend.dubbo;

import cn.how2j.trend.pojo.Index;
import cn.how2j.trend.pojo.IndexData;

import java.util.List;

/**
 * 第三方指数数据服务接口 (Dubbo RPC)
 *
 * 提供第三方数据源的指数数据查询
 *
 * @author qqlin
 */
public interface ThirdPartIndexDataDubboService {

    /**
     * 获取所有指数代码
     *
     * @return 指数代码列表
     */
    List<Index> getCodes();

    /**
     * 获取指定指数的详细数据
     *
     * @param code 指数代码
     * @return 指数数据列表
     */
    List<IndexData> getIndexData(String code);

    /**
     * 检查服务是否可用
     *
     * @return true 如果服务可用
     */
    boolean isAvailable();
}
