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

package org.apache.shardingsphere.elasticjob.lite.internal.config;

import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.infra.env.TimeService;
import org.apache.shardingsphere.elasticjob.infra.exception.JobConfigurationException;
import org.apache.shardingsphere.elasticjob.infra.exception.JobExecutionEnvironmentException;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.infra.yaml.YamlEngine;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;

/**
 * Configuration service.
 */
public final class ConfigurationService {

    private final TimeService timeService;

    private final JobNodeStorage jobNodeStorage;

    public ConfigurationService(final CoordinatorRegistryCenter regCenter, final String jobName) {
        jobNodeStorage = new JobNodeStorage(regCenter, jobName);
        timeService = new TimeService();
    }

    /**
     * Load job configuration.
     *
     * @param fromCache load from cache or not
     * @return job configuration
     */
    public JobConfiguration load(final boolean fromCache) {
        String result;
        if (fromCache) {
            result = jobNodeStorage.getJobNodeData(ConfigurationNode.ROOT);
            if (null == result) {
                result = jobNodeStorage.getJobNodeDataDirectly(ConfigurationNode.ROOT);
            }
        } else {
            /**
             * cron: 0/5 * * * * ?
             * description: ''
             * disabled: false
             * failover: false
             * jobExtraConfigurations:
             * - !!org.apache.shardingsphere.elasticjob.tracing.yaml.YamlTracingConfiguration
             *   tracingStorageConfiguration: !!org.apache.shardingsphere.elasticjob.tracing.rdb.yaml.YamlDataSourceConfiguration
             *     dataSourceClassName: org.apache.commons.dbcp2.BasicDataSource
             *     props:
             *       logAbandoned: false
             *       numTestsPerEvictionRun: 3
             *       url: jdbc:mysql://10.122.111.97:3306/elasticjob?characterEncoding=utf8&useSSL=false&serverTimezone=UTC&rewriteBatchedStatements=true
             *       timeBetweenEvictionRunsMillis: -1
             *       enableAutoCommitOnReturn: true
             *       removeAbandonedTimeout: 300
             *       lifo: true
             *       softMinEvictableIdleTimeMillis: -1
             *       driverClassName: com.mysql.cj.jdbc.Driver
             *       rollbackOnReturn: true
             *       abandonedUsageTracking: false
             *       testOnBorrow: true
             *       testOnReturn: false
             *       numIdle: 1
             *       maxIdle: 8
             *       minIdle: 0
             *       logExpiredConnections: true
             *       maxTotal: 8
             *       numActive: 0
             *       defaultTransactionIsolation: -1
             *       testOnCreate: false
             *       removeAbandonedOnMaintenance: false
             *       fastFailValidation: false
             *       password: appadmin
             *       cacheState: true
             *       initialSize: 0
             *       maxWaitMillis: -1
             *       validationQueryTimeout: -1
             *       evictionPolicyClassName: org.apache.commons.pool2.impl.DefaultEvictionPolicy
             *       maxOpenPreparedStatements: -1
             *       removeAbandonedOnBorrow: false
             *       connectionInitSqls: []
             *       testWhileIdle: false
             *       autoCommitOnReturn: true
             *       minEvictableIdleTimeMillis: 1800000
             *       maxConnLifetimeMillis: -1
             *       username: appadmin
             *   type: RDB
             * jobName: kuanghc1-job
             * jobParameter: ''
             * maxTimeDiffSeconds: -1
             * misfire: true
             * monitorExecution: true
             * overwrite: false
             * reconcileIntervalMinutes: 10
             * shardingItemParameters: 0=Beijing,1=Shanghai,2=Shenzhen
             * shardingTotalCount: 3
             * staticSharding: false
             */
            result = jobNodeStorage.getJobNodeDataDirectly(ConfigurationNode.ROOT);
        }
        return YamlEngine.unmarshal(result, JobConfigurationPOJO.class).toJobConfiguration();
    }

    /**
     * Set up job configuration.
     *
     * @param jobClassName job class name
     * @param jobConfig    job configuration to be updated
     * @return accepted job configuration
     */
    public JobConfiguration setUpJobConfiguration(final String jobClassName, final JobConfiguration jobConfig) {
        checkConflictJob(jobClassName, jobConfig);
        if (!jobNodeStorage.isJobNodeExisted(ConfigurationNode.ROOT) || jobConfig.isOverwrite()) {
            jobNodeStorage.replaceJobNode(ConfigurationNode.ROOT, YamlEngine.marshal(JobConfigurationPOJO.fromJobConfiguration(jobConfig)));
            jobNodeStorage.replaceJobRootNode(jobClassName);
            return jobConfig;
        }
        return load(false);
    }

    private void checkConflictJob(final String newJobClassName, final JobConfiguration jobConfig) {
        if (!jobNodeStorage.isJobRootNodeExisted()) {
            return;
        }
        String originalJobClassName = jobNodeStorage.getJobRootNodeData();
        if (null != originalJobClassName && !originalJobClassName.equals(newJobClassName)) {
            throw new JobConfigurationException(
                    "Job conflict with register center. The job '%s' in register center's class is '%s', your job class is '%s'", jobConfig.getJobName(), originalJobClassName, newJobClassName);
        }
    }

    /**
     * kuanghc1:检查作业服务器和注册中心之间可容忍的最大时间不同秒数。
     * Check max time different seconds tolerable between job server and registry center.
     *
     * @throws JobExecutionEnvironmentException throe JobExecutionEnvironmentException if exceed max time different seconds
     */
    public void checkMaxTimeDiffSecondsTolerable() throws JobExecutionEnvironmentException {
        int maxTimeDiffSeconds = load(true).getMaxTimeDiffSeconds();
        if (0 > maxTimeDiffSeconds) {
            return;
        }
        long timeDiff = Math.abs(timeService.getCurrentMillis() - jobNodeStorage.getRegistryCenterTime());
        if (timeDiff > maxTimeDiffSeconds * 1000L) {
            throw new JobExecutionEnvironmentException(
                    "Time different between job server and register center exceed '%s' seconds, max time different is '%s' seconds.", timeDiff / 1000, maxTimeDiffSeconds);
        }
    }
}