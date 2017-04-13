package com.enation.schedule;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Calendar;

//import static org.quartz.DateBuilder.futureDate;
//import static org.quartz.TriggerBuilder.newTrigger;

import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;
import static org.quartz.DateBuilder.*;

/**
 * SchedulerTest
 *
 * @author panjn
 * @date 2016/9/26
 */
public class SchedulerTest {
    public static void main(String[] args) {
        try{
//      通过schedulerFactory获取一个调度器
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

            //       启动调度
            scheduler.start();


//       创建jobDetail实例，绑定Job实现类
//       指明job的名称，所在组的名称，以及绑定job类
        JobDetail jobDetail=JobBuilder.newJob(StartContentJob.class).withIdentity("任务1", "group1").build();
//       定义调度触发规则，比如每1秒运行一次，共运行8次
//            SimpleTrigger simpleTrigger=new SimpleTrigger("simpleTrigger","triggerGroup");
//            SimpleTrigger simpleTrigger = scheduler.getTrigger();
//            SimpleTrigger simpleTrigger = scheduler.getTrigger();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS");
        // Trigger the job to run now, and then repeat every 40 seconds
            Date date = new Date();
            System.out.println(sdf.format(date));
            java.util.Calendar cal = java.util.Calendar.getInstance();
            java.util.Calendar cal2 = java.util.Calendar.getInstance();
            cal.setTime(date);
//            cal.add(java.util.Calendar.MINUTE, 1);
            cal.add(java.util.Calendar.SECOND, -10);

            cal2.setTime(date);
            cal2.add(Calendar.SECOND, 10);

//            cal.add(java.util.Calendar.DATE, 3);

//            SimpleTrigger  trigger = (SimpleTrigger)newTrigger()
//                .withIdentity("trigger1", "group1")
//                        .startAt(futureDate(10, DateBuilder.IntervalUnit.SECOND))
////                .startAt(cal.getTime())
//                        //.startNow()
////                .withSchedule(simpleSchedule()
////                        //.withIntervalInSeconds(10)
////                        //.repeatForever()
////                )
//                    .build();

            System.out.println(sdf.format(cal.getTime()));
            SimpleTrigger trigger = (SimpleTrigger)newTrigger()
                    .withIdentity("trigger1", "group1")
//                    .startAt(futureDate(10, DateBuilder.IntervalUnit.SECOND))
                    .startAt(cal.getTime())
                            //.startNow()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                                .withIntervalInMilliseconds(1000)
                        .withMisfireHandlingInstructionNextWithExistingCount()
//                        .withRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY)
//                        .withRepeatCount(10)
                                .repeatForever()
                )
                    .endAt(cal2.getTime())
                    .build();

////       马上启动
//            simpleTrigger.setStartTime(new Date());
////       间隔时间
//            simpleTrigger.setRepeatInterval(1000);
////       运行次数
//            simpleTrigger.setRepeatCount(8);

//       把作业和触发器注册到任务调度中
            scheduler.scheduleJob(jobDetail, trigger);




        }catch(SchedulerException e){
            e.printStackTrace();
        }
    }
}
