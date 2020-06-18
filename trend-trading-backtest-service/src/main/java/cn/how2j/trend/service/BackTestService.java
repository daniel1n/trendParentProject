package cn.how2j.trend.service;

import cn.how2j.trend.pojo.AnnualProfit;
import cn.how2j.trend.pojo.IndexData;
import cn.how2j.trend.pojo.Profit;

import java.util.List;
import java.util.Map;

/**
 * @author qqlin
 * @date 2020-6-14 10:58
 */

public interface BackTestService {

    /**
     * 获取指数数据
     *
     * @param code 证券代码
     * @return 返回指数代码对应的所有数据
     */
    List<IndexData> indexDataList(String code);

    /**
     * 存放模拟数据中的相关计算数值
     *
     * @param movingAverageDay MA 均线：MA 即 moving average, 移动均线的意思。
     *                         比如MA20就表示20日均线，取最近20天的值的平均数。
     *                         如果当前的收盘点高于这个均线一定的比例，那么我们认为上涨的趋势可能就来了，就可以买了。
     * @param sellRate         出售阈值：如果当前的收盘点.
     *                         比起最近的20个交易日里的最高的点，跌了 5%或者 10%了，
     *                         那么我们认为下跌趋势可能就来了，就可以卖了。
     * @param buyRate          买入阈值：如果当前的收盘点.
     *                         比起最近的20个交易日里的最高的点，涨了 5%或者 10%了，
     *                         那么我们认为下跌趋势可能就来了，就可以买了。
     * @param serviceCharge    服务手续费
     * @param indexDatas       指数的数据，用于计算
     * @return 存入Map中的相关计算数值
     */
    Map<String, Object> simulate(int movingAverageDay, float sellRate, float buyRate,
                                 float serviceCharge, List<IndexData> indexDatas);

    /**
     * 用于计算当前的时间范围是多少年。
     *
     * @param allIndexDatas 该指数的所有值
     * @return 总共的年份
     */
    float getYears(List<IndexData> allIndexDatas);

    /**
     * 获取某个日期里的年份
     *
     * @param date 日期
     * @return 返回哪一年
     */
    int getYear(String date);

    /**
     * 计算某一年的指数投资收益
     *
     * @param year       某一年
     * @param indexdatas 所有的指数数据
     * @return 返回指数投资收益
     */
    float getIndexIncome(int year, List<IndexData> indexdatas);

    /**
     * 计算某一年的趋势投资收益
     *
     * @param year    某一年
     * @param profits 所有的收益
     * @return 返回趋势投资的收益
     */
    float getTrendIncome(int year, List<Profit> profits);

    /**
     * 计算完整时间范围内，每一年的指数投资的收益和趋势投资的收益
     *
     * @param indexDatas 所有的指数数据
     * @param profits    所有的收益
     * @return 某一年的收益
     */
    List<AnnualProfit> calculateAnnualProfits(List<IndexData> indexDatas, List<Profit> profits);

}
