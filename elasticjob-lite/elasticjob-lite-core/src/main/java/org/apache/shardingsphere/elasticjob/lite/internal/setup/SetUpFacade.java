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

package org.apache.shardingsphere.elasticjob.lite.internal.setup;

import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.infra.listener.ElasticJobListener;
import org.apache.shardingsphere.elasticjob.lite.internal.config.ConfigurationService;
import org.apache.shardingsphere.elasticjob.lite.internal.election.LeaderService;
import org.apache.shardingsphere.elasticjob.lite.internal.instance.InstanceService;
import org.apache.shardingsphere.elasticjob.lite.internal.listener.ListenerManager;
import org.apache.shardingsphere.elasticjob.lite.internal.reconcile.ReconcileService;
import org.apache.shardingsphere.elasticjob.lite.internal.server.ServerService;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;

import java.util.Collection;

/**
 * Set up facade.
 */
public final class SetUpFacade {
    
    private final ConfigurationService configService;
    
    private final LeaderService leaderService;
    
    private final ServerService serverService;
    
    private final InstanceService instanceService;
    
    private final ReconcileService reconcileService;
    
    private final ListenerManager listenerManager;

    /**
     * configService = {ConfigurationService@3009}
     *  timeService = {TimeService@3017}
     *  jobNodeStorage = {JobNodeStorage@3018}
     *   regCenter = {ZookeeperRegistryCenter@2936}
     *   jobName = "kuanghc1-job"
     *   jobNodePath = {JobNodePath@3019}
     *    jobName = "kuanghc1-job"
     *
     * leaderService = {LeaderService@3011}
     *  jobName = "kuanghc1-job"
     *  serverService = {ServerService@3026}
     *   jobName = "kuanghc1-job"
     *   jobNodeStorage = {JobNodeStorage@3028}
     *    regCenter = {ZookeeperRegistryCenter@2936}
     *    jobName = "kuanghc1-job"
     *    jobNodePath = {JobNodePath@3030}
     *     jobName = "kuanghc1-job"
     *   serverNode = {ServerNode@3029}
     *    jobName = "kuanghc1-job"
     *    jobNodePath = {JobNodePath@3031}
     *     jobName = "kuanghc1-job"
     *  jobNodeStorage = {JobNodeStorage@3027}
     *   regCenter = {ZookeeperRegistryCenter@2936}
     *   jobName = "kuanghc1-job"
     *   jobNodePath = {JobNodePath@3032}
     *    jobName = "kuanghc1-job"
     *
     * serverService = {ServerService@3012}
     *  jobName = "kuanghc1-job"
     *  jobNodeStorage = {JobNodeStorage@3033}
     *  serverNode = {ServerNode@3034}
     *
     *  instanceService = {InstanceService@3013}
     *  jobNodeStorage = {JobNodeStorage@3035}
     *  instanceNode = {InstanceNode@3036}
     *  triggerNode = {TriggerNode@3037}
     *  serverService = {ServerService@3038}
     *
     *  reconcileService = {ReconcileService@3014} "ReconcileService [NEW]"
     *  lastReconcileTime = 1640152767612
     *  configService = {ConfigurationService@3047}
     *  shardingService = {ShardingService@3048}
     *  jobNodePath = {JobNodePath@3049}
     *  regCenter = {ZookeeperRegistryCenter@2936}
     *  delegate = {AbstractScheduledService$ServiceDelegate@3050} "ReconcileService [NEW]"
     *
     *  listenerManager = {ListenerManager@3015}
     *  jobNodeStorage = {JobNodeStorage@3024}
     *  electionListenerManager = {ElectionListenerManager@3025}
     *  shardingListenerManager = {ShardingListenerManager@3026}
     *  failoverListenerManager = {FailoverListenerManager@3027}
     *  monitorExecutionListenerManager = {MonitorExecutionListenerManager@3028}
     *  shutdownListenerManager = {ShutdownListenerManager@3029}
     *  triggerListenerManager = {TriggerListenerManager@3030}
     *  rescheduleListenerManager = {RescheduleListenerManager@3031}
     *  guaranteeListenerManager = {GuaranteeListenerManager@3032}
     *  regCenterConnectionStateListener = {RegistryCenterConnectionStateListener@3033}
     *
     * @param regCenter
     * @param jobName
     * @param elasticJobListeners
     */
    public SetUpFacade(final CoordinatorRegistryCenter regCenter, final String jobName, final Collection<ElasticJobListener> elasticJobListeners) {
        configService = new ConfigurationService(regCenter, jobName);
        leaderService = new LeaderService(regCenter, jobName);
        serverService = new ServerService(regCenter, jobName);
        instanceService = new InstanceService(regCenter, jobName);
        reconcileService = new ReconcileService(regCenter, jobName);
        listenerManager = new ListenerManager(regCenter, jobName, elasticJobListeners);
    }
    
    /**
     * Set up job configuration.
     *
     * this.jobConfiguration = {JobConfiguration@3354}
     *  jobName = "kuanghc1-job"
     *  cron = "0/5 * * * * ?"
     *  timeZone = null
     *  shardingTotalCount = 3
     *  shardingItemParameters = "0=Beijing,1=Shanghai,2=Shenzhen"
     *  jobParameter = ""
     *  monitorExecution = true
     *  failover = false
     *  misfire = true
     *  maxTimeDiffSeconds = -1
     *  reconcileIntervalMinutes = 10
     *  jobShardingStrategyType = null
     *  jobExecutorServiceHandlerType = null
     *  jobErrorHandlerType = null
     *  jobListenerTypes = {ArrayList@3359}  size = 0
     *  extraConfigurations = {LinkedList@3360}  size = 1
     *  description = ""
     *  props = {Properties@3362}  size = 0
     *  disabled = false
     *  overwrite = false
     *  label = null
     *  staticSharding = false
     *
     * @param jobClassName job class name
     * @param jobConfig job configuration to be updated
     * @return accepted job configuration
     */
    public JobConfiguration setUpJobConfiguration(final String jobClassName, final JobConfiguration jobConfig) {
        return configService.setUpJobConfiguration(jobClassName, jobConfig);
    }
    
    /**
     * Register start up info.
     * 
     * @param enabled enable job on startup
     */
    public void registerStartUpInfo(final boolean enabled) {
        listenerManager.startAllListeners();
        leaderService.electLeader();
        serverService.persistOnline(enabled);
        instanceService.persistOnline();
        if (!reconcileService.isRunning()) {
            reconcileService.startAsync();
        }
    }
    
    /**
     * Tear down.
     */
    public void tearDown() {
        if (reconcileService.isRunning()) {
            reconcileService.stopAsync();
        }
    }
}