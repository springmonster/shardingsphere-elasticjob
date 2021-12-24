/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.elasticjob.lite.internal.schedule;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.elasticjob.infra.handler.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Job registry.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JobRegistry {

    private static volatile JobRegistry instance;

    /**
     * 作业调度控制器集合
     * key：作业名称
     */
    private final Map<String, JobScheduleController> schedulerMap = new ConcurrentHashMap<>();

    /**
     * 注册中心集合
     * key：作业名称
     */
    private final Map<String, CoordinatorRegistryCenter> regCenterMap = new ConcurrentHashMap<>();

    /**
     * 作业运行实例集合
     * key：作业名称
     */
    private final Map<String, JobInstance> jobInstanceMap = new ConcurrentHashMap<>();

    /**
     * 运行中作业集合
     * key：作业名字
     */
    private final Map<String, Boolean> jobRunningMap = new ConcurrentHashMap<>();

    /**
     * 作业总分片数量集合
     * key：作业名字
     */
    private final Map<String, Integer> currentShardingTotalCountMap = new ConcurrentHashMap<>();

    /**
     * 单例模式
     * <p>
     * Get instance of job registry.
     *
     * @return instance of job registry
     */
    public static JobRegistry getInstance() {
        if (null == instance) {
            synchronized (JobRegistry.class) {
                if (null == instance) {
                    instance = new JobRegistry();
                }
            }
        }
        return instance;
    }

    /**
     * Register registry center.
     *
     * @param jobName   job name
     * @param regCenter registry center
     */
    public void registerRegistryCenter(final String jobName, final CoordinatorRegistryCenter regCenter) {
        regCenterMap.put(jobName, regCenter);
        // kuanghc1:！！！这里是关键，注册进zk，然后从 AbstractJobListener 就能收到监听事件
        regCenter.addCacheData("/" + jobName);
    }

    /**
     * Register job.
     * <p>
     * schedulerMap = {ConcurrentHashMap@4891}  size = 1
     * "kuanghc1-job" -> {JobScheduleController@4868}
     * key = "kuanghc1-job"
     * value = {JobScheduleController@4868}
     * scheduler = {StdScheduler@4064}
     * jobDetail = {JobDetailImpl@4869} "JobDetail 'DEFAULT.kuanghc1-job':  jobClass: 'org.apache.shardingsphere.elasticjob.lite.internal.schedule.LiteJob concurrentExectionDisallowed: false persistJobDataAfterExecution: false isDurable: false requestsRecovers: false"
     * triggerIdentity = "kuanghc1-job"
     *
     * @param jobName               job name
     * @param jobScheduleController job schedule controller
     */
    public void registerJob(final String jobName, final JobScheduleController jobScheduleController) {
        schedulerMap.put(jobName, jobScheduleController);
    }

    /**
     * Get job schedule controller.
     *
     * @param jobName job name
     * @return job schedule controller
     */
    public JobScheduleController getJobScheduleController(final String jobName) {
        return schedulerMap.get(jobName);
    }

    /**
     * Get registry center.
     *
     * @param jobName job name
     * @return registry center
     */
    public CoordinatorRegistryCenter getRegCenter(final String jobName) {
        return regCenterMap.get(jobName);
    }

    /**
     * Add job instance.
     *
     * @param jobName     job name
     * @param jobInstance job instance
     */
    public void addJobInstance(final String jobName, final JobInstance jobInstance) {
        jobInstanceMap.put(jobName, jobInstance);
    }

    /**
     * Get job instance.
     *
     * @param jobName job name
     * @return job instance
     */
    public JobInstance getJobInstance(final String jobName) {
        return jobInstanceMap.get(jobName);
    }

    /**
     * Judge job is running or not.
     *
     * @param jobName job name
     * @return job is running or not
     */
    public boolean isJobRunning(final String jobName) {
        return jobRunningMap.getOrDefault(jobName, false);
    }

    /**
     * Set job running status.
     *
     * @param jobName   job name
     * @param isRunning job running status
     */
    public void setJobRunning(final String jobName, final boolean isRunning) {
        jobRunningMap.put(jobName, isRunning);
    }

    /**
     * Get sharding total count which running on current job server.
     *
     * @param jobName job name
     * @return sharding total count which running on current job server
     */
    public int getCurrentShardingTotalCount(final String jobName) {
        return currentShardingTotalCountMap.getOrDefault(jobName, 0);
    }

    /**
     * Set sharding total count which running on current job server.
     *
     * @param jobName                   job name
     * @param currentShardingTotalCount sharding total count which running on current job server
     */
    public void setCurrentShardingTotalCount(final String jobName, final int currentShardingTotalCount) {
        currentShardingTotalCountMap.put(jobName, currentShardingTotalCount);
    }

    /**
     * Shutdown job schedule.
     *
     * @param jobName job name
     */
    public void shutdown(final String jobName) {
        Optional.ofNullable(schedulerMap.remove(jobName)).ifPresent(JobScheduleController::shutdown);
        Optional.ofNullable(regCenterMap.remove(jobName)).ifPresent(regCenter -> regCenter.evictCacheData("/" + jobName));
        jobInstanceMap.remove(jobName);
        jobRunningMap.remove(jobName);
        currentShardingTotalCountMap.remove(jobName);
    }

    /**
     * Judge job is shutdown or not.
     *
     * @param jobName job name
     * @return job is shutdown or not
     */
    public boolean isShutdown(final String jobName) {
        return !schedulerMap.containsKey(jobName) || !jobInstanceMap.containsKey(jobName);
    }
}