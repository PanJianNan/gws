package com.enation.schedule;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * StartContentJob
 *
 * @author panjn
 * @date 2016/9/26
 */
public class StopContentJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.out.println("停用内容！");
    }
}
