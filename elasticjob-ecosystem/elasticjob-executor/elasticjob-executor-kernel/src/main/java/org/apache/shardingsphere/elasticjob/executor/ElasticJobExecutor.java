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

package org.apache.shardingsphere.elasticjob.executor;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.error.handler.JobErrorHandler;
import org.apache.shardingsphere.elasticjob.executor.context.ExecutorContext;
import org.apache.shardingsphere.elasticjob.executor.item.JobItemExecutor;
import org.apache.shardingsphere.elasticjob.executor.item.JobItemExecutorFactory;
import org.apache.shardingsphere.elasticjob.infra.env.IpUtils;
import org.apache.shardingsphere.elasticjob.infra.exception.ExceptionUtils;
import org.apache.shardingsphere.elasticjob.infra.exception.JobExecutionEnvironmentException;
import org.apache.shardingsphere.elasticjob.infra.listener.ShardingContexts;
import org.apache.shardingsphere.elasticjob.tracing.event.JobExecutionEvent;
import org.apache.shardingsphere.elasticjob.tracing.event.JobExecutionEvent.ExecutionSource;
import org.apache.shardingsphere.elasticjob.tracing.event.JobStatusTraceEvent.State;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

/**
 * ElasticJob executor.
 */
@Slf4j
public final class ElasticJobExecutor {

    private final ElasticJob elasticJob;

    private final JobFacade jobFacade;

    private final JobItemExecutor jobItemExecutor;

    private final ExecutorContext executorContext;

    private final Map<Integer, String> itemErrorMessages;

    public ElasticJobExecutor(final ElasticJob elasticJob, final JobConfiguration jobConfig, final JobFacade jobFacade) {
        this(elasticJob, jobConfig, jobFacade, JobItemExecutorFactory.getExecutor(elasticJob.getClass()));
    }

    public ElasticJobExecutor(final String type, final JobConfiguration jobConfig, final JobFacade jobFacade) {
        this(null, jobConfig, jobFacade, JobItemExecutorFactory.getExecutor(type));
    }

    private ElasticJobExecutor(final ElasticJob elasticJob, final JobConfiguration jobConfig, final JobFacade jobFacade, final JobItemExecutor jobItemExecutor) {
        this.elasticJob = elasticJob;
        this.jobFacade = jobFacade;
        this.jobItemExecutor = jobItemExecutor;

        executorContext = new ExecutorContext(jobFacade.loadJobConfiguration(true));
        itemErrorMessages = new ConcurrentHashMap<>(jobConfig.getShardingTotalCount(), 1);
    }

    /**
     * kuanghc1:!!!这里需要重点关注
     * khc:------>JobRunShell，LiteJob，ElasticJobExecutor
     * <p>
     * Execute job.
     */
    public void execute() {
        // 这里是获取元信息，可以决定是否从缓存
        JobConfiguration jobConfiguration = jobFacade.loadJobConfiguration(true);

        // 是否重载，分为CPU和LOG
        executorContext.reloadIfNecessary(jobConfiguration);
        JobErrorHandler jobErrorHandler = executorContext.get(JobErrorHandler.class);
        try {
            // 检查本机与服务器的时间差
            // Elastic-Job-Lite 作业触发是依赖本机时间，相同集群使用注册中心时间为基准，校验本机与注册中心的时间误差是否在允许范围内
            // 可能会抛出异常
            jobFacade.checkJobExecutionEnvironment();
        } catch (final JobExecutionEnvironmentException cause) {
            jobErrorHandler.handleException(jobConfiguration.getJobName(), cause);
        }
        /**
         * result = {ShardingContexts@5199} "ShardingContexts(taskId=kuanghc1-1224-3@-@0,1,2@-@READY@-@10.109.71.139@-@54196, jobName=kuanghc1-1224-3, shardingTotalCount=3, jobParameter=, shardingItemParameters={0=Beijing, 1=Shanghai, 2=Shenzhen}, jobEventSamplingCount=0, currentJobEventSamplingCount=0, allowSendJobEvent=true)"
         *  taskId = "kuanghc1-1224-3@-@0,1,2@-@READY@-@10.109.71.139@-@54196"
         *  jobName = "kuanghc1-1224-3"
         *  shardingTotalCount = 3
         *  jobParameter = ""
         *  shardingItemParameters = {HashMap@5209}  size = 3
         *  jobEventSamplingCount = 0
         *  currentJobEventSamplingCount = 0
         *  allowSendJobEvent = true
         */
        ShardingContexts shardingContexts = jobFacade.getShardingContexts();

        // 发送job的状态，这里会保存到数据库
        jobFacade.postJobStatusTraceEvent(shardingContexts.getTaskId(), State.TASK_STAGING, String.format("Job '%s' execute begin.", jobConfiguration.getJobName()));

        // 这里没看懂
        // TODO: 2021/12/24
        if (jobFacade.misfireIfRunning(shardingContexts.getShardingItemParameters().keySet())) {
            jobFacade.postJobStatusTraceEvent(shardingContexts.getTaskId(), State.TASK_FINISHED, String.format(
                    "Previous job '%s' - shardingItems '%s' is still running, misfired job will start after previous job completed.", jobConfiguration.getJobName(),
                    shardingContexts.getShardingItemParameters().keySet()));
            return;
        }

        try {
            jobFacade.beforeJobExecuted(shardingContexts);
            //CHECKSTYLE:OFF
        } catch (final Throwable cause) {
            //CHECKSTYLE:OFF
            jobErrorHandler.handleException(jobConfiguration.getJobName(), cause);
        }

        // 正常执行
        execute(jobConfiguration, shardingContexts, ExecutionSource.NORMAL_TRIGGER);

        while (jobFacade.isExecuteMisfired(shardingContexts.getShardingItemParameters().keySet())) {
            jobFacade.clearMisfire(shardingContexts.getShardingItemParameters().keySet());
            execute(jobConfiguration, shardingContexts, ExecutionSource.MISFIRE);
        }

        jobFacade.failoverIfNecessary();

        try {
            jobFacade.afterJobExecuted(shardingContexts);
            //CHECKSTYLE:OFF
        } catch (final Throwable cause) {
            //CHECKSTYLE:OFF
            jobErrorHandler.handleException(jobConfiguration.getJobName(), cause);
        }
    }

    private void execute(final JobConfiguration jobConfig, final ShardingContexts shardingContexts, final ExecutionSource executionSource) {
        if (shardingContexts.getShardingItemParameters().isEmpty()) {
            jobFacade.postJobStatusTraceEvent(shardingContexts.getTaskId(), State.TASK_FINISHED, String.format("Sharding item for job '%s' is empty.", jobConfig.getJobName()));
            return;
        }
        // 这里是去sharding下面写入有几个分区，例如[0，1，2]
        jobFacade.registerJobBegin(shardingContexts);

        String taskId = shardingContexts.getTaskId();

        // 又发送，可能又写数据库，需要验证！！！
        jobFacade.postJobStatusTraceEvent(taskId, State.TASK_RUNNING, "");

        try {
            process(jobConfig, shardingContexts, executionSource);
        } finally {
            // TODO Consider increasing the status of job failure, and how to handle the overall loop of job failure
            jobFacade.registerJobCompleted(shardingContexts);
            if (itemErrorMessages.isEmpty()) {
                // 又是发送消息落库
                jobFacade.postJobStatusTraceEvent(taskId, State.TASK_FINISHED, "");
            } else {
                jobFacade.postJobStatusTraceEvent(taskId, State.TASK_ERROR, itemErrorMessages.toString());
            }
        }
    }

    private void process(final JobConfiguration jobConfig, final ShardingContexts shardingContexts, final ExecutionSource executionSource) {
        Collection<Integer> items = shardingContexts.getShardingItemParameters().keySet();

        log.info("分片的大小是 {} ", items.size());

        // 如果分片大小是1，直接执行
        if (1 == items.size()) {
            int item = shardingContexts.getShardingItemParameters().keySet().iterator().next();
            JobExecutionEvent jobExecutionEvent = new JobExecutionEvent(IpUtils.getHostName(), IpUtils.getIp(), shardingContexts.getTaskId(), jobConfig.getJobName(), executionSource, item);
            process(jobConfig, shardingContexts, item, jobExecutionEvent);
            return;
        }
        // 否则启动CountDownLatch
        CountDownLatch latch = new CountDownLatch(items.size());
        for (int each : items) {
            /**
             * jobExecutionEvent = {JobExecutionEvent@5246} ""
             *  id = "6b304a4c-067d-4207-ba90-934ff809c99b"
             *  hostname = "kuanghaochuandeMacBook-Pro.local"
             *  ip = "10.109.71.139"
             *  taskId = "kuanghc1-job@-@0,1,2@-@READY@-@10.109.71.139@-@56798"
             *  jobName = "kuanghc1-job"
             *  source = {JobExecutionEvent$ExecutionSource@4450} "NORMAL_TRIGGER"
             *  shardingItem = 0
             *  startTime = {Date@5253} "Tue Dec 21 17:59:30 CST 2021"
             *  completeTime = null
             *  success = false
             *  failureCause = null
             */
            JobExecutionEvent jobExecutionEvent = new JobExecutionEvent(IpUtils.getHostName(), IpUtils.getIp(), shardingContexts.getTaskId(), jobConfig.getJobName(), executionSource, each);
            ExecutorService executorService = executorContext.get(ExecutorService.class);
            if (executorService.isShutdown()) {
                return;
            }
            executorService.submit(() -> {
                try {
                    process(jobConfig, shardingContexts, each, jobExecutionEvent);
                } finally {
                    latch.countDown();
                }
            });
        }
        try {
            // 都执行完才算结束！
            latch.await();
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    @SuppressWarnings("unchecked")
    private void process(final JobConfiguration jobConfig, final ShardingContexts shardingContexts, final int item, final JobExecutionEvent startEvent) {
        // 又是发消息
        jobFacade.postJobExecutionEvent(startEvent);

        log.info("Job '{}' executing, item is: '{}'.", jobConfig.getJobName(), item);

        JobExecutionEvent completeEvent;

        try {
            // 这里就到了SimpleJob了
            jobItemExecutor.process(elasticJob, jobConfig, jobFacade, shardingContexts.createShardingContext(item));
            // 这里如果没问题，那么就产生一个成功的消息
            completeEvent = startEvent.executionSuccess();

            log.info("Job '{}' executed, item is: '{}'.", jobConfig.getJobName(), item);

            jobFacade.postJobExecutionEvent(completeEvent);

        } catch (final Throwable cause) {

            // 这里如果失败了，那么就产生一个失败的消息
            completeEvent = startEvent.executionFailure(ExceptionUtils.transform(cause));
            jobFacade.postJobExecutionEvent(completeEvent);
            itemErrorMessages.put(item, ExceptionUtils.transform(cause));
            JobErrorHandler jobErrorHandler = executorContext.get(JobErrorHandler.class);
            jobErrorHandler.handleException(jobConfig.getJobName(), cause);
        }
    }

    /**
     * Shutdown executor.
     */
    public void shutdown() {
        executorContext.shutdown();
    }
}