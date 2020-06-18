package cn.how2j.trend.service.impl;

import cn.how2j.trend.client.IndexDataClient;
import cn.how2j.trend.pojo.AnnualProfit;
import cn.how2j.trend.pojo.IndexData;
import cn.how2j.trend.pojo.Profit;
import cn.how2j.trend.pojo.Trade;
import cn.how2j.trend.service.BackTestService;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author qqlin
 * @date 2020-6-14 10:59
 */
@Service
public class BackTestServiceImpl implements BackTestService {

    @Autowired
    private IndexDataClient indexDataClient;

    @Override
    public List<IndexData> indexDataList(String code) {
        List<IndexData> result = indexDataClient.getIndexData(code);
        Collections.reverse(result);

//        for (IndexData indexData : result) {
//            System.out.println(indexData.getDate());
//        }

        return result;
    }

    @Override
    public Map<String, Object> simulate(int movingAverageDay, float sellRate, float buyRate,
                                        float serviceCharge, List<IndexData> indexDatas) {
        List<Profit> profits = new ArrayList<>();
        List<Trade> trades = new ArrayList<>();

        // 初始现金
        float initCash = 1000;
        // 现金
        float cash = initCash;
        // 份额
        float share = 0;
        // 份值
        float value = 0;

        //盈利交易次数
        int winCount = 0;
        float totalWinRate = 0;
        float avgWinRate = 0;

        //亏损交易次数
        int lossCount = 0;
        float totalLossRate = 0;
        float avgLossRate = 0;

        // 初始收盘点
        float init = 0;
        if (!indexDatas.isEmpty()) {
            init = indexDatas.get(0).getClosePoint();
        }

        for (int i = 0; i < indexDatas.size(); i++) {
            IndexData indexData = indexDatas.get(i);
            float closePoint = indexData.getClosePoint();
            float avg = getMA(i, movingAverageDay, indexDatas);
            float max = getMax(i, movingAverageDay, indexDatas);

            float increase_rate = closePoint / avg;
            float decrease_rate = closePoint / max;

            if (avg != 0) {
                // buy 超过了均线
                if (increase_rate > buyRate) {
                    // 如果没买
                    if (0 == share) {
                        share = cash / closePoint;
                        cash = 0;

                        Trade trade = new Trade();
                        trade.setBuyDate(indexData.getDate());
                        trade.setBuyClosePoint(indexData.getClosePoint());
                        trade.setSellDate("n/a");
                        trade.setSellClosePoint(0);
                        trades.add(trade);
                    }
                }
                //sell 低于了卖点
                else if (decrease_rate < sellRate) {
                    // 如果没卖
                    if (0 != share) {
                        cash = closePoint * share * (1 - serviceCharge);
                        share = 0;

                        Trade trade = trades.get(trades.size() - 1);
                        trade.setSellDate(indexData.getDate());
                        trade.setSellClosePoint(indexData.getClosePoint());

                        float rate = cash / initCash;
                        trade.setRate(rate);

                        if (trade.getSellClosePoint() - trade.getBuyClosePoint() > 0) {
                            totalWinRate += (trade.getSellClosePoint() - trade.getBuyClosePoint()) / trade.getBuyClosePoint();
                            winCount++;
                        } else {
                            totalLossRate += (trade.getSellClosePoint() - trade.getBuyClosePoint()) / trade.getBuyClosePoint();
                            lossCount++;
                        }
                    }
                }
                // do nothing
                else {

                }
            }

            if (share != 0) {
                value = closePoint * share;
            } else {
                value = cash;
            }
            float rate = value / initCash;

            Profit profit = new Profit();
            profit.setDate(indexData.getDate());
            profit.setValue(rate * init);

//            System.out.println("profit.value:" + profit.getValue());
            profits.add(profit);
        }

        avgWinRate = totalWinRate / winCount;
        avgLossRate = totalLossRate / lossCount;

        List<AnnualProfit> annualProfits = calculateAnnualProfits(indexDatas, profits);

        Map<String, Object> map = new HashMap<>();
        map.put("profits", profits);
        map.put("trades", trades);

        map.put("winCount", winCount);
        map.put("lossCount", lossCount);
        map.put("avgWinRate", avgWinRate);
        map.put("avgLossRate", avgLossRate);

        map.put("annualProfits", annualProfits);

        return map;
    }

    private static float getMax(int i, int day, List<IndexData> indexDatas) {
        int start = i - 1 - day;

        if (start < 0) {
            start = 0;
        }

        int now = i - 1;

        if (start < 0) {
            return 0;
        }

        float max = 0;
        for (int j = start; j < now; j++) {
            IndexData bean = indexDatas.get(j);
            if (bean.getClosePoint() > max) {
                max = bean.getClosePoint();
            }
        }
        return max;
    }

    private static float getMA(int i, int day, List<IndexData> indexDatas) {
        int start = i - 1 - day;
        int now = i - 1;

        if (start < 0) {
            return 0;
        }

        float sum = 0;
        float avg = 0;
        for (int j = start; j < now; j++) {
            IndexData bean = indexDatas.get(j);
            sum += bean.getClosePoint();
        }
        avg = sum / (now - start);
        return avg;
    }

    @Override
    public float getYears(List<IndexData> allIndexDatas) {
        float years;
        String sDateStart = allIndexDatas.get(0).getDate();
        String sDateEnd = allIndexDatas.get(allIndexDatas.size() - 1).getDate();

        Date dateStart = DateUtil.parse(sDateStart);
        Date dateEnd = DateUtil.parse(sDateEnd);

        long days = DateUtil.between(dateStart, dateEnd, DateUnit.DAY);
        years = days / 365f;
        return years;
    }

    @Override
    public int getYear(String date) {
        String strYear = StrUtil.subBefore(date, "-", false);
        return Convert.toInt(strYear);
    }

    @Override
    public float getIndexIncome(int year, List<IndexData> indexdatas) {
        IndexData firstIndexData = null;
        IndexData lastIndexData = null;

        for (IndexData indexData : indexdatas) {
            String strDate = indexData.getDate();
            int currentYear = getYear(strDate);

            if (currentYear == year) {
                if (null == firstIndexData) {
                    firstIndexData = indexData;
                } else {
                    lastIndexData = indexData;
                }
            }
        }
        return (lastIndexData.getClosePoint() - firstIndexData.getClosePoint()) / firstIndexData.getClosePoint();
    }

    @Override
    public float getTrendIncome(int year, List<Profit> profits) {
        Profit firstProfit = null;
        Profit lastProfit = null;

        for (Profit profit : profits) {
            String strDate = profit.getDate();
            int currentYear = getYear(strDate);

            if (currentYear == year) {
                if (null == firstProfit) {
                    firstProfit = profit;
                } else {
                    lastProfit = profit;
                }
            }
        }
        return (lastProfit.getValue() - firstProfit.getValue()) / firstProfit.getValue();
    }

    @Override
    public List<AnnualProfit> calculateAnnualProfits(List<IndexData> indexDatas, List<Profit> profits) {
        List<AnnualProfit> result = new ArrayList<>();

        String strStartDate = indexDatas.get(0).getDate();
        String strEndDate = indexDatas.get(indexDatas.size() - 1).getDate();

        Date startDate = DateUtil.parse(strStartDate);
        Date endDate = DateUtil.parse(strEndDate);

        int startYear = DateUtil.year(startDate);
        int endYear = DateUtil.year(endDate);

        for (int year = startYear; year <= endYear; year++) {
            AnnualProfit annualProfit = new AnnualProfit();
            annualProfit.setYear(year);

            float indexIncome = getIndexIncome(year, indexDatas);
            float trendIncome = getTrendIncome(year, profits);
            annualProfit.setIndexIncome(indexIncome);
            annualProfit.setTrendIncome(trendIncome);

            result.add(annualProfit);
        }

        return result;
    }
}
