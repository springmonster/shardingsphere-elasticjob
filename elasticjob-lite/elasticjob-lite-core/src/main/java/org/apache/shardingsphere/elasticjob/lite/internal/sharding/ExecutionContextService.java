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

package org.apache.shardingsphere.elasticjob.lite.internal.sharding;

import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.infra.context.ShardingItemParameters;
import org.apache.shardingsphere.elasticjob.infra.handler.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.infra.listener.ShardingContexts;
import org.apache.shardingsphere.elasticjob.lite.internal.config.ConfigurationService;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Execution context service.
 */
public final class ExecutionContextService {

    private final String jobName;

    private final JobNodeStorage jobNodeStorage;

    private final ConfigurationService configService;

    public ExecutionContextService(final CoordinatorRegistryCenter regCenter, final String jobName) {
        this.jobName = jobName;
        jobNodeStorage = new JobNodeStorage(regCenter, jobName);
        configService = new ConfigurationService(regCenter, jobName);
    }

    /**
     * Get job sharding context.
     *
     * @param shardingItems sharding items
     * @return job sharding context
     */
    public ShardingContexts getJobShardingContext(final List<Integer> shardingItems) {
        // 需要从zk获取configuration的信息
        JobConfiguration jobConfiguration = configService.load(false);

        removeRunningIfMonitorExecution(jobConfiguration.isMonitorExecution(), shardingItems);

        // TODO: 2021/12/24  
        if (shardingItems.isEmpty()) {
            return new ShardingContexts(
                    buildTaskId(jobConfiguration, shardingItems),
                    jobConfiguration.getJobName(),
                    jobConfiguration.getShardingTotalCount(),
                    jobConfiguration.getJobParameter(),
                    Collections.emptyMap());
        }

        Map<Integer, String> shardingItemParameterMap = new ShardingItemParameters(jobConfiguration.getShardingItemParameters()).getMap();

        // TODO: 2021/12/24
        return new ShardingContexts(
//                kuanghc1-1224-3@-@0,1,2@-@READY@-@10.109.71.139@-@54196
                buildTaskId(jobConfiguration, shardingItems),
                jobConfiguration.getJobName(),
                jobConfiguration.getShardingTotalCount(),
                jobConfiguration.getJobParameter(),
                getAssignedShardingItemParameterMap(shardingItems, shardingItemParameterMap));
    }

    private String buildTaskId(final JobConfiguration jobConfig, final List<Integer> shardingItems) {
        /**
         * jobInstance = {JobInstance@5187}
         *  jobInstanceId = "10.109.71.139@-@50114"
         *  labels = null
         *  serverIp = "10.109.71.139" 这里是本机的IP
         */
        JobInstance jobInstance = JobRegistry.getInstance().getJobInstance(jobName);
        String shardingItemsString = shardingItems.stream().map(Object::toString).collect(Collectors.joining(","));
        String jobInstanceId = null == jobInstance || null == jobInstance.getJobInstanceId() ? "127.0.0.1@-@1" : jobInstance.getJobInstanceId();
        return String.join("@-@", jobConfig.getJobName(), shardingItemsString, "READY", jobInstanceId);
    }

    /**
     * 任务失效了，但是正在运行了，得从失效列表中移除
     *
     * @param monitorExecution
     * @param shardingItems
     */
    private void removeRunningIfMonitorExecution(final boolean monitorExecution, final List<Integer> shardingItems) {
        if (!monitorExecution) {
            return;
        }
        List<Integer> runningShardingItems = new ArrayList<>(shardingItems.size());
        for (int each : shardingItems) {
            if (isRunning(each)) {
                runningShardingItems.add(each);
            }
        }
        shardingItems.removeAll(runningShardingItems);
    }

    private boolean isRunning(final int shardingItem) {
        return jobNodeStorage.isJobNodeExisted(ShardingNode.getRunningNode(shardingItem));
    }

    private Map<Integer, String> getAssignedShardingItemParameterMap(final List<Integer> shardingItems, final Map<Integer, String> shardingItemParameterMap) {
        Map<Integer, String> result = new HashMap<>(shardingItems.size(), 1);
        for (int each : shardingItems) {
            result.put(each, shardingItemParameterMap.get(each));
        }
        return result;
    }
}