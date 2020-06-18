package cn.how2j.trend.service;

import cn.how2j.trend.pojo.Index;

import java.util.List;
import java.util.Map;

/**
 * 指数代码的采集和存储
 *
 * @author qqlin
 * @date 2020-6-13 10:59
 */
public interface IndexService {

    /**
     * 刷新数据。 刷新的思路就是：
     * 先运行 fetch_indexes_from_third_part 来获取数据
     * 删除数据
     * 保存数据
     *
     * @return 刷新后的数据
     */
    List<Index> fresh();

    /**
     * 清空数据
     */
    void remove();

    /**
     * 存入数据
     *
     * @return 存入之后的数据
     */
    List<Index> store();

    /**
     * 获取数据，这个就是专门用来从 redis 中获取数据
     *
     * @return 获得的数据
     */
    List<Index> get();

    /**
     * 刷新第三方的数据，并存入本地的Redis
     *
     * @return Index类型的数据
     */
    List<Index> fetchIndexesFromThirdPart();

    /**
     * 断路器的方法
     * 如果fetch_indexes_from_third_part获取失败了，
     * 就自动调用 third_part_not_connected 并返回
     *
     * @return 断路器的数据
     */
    List<Index> thirdPartNotConnected();

    /**
     * 从第三方获取出来的内容是Map类型
     * 需要转换为 Index类型
     *
     * @param temp 获取到的Map类型的数据
     * @return 返回Index类型的数据
     */
    List<Index> map2Index(List<Map> temp);
}
