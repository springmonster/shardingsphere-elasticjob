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

package org.apache.shardingsphere.elasticjob.lite.lifecycle.domain;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Job brief info.
 */
@Getter
@Setter
public final class JobBriefInfo implements Serializable, Comparable<JobBriefInfo> {
    
    private static final long serialVersionUID = 8405751873086755148L;
    
    private String jobName;
    
    private JobStatus status;
    
    private String description;
    
    private String cron;
    
    private int instanceCount;
    
    private int shardingTotalCount;
    
    @Override
    public int compareTo(final JobBriefInfo o) {
        return getJobName().compareTo(o.getJobName());
    }
    
    /**
     * Job status.
     */
    public enum JobStatus {
        /**
         * OK status.
         */
        OK,

        /**
         * Crashed status.
         */
        CRASHED,

        /**
         * Disabled status.
         */
        DISABLED,

        /**
         * Sharding flag status.
         */
        SHARDING_FLAG
    }
}
