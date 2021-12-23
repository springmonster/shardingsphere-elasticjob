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

package org.apache.shardingsphere.elasticjob.lite.internal.listener;

import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;

import java.nio.charset.StandardCharsets;

/**
 * kuanghc1：！！！貌似找到了，这里在不停地接收zk的信息
 *
 * Job Listener.
 */
public abstract class AbstractJobListener implements CuratorCacheListener {
    
    @Override
    public final void event(final Type type, final ChildData oldData, final ChildData newData) {
        if (null == newData && null == oldData) {
            return;
        }
        String path = Type.NODE_DELETED == type ? oldData.getPath() : newData.getPath();
        byte[] data = Type.NODE_DELETED == type ? oldData.getData() : newData.getData();
        if (path.isEmpty()) {
            return;
        }
        dataChanged(path, type, null == data ? "" : new String(data, StandardCharsets.UTF_8));
    }
    
    protected abstract void dataChanged(String path, Type eventType, String data);

    /**
     * [INFO ] 2021-12-22 15:29:40,065 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.config.RescheduleListenerManager] khc CronSettingAndJobEventChangedJobListener dataChanged path: /kuanghc1-job/sharding/0/running , eventType: NODE_CREATED , data:
     * [INFO ] 2021-12-22 15:29:40,065 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.guarantee.GuaranteeListenerManager] khc CompletedNodeRemovedJobListener dataChanged path: /kuanghc1-job/sharding/0/running , eventType: NODE_CREATED , data:
     * [INFO ] 2021-12-22 15:29:40,065 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.failover.FailoverListenerManager] khc FailoverSettingsChangedJobListener dataChanged path: /kuanghc1-job/sharding/0/running , eventType: NODE_CREATED , data:
     * [INFO ] 2021-12-22 15:29:40,065 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.election.ElectionListenerManager] khc LeaderAbdicationJobListener dataChanged path: /kuanghc1-job/sharding/0/running , eventType: NODE_CREATED , data:
     * [INFO ] 2021-12-22 15:29:40,065 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.election.ElectionListenerManager] khc LeaderElectionJobListener dataChanged path: /kuanghc1-job/sharding/0/running , eventType: NODE_CREATED , data:
     * [INFO ] 2021-12-22 15:29:40,090 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.sharding.MonitorExecutionListenerManager] khc MonitorExecutionSettingsChangedJobListener dataChanged path: /kuanghc1-job/sharding/0/running , eventType: NODE_CREATED , data:
     * [INFO ] 2021-12-22 15:29:40,090 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.sharding.ShardingListenerManager] khc ListenServersChangedJobListener dataChanged path: /kuanghc1-job/sharding/0/running , eventType: NODE_CREATED , data:
     * [INFO ] 2021-12-22 15:29:40,091 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.failover.FailoverListenerManager] khc JobCrashedJobListener dataChanged path: /kuanghc1-job/sharding/0/running , eventType: NODE_CREATED , data:
     * [INFO ] 2021-12-22 15:29:40,096 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.trigger.TriggerListenerManager] khc JobTriggerStatusJobListener dataChanged path: /kuanghc1-job/sharding/0/running , eventType: NODE_CREATED , data:
     * [INFO ] 2021-12-22 15:29:40,096 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.guarantee.GuaranteeListenerManager] khc StartedNodeRemovedJobListener dataChanged path: /kuanghc1-job/sharding/0/running , eventType: NODE_CREATED , data:
     * [INFO ] 2021-12-22 15:29:40,096 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.sharding.ShardingListenerManager] khc ShardingTotalCountChangedJobListener dataChanged path: /kuanghc1-job/sharding/0/running , eventType: NODE_CREATED , data:
     * [INFO ] 2021-12-22 15:29:40,096 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.instance.ShutdownListenerManager] khc InstanceShutdownStatusJobListener dataChanged path: /kuanghc1-job/sharding/0/running , eventType: NODE_CREATED , data:
     *
     * [INFO ] 2021-12-22 15:29:40,096 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.config.RescheduleListenerManager] khc CronSettingAndJobEventChangedJobListener dataChanged path: /kuanghc1-job/sharding/1/running , eventType: NODE_CREATED , data:
     * [INFO ] 2021-12-22 15:29:40,096 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.guarantee.GuaranteeListenerManager] khc CompletedNodeRemovedJobListener dataChanged path: /kuanghc1-job/sharding/1/running , eventType: NODE_CREATED , data:
     * [INFO ] 2021-12-22 15:29:40,096 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.failover.FailoverListenerManager] khc FailoverSettingsChangedJobListener dataChanged path: /kuanghc1-job/sharding/1/running , eventType: NODE_CREATED , data:
     * [INFO ] 2021-12-22 15:29:40,096 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.election.ElectionListenerManager] khc LeaderAbdicationJobListener dataChanged path: /kuanghc1-job/sharding/1/running , eventType: NODE_CREATED , data:
     * [INFO ] 2021-12-22 15:29:40,096 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.election.ElectionListenerManager] khc LeaderElectionJobListener dataChanged path: /kuanghc1-job/sharding/1/running , eventType: NODE_CREATED , data:
     * [INFO ] 2021-12-22 15:29:40,111 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.sharding.MonitorExecutionListenerManager] khc MonitorExecutionSettingsChangedJobListener dataChanged path: /kuanghc1-job/sharding/1/running , eventType: NODE_CREATED , data:
     * [INFO ] 2021-12-22 15:29:40,111 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.sharding.ShardingListenerManager] khc ListenServersChangedJobListener dataChanged path: /kuanghc1-job/sharding/1/running , eventType: NODE_CREATED , data:
     * [INFO ] 2021-12-22 15:29:40,111 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.failover.FailoverListenerManager] khc JobCrashedJobListener dataChanged path: /kuanghc1-job/sharding/1/running , eventType: NODE_CREATED , data:
     * [INFO ] 2021-12-22 15:29:40,122 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.trigger.TriggerListenerManager] khc JobTriggerStatusJobListener dataChanged path: /kuanghc1-job/sharding/1/running , eventType: NODE_CREATED , data:
     * [INFO ] 2021-12-22 15:29:40,122 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.guarantee.GuaranteeListenerManager] khc StartedNodeRemovedJobListener dataChanged path: /kuanghc1-job/sharding/1/running , eventType: NODE_CREATED , data:
     * [INFO ] 2021-12-22 15:29:40,122 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.sharding.ShardingListenerManager] khc ShardingTotalCountChangedJobListener dataChanged path: /kuanghc1-job/sharding/1/running , eventType: NODE_CREATED , data:
     * [INFO ] 2021-12-22 15:29:40,122 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.instance.ShutdownListenerManager] khc InstanceShutdownStatusJobListener dataChanged path: /kuanghc1-job/sharding/1/running , eventType: NODE_CREATED , data:
     *
     * [INFO ] 2021-12-22 15:29:40,122 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.config.RescheduleListenerManager] khc CronSettingAndJobEventChangedJobListener dataChanged path: /kuanghc1-job/sharding/2/running , eventType: NODE_CREATED , data:
     * [INFO ] 2021-12-22 15:29:40,122 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.guarantee.GuaranteeListenerManager] khc CompletedNodeRemovedJobListener dataChanged path: /kuanghc1-job/sharding/2/running , eventType: NODE_CREATED , data:
     * [INFO ] 2021-12-22 15:29:40,122 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.failover.FailoverListenerManager] khc FailoverSettingsChangedJobListener dataChanged path: /kuanghc1-job/sharding/2/running , eventType: NODE_CREATED , data:
     * [INFO ] 2021-12-22 15:29:40,122 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.election.ElectionListenerManager] khc LeaderAbdicationJobListener dataChanged path: /kuanghc1-job/sharding/2/running , eventType: NODE_CREATED , data:
     * [INFO ] 2021-12-22 15:29:40,122 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.election.ElectionListenerManager] khc LeaderElectionJobListener dataChanged path: /kuanghc1-job/sharding/2/running , eventType: NODE_CREATED , data:
     * [INFO ] 2021-12-22 15:29:40,123 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.sharding.MonitorExecutionListenerManager] khc MonitorExecutionSettingsChangedJobListener dataChanged path: /kuanghc1-job/sharding/2/running , eventType: NODE_CREATED , data:
     * [INFO ] 2021-12-22 15:29:40,123 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.sharding.ShardingListenerManager] khc ListenServersChangedJobListener dataChanged path: /kuanghc1-job/sharding/2/running , eventType: NODE_CREATED , data:
     * [INFO ] 2021-12-22 15:29:40,123 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.failover.FailoverListenerManager] khc JobCrashedJobListener dataChanged path: /kuanghc1-job/sharding/2/running , eventType: NODE_CREATED , data:
     * [INFO ] 2021-12-22 15:29:40,126 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.trigger.TriggerListenerManager] khc JobTriggerStatusJobListener dataChanged path: /kuanghc1-job/sharding/2/running , eventType: NODE_CREATED , data:
     * [INFO ] 2021-12-22 15:29:40,126 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.guarantee.GuaranteeListenerManager] khc StartedNodeRemovedJobListener dataChanged path: /kuanghc1-job/sharding/2/running , eventType: NODE_CREATED , data:
     * [INFO ] 2021-12-22 15:29:40,126 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.sharding.ShardingListenerManager] khc ShardingTotalCountChangedJobListener dataChanged path: /kuanghc1-job/sharding/2/running , eventType: NODE_CREATED , data:
     * [INFO ] 2021-12-22 15:29:40,126 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.instance.ShutdownListenerManager] khc InstanceShutdownStatusJobListener dataChanged path: /kuanghc1-job/sharding/2/running , eventType: NODE_CREATED , data:
     *
     * [INFO ] 2021-12-22 15:29:40,142 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.config.RescheduleListenerManager] khc CronSettingAndJobEventChangedJobListener dataChanged path: /kuanghc1-job/sharding/0/running , eventType: NODE_DELETED , data:
     * [INFO ] 2021-12-22 15:29:40,143 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.guarantee.GuaranteeListenerManager] khc CompletedNodeRemovedJobListener dataChanged path: /kuanghc1-job/sharding/0/running , eventType: NODE_DELETED , data:
     * [INFO ] 2021-12-22 15:29:40,145 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.failover.FailoverListenerManager] khc FailoverSettingsChangedJobListener dataChanged path: /kuanghc1-job/sharding/0/running , eventType: NODE_DELETED , data:
     * [INFO ] 2021-12-22 15:29:40,145 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.election.ElectionListenerManager] khc LeaderAbdicationJobListener dataChanged path: /kuanghc1-job/sharding/0/running , eventType: NODE_DELETED , data:
     * [INFO ] 2021-12-22 15:29:40,145 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.election.ElectionListenerManager] khc LeaderElectionJobListener dataChanged path: /kuanghc1-job/sharding/0/running , eventType: NODE_DELETED , data:
     * [INFO ] 2021-12-22 15:29:40,169 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.sharding.MonitorExecutionListenerManager] khc MonitorExecutionSettingsChangedJobListener dataChanged path: /kuanghc1-job/sharding/0/running , eventType: NODE_DELETED , data:
     * [INFO ] 2021-12-22 15:29:40,169 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.sharding.ShardingListenerManager] khc ListenServersChangedJobListener dataChanged path: /kuanghc1-job/sharding/0/running , eventType: NODE_DELETED , data:
     * [INFO ] 2021-12-22 15:29:40,170 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.failover.FailoverListenerManager] khc JobCrashedJobListener dataChanged path: /kuanghc1-job/sharding/0/running , eventType: NODE_DELETED , data:
     * [INFO ] 2021-12-22 15:29:40,175 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.trigger.TriggerListenerManager] khc JobTriggerStatusJobListener dataChanged path: /kuanghc1-job/sharding/0/running , eventType: NODE_DELETED , data:
     * [INFO ] 2021-12-22 15:29:40,175 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.guarantee.GuaranteeListenerManager] khc StartedNodeRemovedJobListener dataChanged path: /kuanghc1-job/sharding/0/running , eventType: NODE_DELETED , data:
     * [INFO ] 2021-12-22 15:29:40,175 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.sharding.ShardingListenerManager] khc ShardingTotalCountChangedJobListener dataChanged path: /kuanghc1-job/sharding/0/running , eventType: NODE_DELETED , data:
     * [INFO ] 2021-12-22 15:29:40,175 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.instance.ShutdownListenerManager] khc InstanceShutdownStatusJobListener dataChanged path: /kuanghc1-job/sharding/0/running , eventType: NODE_DELETED , data:
     *
     * [INFO ] 2021-12-22 15:29:40,175 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.config.RescheduleListenerManager] khc CronSettingAndJobEventChangedJobListener dataChanged path: /kuanghc1-job/sharding/1/running , eventType: NODE_DELETED , data:
     * [INFO ] 2021-12-22 15:29:40,175 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.guarantee.GuaranteeListenerManager] khc CompletedNodeRemovedJobListener dataChanged path: /kuanghc1-job/sharding/1/running , eventType: NODE_DELETED , data:
     * [INFO ] 2021-12-22 15:29:40,175 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.failover.FailoverListenerManager] khc FailoverSettingsChangedJobListener dataChanged path: /kuanghc1-job/sharding/1/running , eventType: NODE_DELETED , data:
     * [INFO ] 2021-12-22 15:29:40,175 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.election.ElectionListenerManager] khc LeaderAbdicationJobListener dataChanged path: /kuanghc1-job/sharding/1/running , eventType: NODE_DELETED , data:
     * [INFO ] 2021-12-22 15:29:40,175 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.election.ElectionListenerManager] khc LeaderElectionJobListener dataChanged path: /kuanghc1-job/sharding/1/running , eventType: NODE_DELETED , data:
     * [INFO ] 2021-12-22 15:29:40,190 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.sharding.MonitorExecutionListenerManager] khc MonitorExecutionSettingsChangedJobListener dataChanged path: /kuanghc1-job/sharding/1/running , eventType: NODE_DELETED , data:
     * [INFO ] 2021-12-22 15:29:40,190 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.sharding.ShardingListenerManager] khc ListenServersChangedJobListener dataChanged path: /kuanghc1-job/sharding/1/running , eventType: NODE_DELETED , data:
     * [INFO ] 2021-12-22 15:29:40,191 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.failover.FailoverListenerManager] khc JobCrashedJobListener dataChanged path: /kuanghc1-job/sharding/1/running , eventType: NODE_DELETED , data:
     * [INFO ] 2021-12-22 15:29:40,197 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.trigger.TriggerListenerManager] khc JobTriggerStatusJobListener dataChanged path: /kuanghc1-job/sharding/1/running , eventType: NODE_DELETED , data:
     * [INFO ] 2021-12-22 15:29:40,197 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.guarantee.GuaranteeListenerManager] khc StartedNodeRemovedJobListener dataChanged path: /kuanghc1-job/sharding/1/running , eventType: NODE_DELETED , data:
     * [INFO ] 2021-12-22 15:29:40,197 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.sharding.ShardingListenerManager] khc ShardingTotalCountChangedJobListener dataChanged path: /kuanghc1-job/sharding/1/running , eventType: NODE_DELETED , data:
     * [INFO ] 2021-12-22 15:29:40,197 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.instance.ShutdownListenerManager] khc InstanceShutdownStatusJobListener dataChanged path: /kuanghc1-job/sharding/1/running , eventType: NODE_DELETED , data:
     *
     * [INFO ] 2021-12-22 15:29:40,197 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.config.RescheduleListenerManager] khc CronSettingAndJobEventChangedJobListener dataChanged path: /kuanghc1-job/sharding/2/running , eventType: NODE_DELETED , data:
     * [INFO ] 2021-12-22 15:29:40,197 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.guarantee.GuaranteeListenerManager] khc CompletedNodeRemovedJobListener dataChanged path: /kuanghc1-job/sharding/2/running , eventType: NODE_DELETED , data:
     * [INFO ] 2021-12-22 15:29:40,197 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.failover.FailoverListenerManager] khc FailoverSettingsChangedJobListener dataChanged path: /kuanghc1-job/sharding/2/running , eventType: NODE_DELETED , data:
     * [INFO ] 2021-12-22 15:29:40,197 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.election.ElectionListenerManager] khc LeaderAbdicationJobListener dataChanged path: /kuanghc1-job/sharding/2/running , eventType: NODE_DELETED , data:
     * [INFO ] 2021-12-22 15:29:40,197 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.election.ElectionListenerManager] khc LeaderElectionJobListener dataChanged path: /kuanghc1-job/sharding/2/running , eventType: NODE_DELETED , data:
     * [INFO ] 2021-12-22 15:29:40,200 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.sharding.MonitorExecutionListenerManager] khc MonitorExecutionSettingsChangedJobListener dataChanged path: /kuanghc1-job/sharding/2/running , eventType: NODE_DELETED , data:
     * [INFO ] 2021-12-22 15:29:40,200 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.sharding.ShardingListenerManager] khc ListenServersChangedJobListener dataChanged path: /kuanghc1-job/sharding/2/running , eventType: NODE_DELETED , data:
     * [INFO ] 2021-12-22 15:29:40,201 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.failover.FailoverListenerManager] khc JobCrashedJobListener dataChanged path: /kuanghc1-job/sharding/2/running , eventType: NODE_DELETED , data:
     * [INFO ] 2021-12-22 15:29:40,201 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.trigger.TriggerListenerManager] khc JobTriggerStatusJobListener dataChanged path: /kuanghc1-job/sharding/2/running , eventType: NODE_DELETED , data:
     * [INFO ] 2021-12-22 15:29:40,202 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.guarantee.GuaranteeListenerManager] khc StartedNodeRemovedJobListener dataChanged path: /kuanghc1-job/sharding/2/running , eventType: NODE_DELETED , data:
     * [INFO ] 2021-12-22 15:29:40,202 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.sharding.ShardingListenerManager] khc ShardingTotalCountChangedJobListener dataChanged path: /kuanghc1-job/sharding/2/running , eventType: NODE_DELETED , data:
     * [INFO ] 2021-12-22 15:29:40,202 --Curator-SafeNotifyService-0-- [org.apache.shardingsphere.elasticjob.lite.internal.instance.ShutdownListenerManager] khc InstanceShutdownStatusJobListener dataChanged path: /kuanghc1-job/sharding/2/running , eventType: NODE_DELETED , data:
     *
     */
}