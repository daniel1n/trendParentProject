package cn.how2j.trend.web;

import cn.how2j.trend.pojo.AnnualProfit;
import cn.how2j.trend.pojo.IndexData;
import cn.how2j.trend.pojo.Profit;
import cn.how2j.trend.pojo.Trade;
import cn.how2j.trend.service.BackTestService;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * @author qqlin
 * @date 2020-6-14 11:04
 */
@RestController
public class BackTestController {

    @Autowired
    private BackTestService backTestService;

    /**
     * 这个需求是对日期功能的增加，看上去挺简单，其实还略微复杂，需求包括如下内容
     * 1. 当获取到指数数据后，要把对应的开始日期和结束日期拿到并显示在日期控件上
     * 2. 每次切换指数代码，都会更新开始日期和结束日期。
     * 3. 日期能够选择的范围在 开始日期 到 结束日期之间， 超过了就不能选择了。
     * 4. 当选择了新的日期范围的时候，会自动获取对应的数据出来。
     * 5. 对于服务端，如果没有提供开始和结束日期，则返回所有数据。 如果提供了，则返回指定日期范围的对应数据。
     * 6. 开始日期不能大于结束日期
     *
     * @param code         证券代码
     * @param strStartDate 开始日期
     * @param strEndDate   结束日期
     * @return 可在服务器中传递的Map视图
     * @throws Exception
     */
    @GetMapping("/simulate/{code}/{ma}/{buyThreshold}/{sellThreshold}/{serviceCharge}//{startDate}/{endDate}")
    @CrossOrigin
    public Map<String, Object> backTest(@PathVariable("code") String code,
                                        @PathVariable("ma") int movingAverageDay,
                                        @PathVariable("buyThreshold") float buyRate,
                                        @PathVariable("sellThreshold") float sellRate,
                                        @PathVariable("serviceCharge") float serviceCharge,
                                        @PathVariable("startDate") String strStartDate,
                                        @PathVariable("endDate") String strEndDate) throws Exception {
        List<IndexData> allIndexDatas = backTestService.indexDataList(code);

        // 计算出开始日期和结束日期
        String indexStartDate = allIndexDatas.get(0).getDate();
        String indexEndDate = allIndexDatas.get(allIndexDatas.size() - 1).getDate();

        // 根据开始日期和结束日期获取对应日期范围的数据
        allIndexDatas = filterByDateRange(allIndexDatas, strStartDate, strEndDate);

        //计算均线所需的天数
//        int movingAverageDay = 20;

        // 卖出阈值
//        float sellRate = 0.95f;
        // 买入阈值
//        float buyRate = 1.05f;

        // 交易手续费
//        float serviceCharge = 0f;

        // 模拟数据中的相关计算
        Map<String, ?> simulateResult = backTestService.simulate(movingAverageDay, sellRate, buyRate, serviceCharge, allIndexDatas);

        List<Profit> profits = (List<Profit>) simulateResult.get("profits");
        List<Trade> trades = (List<Trade>) simulateResult.get("trades");

        // 获取区间范围内的合计年份
        float years = backTestService.getYears(allIndexDatas);

        // 计算指数投资的收益和年化收益率
        final float closePoint = allIndexDatas.get(allIndexDatas.size() - 1).getClosePoint() - allIndexDatas.get(0).getClosePoint();
        float indexIncomeTotal = closePoint / allIndexDatas.get(0).getClosePoint();
        float indexIncomeAnnual = (float) Math.pow(1 + indexIncomeTotal, 1 / years) - 1;

        // 计算趋势投资的收益和年化收益率
        final float value = profits.get(profits.size() - 1).getValue() - profits.get(0).getValue();
        float trendIncomeTotal = value / profits.get(0).getValue();
        float trendIncomeAnnual = (float) Math.pow(1 + trendIncomeTotal, 1 / years) - 1;

        // 计算盈利交易次数和盈利交易比例
        int winCount = (Integer) simulateResult.get("winCount");
        float avgWinRate = (Float) simulateResult.get("avgWinRate");

        // 计算亏损交易次数和盈利交易比例
        int lossCount = (Integer) simulateResult.get("lossCount");
        float avgLossRate = (Float) simulateResult.get("avgLossRate");

        // 计算完整时间范围内，每一年的指数投资的收益和趋势投资的收益
        List<AnnualProfit> annualProfits = (List<AnnualProfit>) simulateResult.get("annualProfits");

        Map<String, Object> result = new HashMap<>();
        result.put("indexDatas", allIndexDatas);
        result.put("indexStartDate", indexStartDate);
        result.put("indexEndDate", indexEndDate);

        result.put("profits", profits);
        result.put("trades", trades);

        result.put("years", years);
        result.put("indexIncomeTotal", indexIncomeTotal);
        result.put("indexIncomeAnnual", indexIncomeAnnual);
        result.put("trendIncomeTotal", trendIncomeTotal);
        result.put("trendIncomeAnnual", trendIncomeAnnual);

        result.put("winCount", winCount);
        result.put("lossCount", lossCount);
        result.put("avgWinRate", avgWinRate);
        result.put("avgLossRate", avgLossRate);

        result.put("annualProfits", annualProfits);

        return result;
    }

    /**
     * 根据开始日期和结束日期获取对应日期范围的数据
     *
     * @param allIndexDatas 对应的全部指数数据
     * @param strStartDate  开始日期
     * @param strEndDate    结束日期
     * @return 日期区间的所有数据
     */
    private List<IndexData> filterByDateRange(List<IndexData> allIndexDatas, String strStartDate, String strEndDate) {
        if (StrUtil.isBlankOrUndefined(strStartDate) || StrUtil.isBlankOrUndefined(strEndDate)) {
            return allIndexDatas;
        }

        List<IndexData> result = new ArrayList<>();
        Date startDate = DateUtil.parse(strStartDate);
        Date endDate = DateUtil.parse(strEndDate);

        for (IndexData indexData : allIndexDatas) {
            Date date = DateUtil.parse(indexData.getDate());
            if (date.getTime() >= startDate.getTime() && date.getTime() <= endDate.getTime()) {
                result.add(indexData);
            }
        }

        return result;
    }

}
