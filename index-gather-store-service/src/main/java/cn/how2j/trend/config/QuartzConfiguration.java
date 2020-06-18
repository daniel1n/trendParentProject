package cn.how2j.trend.config;

import cn.how2j.trend.job.IndexDataSyncJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 定时器设置
 *
 * @author qqlin
 * @date 2020-6-13 22:08
 */
@Configuration
public class QuartzConfiguration {

    /**
     * 设置为1分钟
     */
    private static final int interval = 1;

    @Bean
    public JobDetail weatherDataSyncJobDetail() {
        return JobBuilder.newJob(IndexDataSyncJob.class)
                .withIdentity("indexDataSyncJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger weatherDataSyncTrigger() {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder
                .simpleSchedule()
                .withIntervalInMinutes(interval)
                .repeatForever();

        return TriggerBuilder.newTrigger().forJob(weatherDataSyncJobDetail())
                .withIdentity("indexDataSyncTrigger")
                .withSchedule(scheduleBuilder)
                .build();
    }
}
