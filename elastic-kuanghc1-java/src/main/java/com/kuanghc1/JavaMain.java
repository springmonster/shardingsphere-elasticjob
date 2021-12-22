package com.kuanghc1;/*
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

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.shardingsphere.elasticjob.tracing.api.TracingConfiguration;

import javax.sql.DataSource;
import java.io.IOException;

public final class JavaMain {

    // Zookeeper相关
    private static final String ZOOKEEPER_CONNECTION_STRING = "127.0.0.1:2181";
    private static final String JOB_NAMESPACE = "elastic-job-mysql";

    // MySQL相关
    private static final String EVENT_RDB_STORAGE_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String EVENT_RDB_STORAGE_URL = "jdbc:mysql://10.122.111.97:3306/elasticjob?characterEncoding=utf8&useSSL=false&serverTimezone=UTC&rewriteBatchedStatements=true";
    private static final String EVENT_RDB_STORAGE_USERNAME = "appadmin";
    private static final String EVENT_RDB_STORAGE_PASSWORD = "appadmin";

    public static void main(final String[] args) throws IOException {

        CoordinatorRegistryCenter regCenter = setUpRegistryCenter();

        // kuanghc1:配置Database，这里是通过数据库记录log
        TracingConfiguration<DataSource> tracingConfig = new TracingConfiguration<>("RDB", setUpEventTraceDataSource());
//        setUpHttpJob(regCenter, tracingConfig);
        setUpSimpleJob(regCenter, tracingConfig);
//        setUpDataflowJob(regCenter, tracingConfig);
//        setUpScriptJob(regCenter, tracingConfig);
//        setUpOneOffJob(regCenter, tracingConfig);
//        setUpOneOffJobWithEmail(regCenter, tracingConfig);
//        setUpOneOffJobWithDingtalk(regCenter, tracingConfig);
//        setUpOneOffJobWithWechat(regCenter, tracingConfig);
    }

    /**
     * result = {ZookeeperRegistryCenter@3649}
     * zkConfig = {ZookeeperConfiguration@3651}
     * serverLists = "127.0.0.1:2181"
     * namespace = "elastic-job-mysql"
     * baseSleepTimeMilliseconds = 1000
     * maxSleepTimeMilliseconds = 3000
     * maxRetries = 3
     * sessionTimeoutMilliseconds = 0
     * connectionTimeoutMilliseconds = 0
     * digest = null
     * caches = {ConcurrentHashMap@3652}  size = 0
     * client = {CuratorFrameworkImpl@3653}
     * log = {Logger@3658} "Logger[org.apache.curator.framework.imps.CuratorFrameworkImpl]"
     * client = {CuratorZookeeperClient@3659}
     * listeners = {StandardListenerManager@3660}
     * unhandledErrorListeners = {StandardListenerManager@3661}
     * threadFactory = {ThreadFactoryBuilder$1@3662}
     * maxCloseWaitMs = 1000
     * backgroundOperations = {DelayQueue@3663}  size = 0
     * forcedSleepOperations = {LinkedBlockingQueue@3664}  size = 0
     * namespace = {NamespaceImpl@3665}
     * connectionStateManager = {ConnectionStateManager@3666}
     * authInfos = {RegularImmutableList@3667}  size = 0
     * defaultData = {byte[9]@3668} [49, 50, 55, 46, 48, 46, 48, 46, 49]
     * failedDeleteManager = {FailedDeleteManager@3669}
     * failedRemoveWatcherManager = {FailedRemoveWatchManager@3670}
     * compressionProvider = {GzipCompressionProvider@3671}
     * aclProvider = {DefaultACLProvider@3672}
     * namespaceFacadeCache = {NamespaceFacadeCache@3673}
     * useContainerParentsIfAvailable = true
     * connectionStateErrorPolicy = {StandardConnectionStateErrorPolicy@3674}
     * currentInstanceIndex = {AtomicLong@3675} "1"
     * internalConnectionHandler = {StandardInternalConnectionHandler@3676}
     * ensembleTracker = {EnsembleTracker@3677}
     * schemaSet = {SchemaSet$2@3678}
     * runSafeService = {Executors$FinalizableDelegatedExecutorService@3679}
     * executorService = {Executors$DelegatedScheduledExecutorService@3680}
     * logAsErrorConnectionErrors = {AtomicBoolean@3681} "true"
     * debugListener = null
     * debugUnhandledErrorListener = null
     * state = {AtomicReference@3682} "STARTED"
     * debugCheckBackgroundRetryLatch = null
     * debugCheckBackgroundRetryReadyLatch = null
     * injectedCode = null
     * sleepAndQueueOperationSeconds = 1
     *
     * @return
     */
    private static CoordinatorRegistryCenter setUpRegistryCenter() {
        ZookeeperConfiguration zkConfig = new ZookeeperConfiguration(ZOOKEEPER_CONNECTION_STRING, JOB_NAMESPACE);
        CoordinatorRegistryCenter result = new ZookeeperRegistryCenter(zkConfig);
        result.init();
        return result;
    }

    private static DataSource setUpEventTraceDataSource() {
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName(EVENT_RDB_STORAGE_DRIVER);
        result.setUrl(EVENT_RDB_STORAGE_URL);
        result.setUsername(EVENT_RDB_STORAGE_USERNAME);
        result.setPassword(EVENT_RDB_STORAGE_PASSWORD);
        return result;
    }

//    private static void setUpHttpJob(final CoordinatorRegistryCenter regCenter, final TracingConfiguration<DataSource> tracingConfig) {
//        new ScheduleJobBootstrap(regCenter, "HTTP", JobConfiguration.newBuilder("javaHttpJob", 3)
//                .setProperty(HttpJobProperties.URI_KEY, "https://github.com")
//                .setProperty(HttpJobProperties.METHOD_KEY, "GET")
//                .cron("0/5 * * * * ?").shardingItemParameters("0=Beijing,1=Shanghai,2=Guangzhou").addExtraConfigurations(tracingConfig).build()).schedule();
//
//    }

    /**
     * @param regCenter
     * @param tracingConfig
     */
    private static void setUpSimpleJob(final CoordinatorRegistryCenter regCenter, final TracingConfiguration<DataSource> tracingConfig) {
        // kuanghc1:这里如果shardingItemParameters和shardingTotalCount的数目不相等会怎么样
        JobConfiguration jobConfiguration = JobConfiguration
                .newBuilder("kuanghc1-job", 3)
                .cron("0/5 * * * * ?")
                .shardingItemParameters("0=Beijing,1=Shanghai,2=Shenzhen")
                .addExtraConfigurations(tracingConfig)
                .build();

        new ScheduleJobBootstrap(regCenter, new JavaSimpleJob(), jobConfiguration).schedule();
    }

//    private static void setUpDataflowJob(final CoordinatorRegistryCenter regCenter, final TracingConfiguration<DataSource> tracingConfig) {
//        new ScheduleJobBootstrap(regCenter, new JavaDataflowJob(), JobConfiguration.newBuilder("javaDataflowElasticJob", 3)
//                .cron("0/5 * * * * ?").shardingItemParameters("0=Beijing,1=Shanghai,2=Guangzhou")
//                .setProperty(DataflowJobProperties.STREAM_PROCESS_KEY, Boolean.TRUE.toString()).addExtraConfigurations(tracingConfig).build()).schedule();
//    }
//
//    private static void setUpOneOffJob(final CoordinatorRegistryCenter regCenter, final TracingConfiguration<DataSource> tracingConfig) {
//        new OneOffJobBootstrap(regCenter, new com.kuanghc1.JavaSimpleJob(), JobConfiguration.newBuilder("javaOneOffSimpleJob", 3)
//                .shardingItemParameters("0=Beijing,1=Shanghai,2=Guangzhou").addExtraConfigurations(tracingConfig).build()).execute();
//    }
//
//    private static void setUpScriptJob(final CoordinatorRegistryCenter regCenter, final TracingConfiguration<DataSource> tracingConfig) throws IOException {
//        new ScheduleJobBootstrap(regCenter, "SCRIPT", JobConfiguration.newBuilder("scriptElasticJob", 3)
//                .cron("0/5 * * * * ?").setProperty(ScriptJobProperties.SCRIPT_KEY, buildScriptCommandLine()).addExtraConfigurations(tracingConfig).build()).schedule();
//    }
//
//    private static void setUpOneOffJobWithEmail(final CoordinatorRegistryCenter regCenter, final TracingConfiguration<DataSource> tracingConfig) {
//        JobConfiguration jobConfig = JobConfiguration.newBuilder("javaOccurErrorOfEmailJob", 3)
//                .shardingItemParameters("0=Beijing,1=Shanghai,2=Guangzhou").jobErrorHandlerType("EMAIL").addExtraConfigurations(tracingConfig).build();
//        setEmailProperties(jobConfig);
//        new OneOffJobBootstrap(regCenter, new JavaOccurErrorJob(), jobConfig).execute();
//    }

//    private static void setUpOneOffJobWithDingtalk(final CoordinatorRegistryCenter regCenter, final TracingConfiguration<DataSource> tracingConfig) {
//        JobConfiguration jobConfig = JobConfiguration.newBuilder("javaOccurErrorOfDingtalkJob", 3)
//                .shardingItemParameters("0=Beijing,1=Shanghai,2=Guangzhou").jobErrorHandlerType("DINGTALK").addExtraConfigurations(tracingConfig).build();
//        setDingtalkProperties(jobConfig);
//        new OneOffJobBootstrap(regCenter, new JavaOccurErrorJob(), jobConfig).execute();
//    }
//
//    private static void setUpOneOffJobWithWechat(final CoordinatorRegistryCenter regCenter, final TracingConfiguration<DataSource> tracingConfig) {
//        JobConfiguration jobConfig = JobConfiguration.newBuilder("javaOccurErrorOfWechatJob", 3)
//                .shardingItemParameters("0=Beijing,1=Shanghai,2=Guangzhou").jobErrorHandlerType("WECHAT").addExtraConfigurations(tracingConfig).build();
//        setWechatProperties(jobConfig);
//        new OneOffJobBootstrap(regCenter, new JavaOccurErrorJob(), jobConfig).execute();
//    }
//
//    private static void setEmailProperties(final JobConfiguration jobConfig) {
//        jobConfig.getProps().setProperty(EmailPropertiesConstants.HOST, "host");
//        jobConfig.getProps().setProperty(EmailPropertiesConstants.PORT, "465");
//        jobConfig.getProps().setProperty(EmailPropertiesConstants.USERNAME, "username");
//        jobConfig.getProps().setProperty(EmailPropertiesConstants.PASSWORD, "password");
//        jobConfig.getProps().setProperty(EmailPropertiesConstants.FROM, "from@xxx.xx");
//        jobConfig.getProps().setProperty(EmailPropertiesConstants.TO, "to1@xxx.xx,to1@xxx.xx");
//    }

//    private static void setDingtalkProperties(final JobConfiguration jobConfig) {
//        jobConfig.getProps().setProperty(DingtalkPropertiesConstants.WEBHOOK, "https://oapi.dingtalk.com/robot/send?access_token=token");
//        jobConfig.getProps().setProperty(DingtalkPropertiesConstants.KEYWORD, "keyword");
//        jobConfig.getProps().setProperty(DingtalkPropertiesConstants.SECRET, "secret");
//    }
//
//    private static void setWechatProperties(final JobConfiguration jobConfig) {
//        jobConfig.getProps().setProperty(WechatPropertiesConstants.WEBHOOK, "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=key");
//    }
//
//    private static String buildScriptCommandLine() throws IOException {
//        if (System.getProperties().getProperty("os.name").contains("Windows")) {
//            return Paths.get(com.kuanghc1.JavaMain.class.getResource("/script/demo.bat").getPath().substring(1)).toString();
//        }
//        Path result = Paths.get(com.kuanghc1.JavaMain.class.getResource("/script/demo.sh").getPath());
//        Files.setPosixFilePermissions(result, PosixFilePermissions.fromString("rwxr-xr-x"));
//        return result.toString();
//    }
}