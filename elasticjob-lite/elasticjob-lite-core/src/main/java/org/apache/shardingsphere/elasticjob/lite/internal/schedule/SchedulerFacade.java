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

import org.apache.shardingsphere.elasticjob.lite.internal.election.LeaderService;
import org.apache.shardingsphere.elasticjob.lite.internal.sharding.ExecutionService;
import org.apache.shardingsphere.elasticjob.lite.internal.sharding.ShardingService;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;

/**
 * Scheduler facade.
 */
public final class SchedulerFacade {
    
    private final String jobName;
    
    private final LeaderService leaderService;
    
    private final ShardingService shardingService;
    
    private final ExecutionService executionService;

    /**
     * schedulerFacade = {SchedulerFacade@3372}
     *  jobName = "kuanghc1-job"
     *  leaderService = {LeaderService@3373}
     *   jobName = "kuanghc1-job"
     *   serverService = {ServerService@3376}
     *   jobNodeStorage = {JobNodeStorage@3377}
     *  shardingService = {ShardingService@3374}
     *   jobName = "kuanghc1-job"
     *   jobNodeStorage = {JobNodeStorage@3378}
     *   leaderService = {LeaderService@3379}
     *   configService = {ConfigurationService@3380}
     *   instanceService = {InstanceService@3381}
     *   instanceNode = {InstanceNode@3382}
     *   serverService = {ServerService@3383}
     *   executionService = {ExecutionService@3384}
     *   jobNodePath = {JobNodePath@3385}
     *  executionService = {ExecutionService@3375}
     *   jobName = "kuanghc1-job"
     *   jobNodeStorage = {JobNodeStorage@3386}
     *   configService = {ConfigurationService@3387}
     *
     * @param regCenter
     * @param jobName
     */
    public SchedulerFacade(final CoordinatorRegistryCenter regCenter, final String jobName) {
        this.jobName = jobName;
        leaderService = new LeaderService(regCenter, jobName);
        shardingService = new ShardingService(regCenter, jobName);
        executionService = new ExecutionService(regCenter, jobName);
    }
    
    /**
     * Create job trigger listener.
     *
     * @return job trigger listener
     */
    public JobTriggerListener newJobTriggerListener() {
        return new JobTriggerListener(executionService, shardingService);
    }
    
    /**
     * Shutdown instance.
     */
    public void shutdownInstance() {
        if (leaderService.isLeader()) {
            leaderService.removeLeader();
        }
        JobRegistry.getInstance().shutdown(jobName);
    }
}