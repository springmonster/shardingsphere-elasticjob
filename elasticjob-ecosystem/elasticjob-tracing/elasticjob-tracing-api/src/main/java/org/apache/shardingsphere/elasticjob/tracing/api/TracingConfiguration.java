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

package org.apache.shardingsphere.elasticjob.tracing.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.elasticjob.api.JobExtraConfiguration;
import org.apache.shardingsphere.elasticjob.tracing.exception.TracingStorageConverterNotFoundException;
import org.apache.shardingsphere.elasticjob.tracing.storage.TracingStorageConverterFactory;

/**
 * Tracing configuration.
 *
 * @param <T> type of tracing storage
 */
@Getter
@RequiredArgsConstructor
public final class TracingConfiguration<T> implements JobExtraConfiguration {

    private final String type;

    private final TracingStorageConfiguration<T> tracingStorageConfiguration;

    /**
     * this.tracingStorageConfiguration = {DataSourceConfiguration@3595}
     * dataSourceClassName = "org.apache.commons.dbcp2.BasicDataSource"
     * props = {LinkedHashMap@3596}  size = 37
     * "logAbandoned" : {Boolean@3523} false
     * "numTestsPerEvictionRun" : {Integer@3525} 3
     * "url" : "jdbc:mysql://10.122.111.97:3306/elasticjob?characterEncoding=utf8&useSSL=false&serverTimezone=UTC&rewriteBatchedStatements=true"
     * "timeBetweenEvictionRunsMillis" : {Long@3529} -1
     * "enableAutoCommitOnReturn" : {Boolean@3531} true
     * "removeAbandonedTimeout" : {Integer@3533} 300
     * "lifo" : {Boolean@3535} true
     * "softMinEvictableIdleTimeMillis" : {Long@3537} -1
     * "driverClassName" : "com.mysql.cj.jdbc.Driver"
     * "rollbackOnReturn" : {Boolean@3541} true
     * "abandonedUsageTracking" : {Boolean@3543} false
     * "testOnBorrow" : {Boolean@3545} true
     * "testOnReturn" : {Boolean@3547} false
     * "numIdle" : {Integer@3549} 1
     * "maxIdle" : {Integer@3551} 8
     * "minIdle" : {Integer@3553} 0
     * "logExpiredConnections" : {Boolean@3555} true
     * "maxTotal" : {Integer@3557} 8
     * "numActive" : {Integer@3559} 0
     * "defaultTransactionIsolation" : {Integer@3561} -1
     * "testOnCreate" : {Boolean@3563} false
     * "removeAbandonedOnMaintenance" : {Boolean@3565} false
     * "fastFailValidation" : {Boolean@3567} false
     * "password" : "appadmin"
     * "cacheState" : {Boolean@3571} true
     * "initialSize" : {Integer@3573} 0
     * "maxWaitMillis" : {Long@3575} -1
     * "validationQueryTimeout" : {Integer@3577} -1
     * "evictionPolicyClassName" : "org.apache.commons.pool2.impl.DefaultEvictionPolicy"
     * "maxOpenPreparedStatements" : {Integer@3581} -1
     * "removeAbandonedOnBorrow" : {Boolean@3583} false
     * "connectionInitSqls" : {Collections$EmptyList@3585}  size = 0
     * "testWhileIdle" : {Boolean@3587} false
     * "autoCommitOnReturn" : {Boolean@3589} true
     * "minEvictableIdleTimeMillis" : {Long@3591} 1800000
     * "maxConnLifetimeMillis" : {Long@3593} -1
     * "username" : "appadmin"
     *
     * @param type
     * @param storage
     */
    @SuppressWarnings("unchecked")
    public TracingConfiguration(final String type, final T storage) {
        this.type = type;
        this.tracingStorageConfiguration = TracingStorageConverterFactory
                .findConverter((Class<T>) storage.getClass())
                .orElseThrow(() -> new TracingStorageConverterNotFoundException(storage.getClass()))
                .convertObjectToConfiguration(storage);
    }
}