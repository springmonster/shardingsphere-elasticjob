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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.elasticjob.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.error.handler.JobErrorHandlerPropertiesValidator;
import org.apache.shardingsphere.elasticjob.executor.ElasticJobExecutor;
import org.apache.shardingsphere.elasticjob.infra.exception.JobSystemException;
import org.apache.shardingsphere.elasticjob.infra.handler.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.infra.listener.ElasticJobListener;
import org.apache.shardingsphere.elasticjob.infra.listener.ElasticJobListenerFactory;
import org.apache.shardingsphere.elasticjob.infra.spi.ElasticJobServiceLoader;
import org.apache.shardingsphere.elasticjob.lite.api.listener.AbstractDistributeOnceElasticJobListener;
import org.apache.shardingsphere.elasticjob.lite.internal.guarantee.GuaranteeService;
import org.apache.shardingsphere.elasticjob.lite.internal.setup.JobClassNameProviderFactory;
import org.apache.shardingsphere.elasticjob.lite.internal.setup.SetUpFacade;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.tracing.api.TracingConfiguration;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.simpl.SimpleThreadPool;

import java.util.Collection;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Job scheduler.
 */
public final class JobScheduler {

    static {
        ElasticJobServiceLoader.registerTypedService(JobErrorHandlerPropertiesValidator.class);
    }

    private static final String JOB_EXECUTOR_DATA_MAP_KEY = "jobExecutor";

    @Getter
    private final CoordinatorRegistryCenter coordinatorRegistryCenter;

    @Getter
    private final JobConfiguration jobConfiguration;

    private final SetUpFacade setUpFacade;

    private final SchedulerFacade schedulerFacade;

    private final LiteJobFacade liteJobFacade;

    private final ElasticJobExecutor elasticJobExecutor;

    @Getter
    private final JobScheduleController jobScheduleController;

    public JobScheduler(final CoordinatorRegistryCenter coordinatorRegistryCenter, final ElasticJob elasticJob, final JobConfiguration jobConfiguration) {
        Preconditions.checkArgument(null != elasticJob, "Elastic job cannot be null.");

        this.coordinatorRegistryCenter = coordinatorRegistryCenter;

        Collection<ElasticJobListener> jobListeners = getElasticJobListeners(jobConfiguration);

        setUpFacade = new SetUpFacade(coordinatorRegistryCenter, jobConfiguration.getJobName(), jobListeners);

        String jobClassName = JobClassNameProviderFactory.getProvider().getJobClassName(elasticJob);

        // 这里是设置JobConfiguration
        this.jobConfiguration = setUpFacade.setUpJobConfiguration(jobClassName, jobConfiguration);

        schedulerFacade = new SchedulerFacade(coordinatorRegistryCenter, jobConfiguration.getJobName());

        liteJobFacade = new LiteJobFacade(coordinatorRegistryCenter, jobConfiguration.getJobName(), jobListeners, findTracingConfiguration().orElse(null));

        validateJobProperties();

        // 这里还会读取jobConfiguration
        elasticJobExecutor = new ElasticJobExecutor(elasticJob, this.jobConfiguration, liteJobFacade);

        setGuaranteeServiceForElasticJobListeners(coordinatorRegistryCenter, jobListeners);

        jobScheduleController = createJobScheduleController();
    }

    public JobScheduler(final CoordinatorRegistryCenter regCenter, final String elasticJobType, final JobConfiguration jobConfig) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(elasticJobType), "Elastic job type cannot be null or empty.");
        this.coordinatorRegistryCenter = regCenter;
        Collection<ElasticJobListener> jobListeners = getElasticJobListeners(jobConfig);
        setUpFacade = new SetUpFacade(regCenter, jobConfig.getJobName(), jobListeners);
        this.jobConfiguration = setUpFacade.setUpJobConfiguration(elasticJobType, jobConfig);
        schedulerFacade = new SchedulerFacade(regCenter, jobConfig.getJobName());
        liteJobFacade = new LiteJobFacade(regCenter, jobConfig.getJobName(), jobListeners, findTracingConfiguration().orElse(null));
        validateJobProperties();
        elasticJobExecutor = new ElasticJobExecutor(elasticJobType, this.jobConfiguration, liteJobFacade);
        setGuaranteeServiceForElasticJobListeners(regCenter, jobListeners);
        jobScheduleController = createJobScheduleController();
    }

    private Collection<ElasticJobListener> getElasticJobListeners(final JobConfiguration jobConfiguration) {
        return jobConfiguration.getJobListenerTypes().stream()
                .map(type -> ElasticJobListenerFactory.createListener(type).orElseThrow(() -> new IllegalArgumentException(String.format("Can not find job listener type '%s'.", type))))
                .collect(Collectors.toList());
    }

    private Optional<TracingConfiguration<?>> findTracingConfiguration() {
        return jobConfiguration.getExtraConfigurations().stream().filter(each -> each instanceof TracingConfiguration).findFirst().map(extraConfig -> (TracingConfiguration<?>) extraConfig);
    }

    private void validateJobProperties() {
        validateJobErrorHandlerProperties();
    }

    private void validateJobErrorHandlerProperties() {
        if (null != jobConfiguration.getJobErrorHandlerType()) {
            ElasticJobServiceLoader.newTypedServiceInstance(JobErrorHandlerPropertiesValidator.class, jobConfiguration.getJobErrorHandlerType(), jobConfiguration.getProps())
                    .ifPresent(validator -> validator.validate(jobConfiguration.getProps()));
        }
    }

    private void setGuaranteeServiceForElasticJobListeners(final CoordinatorRegistryCenter regCenter, final Collection<ElasticJobListener> elasticJobListeners) {
        GuaranteeService guaranteeService = new GuaranteeService(regCenter, jobConfiguration.getJobName());
        for (ElasticJobListener each : elasticJobListeners) {
            if (each instanceof AbstractDistributeOnceElasticJobListener) {
                ((AbstractDistributeOnceElasticJobListener) each).setGuaranteeService(guaranteeService);
            }
        }
    }

    /**
     * result = {JobScheduleController@4868}
     *  scheduler = {StdScheduler@4064}
     *  jobDetail = {JobDetailImpl@4869} "JobDetail 'DEFAULT.kuanghc1-job':  jobClass: 'org.apache.shardingsphere.elasticjob.lite.internal.schedule.LiteJob concurrentExectionDisallowed: false persistJobDataAfterExecution: false isDurable: false requestsRecovers: false"
     *   name = "kuanghc1-job"
     *   group = "DEFAULT"
     *   description = null
     *   jobClass = {Class@4873} "class org.apache.shardingsphere.elasticjob.lite.internal.schedule.LiteJob"
     *   jobDataMap = {JobDataMap@4874}  size = 1
     *    "jobExecutor" -> {ElasticJobExecutor@3987}
     *     key = "jobExecutor"
     *     value = {ElasticJobExecutor@3987}
     *      elasticJob = {JavaSimpleJob@3988}
     *      jobFacade = {LiteJobFacade@3479}
     *      jobItemExecutor = {SimpleJobExecutor@3989}
     *      executorContext = {ExecutorContext@3990}
     *      itemErrorMessages = {ConcurrentHashMap@4888}  size = 0
     *   durability = false
     *   shouldRecover = false
     *   key = {JobKey@4875} "DEFAULT.kuanghc1-job"
     *    name = "kuanghc1-job"
     *    group = "DEFAULT"
     *  triggerIdentity = "kuanghc1-job"
     *
     * @return
     */
    private JobScheduleController createJobScheduleController() {
        // kuanghc1：这里的 createScheduler() 启动了quartz的任务
        JobScheduleController result = new JobScheduleController(createScheduler(), createJobDetail(), this.getJobConfiguration().getJobName());
        JobRegistry.getInstance().registerJob(this.getJobConfiguration().getJobName(), result);
        registerStartUpInfo();
        // 到这里的时候，zk中的结构是
        // namespace/job/instances
        // namespace/job/leader
        // namespace/job/servers
        return result;
    }

    /**
     * kuanghc1:这里就会创建
     *
     * result = {StdScheduler@4064}
     *  sched = {QuartzScheduler@4065}
     *   resources = {QuartzSchedulerResources@4066}
     *   schedThread = {QuartzSchedulerThread@4047} "Thread[kuanghc1-job_QuartzSchedulerThread,5,QuartzScheduler:kuanghc1-job]"
     *   threadGroup = {ThreadGroup@4067} "java.lang.ThreadGroup[name=QuartzScheduler:kuanghc1-job,maxpri=10]"
     *   context = {SchedulerContext@4068}  size = 0
     *   listenerManager = {ListenerManagerImpl@4069}
     *   internalJobListeners = {HashMap@4070}  size = 1
     *   internalTriggerListeners = {HashMap@4071}  size = 0
     *   internalSchedulerListeners = {ArrayList@4072}  size = 1
     *   jobFactory = {PropertySettingJobFactory@4073}
     *   jobMgr = {ExecutingJobsManager@4074}
     *   errLogger = {ErrorLogger@4075}
     *   signaler = {SchedulerSignalerImpl@4076}
     *   random = {Random@4077}
     *   holdToPreventGC = {ArrayList@4078}  size = 1
     *   signalOnSchedulingChange = true
     *   closed = false
     *   shuttingDown = false
     *   boundRemotely = false
     *   jmxBean = null
     *   initialStart = null
     *   log = {Logger@4079} "Logger[org.quartz.core.QuartzScheduler]"
     *
     * @return
     * @see SimpleThreadPool#initialize()
     * @see SimpleThreadPool 277行
     * @see StdSchedulerFactory
     * 1333行，QuartzScheduler 的初始化
     * QuartzScheduler 的215行初始化 QuartzSchedulerThread ，217行启动 QuartzSchedulerThread
     * 然后初始化 QuartzSchedulerThread
     * QuartzSchedulerThread 的398行，执行 JobRunShell，if (qsRsrcs.getThreadPool().runInThread(shell) == false) {
     * 在 SimpleThreadPool 中，427行左右，JobRunShell
     * JobRunShell 的 202行，到LiteJob，
     * 然后调用，ElasticJobExecutor，JobItemExecutor，SimpleJobExecutor，然后就调用到了JavaSimpleJob
     */
    private Scheduler createScheduler() {
        Scheduler result;
        try {
            StdSchedulerFactory factory = new StdSchedulerFactory();
            factory.initialize(getQuartzProps());
            result = factory.getScheduler();
            result.getListenerManager().addTriggerListener(schedulerFacade.newJobTriggerListener());
        } catch (final SchedulerException ex) {
            throw new JobSystemException(ex);
        }
        return result;
    }

    private Properties getQuartzProps() {
        Properties result = new Properties();
        result.put("org.quartz.threadPool.class", SimpleThreadPool.class.getName());
        result.put("org.quartz.threadPool.threadCount", "1");
        result.put("org.quartz.scheduler.instanceName", this.getJobConfiguration().getJobName());
        result.put("org.quartz.jobStore.misfireThreshold", "1");
        result.put("org.quartz.plugin.shutdownhook.class", JobShutdownHookPlugin.class.getName());
        result.put("org.quartz.plugin.shutdownhook.cleanShutdown", Boolean.TRUE.toString());
        return result;
    }

    private JobDetail createJobDetail() {
        JobDetail result = JobBuilder.newJob(LiteJob.class).withIdentity(this.getJobConfiguration().getJobName()).build();
        result.getJobDataMap().put(JOB_EXECUTOR_DATA_MAP_KEY, elasticJobExecutor);
        return result;
    }

    private void registerStartUpInfo() {
        JobRegistry.getInstance().registerRegistryCenter(jobConfiguration.getJobName(), coordinatorRegistryCenter);
        JobRegistry.getInstance().addJobInstance(jobConfiguration.getJobName(), new JobInstance());
        JobRegistry.getInstance().setCurrentShardingTotalCount(jobConfiguration.getJobName(), jobConfiguration.getShardingTotalCount());
        setUpFacade.registerStartUpInfo(!jobConfiguration.isDisabled());
    }

    /**
     * Shutdown job.
     */
    public void shutdown() {
        setUpFacade.tearDown();
        schedulerFacade.shutdownInstance();
        elasticJobExecutor.shutdown();
    }
}