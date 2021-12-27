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

package org.apache.shardingsphere.elasticjob.tracing.rdb.datasource;

import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.elasticjob.tracing.api.TracingStorageConfiguration;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;

/**
 * Data source configuration.
 */
@Getter
@RequiredArgsConstructor
public final class DataSourceConfiguration implements TracingStorageConfiguration<DataSource> {

    private static final String GETTER_PREFIX = "get";

    private static final String SETTER_PREFIX = "set";

    private static final Collection<Class<?>> GENERAL_CLASS_TYPE;

    private static final Collection<String> SKIPPED_PROPERTY_NAMES;

    static {
        GENERAL_CLASS_TYPE = Sets.newHashSet(boolean.class, Boolean.class, int.class, Integer.class, long.class, Long.class, String.class, Collection.class, List.class);
        SKIPPED_PROPERTY_NAMES = Sets.newHashSet("loginTimeout");
    }

    private final String dataSourceClassName;

    private final Map<String, Object> props = new LinkedHashMap<>();

    /**
     * result = {DataSourceConfiguration@3595}
     *  dataSourceClassName = "org.apache.commons.dbcp2.BasicDataSource"
     *  props = {LinkedHashMap@3596}  size = 37
     *   "logAbandoned" : {Boolean@3523} false
     *   "numTestsPerEvictionRun" : {Integer@3525} 3
     *   "url" : "jdbc:mysql://10.122.111.97:3306/elasticjob?characterEncoding=utf8&useSSL=false&serverTimezone=UTC&rewriteBatchedStatements=true"
     *   "timeBetweenEvictionRunsMillis" : {Long@3529} -1
     *   "enableAutoCommitOnReturn" : {Boolean@3531} true
     *   "removeAbandonedTimeout" : {Integer@3533} 300
     *   "lifo" : {Boolean@3535} true
     *   "softMinEvictableIdleTimeMillis" : {Long@3537} -1
     *   "driverClassName" : "com.mysql.cj.jdbc.Driver"
     *   "rollbackOnReturn" : {Boolean@3541} true
     *   "abandonedUsageTracking" : {Boolean@3543} false
     *   "testOnBorrow" : {Boolean@3545} true
     *   "testOnReturn" : {Boolean@3547} false
     *   "numIdle" : {Integer@3549} 1
     *   "maxIdle" : {Integer@3551} 8
     *   "minIdle" : {Integer@3553} 0
     *   "logExpiredConnections" : {Boolean@3555} true
     *   "maxTotal" : {Integer@3557} 8
     *   "numActive" : {Integer@3559} 0
     *   "defaultTransactionIsolation" : {Integer@3561} -1
     *   "testOnCreate" : {Boolean@3563} false
     *   "removeAbandonedOnMaintenance" : {Boolean@3565} false
     *   "fastFailValidation" : {Boolean@3567} false
     *   "password" : "appadmin"
     *   "cacheState" : {Boolean@3571} true
     *   "initialSize" : {Integer@3573} 0
     *   "maxWaitMillis" : {Long@3575} -1
     *   "validationQueryTimeout" : {Integer@3577} -1
     *   "evictionPolicyClassName" : "org.apache.commons.pool2.impl.DefaultEvictionPolicy"
     *   "maxOpenPreparedStatements" : {Integer@3581} -1
     *   "removeAbandonedOnBorrow" : {Boolean@3583} false
     *   "connectionInitSqls" : {Collections$EmptyList@3585}  size = 0
     *   "testWhileIdle" : {Boolean@3587} false
     *   "autoCommitOnReturn" : {Boolean@3589} true
     *   "minEvictableIdleTimeMillis" : {Long@3591} 1800000
     *   "maxConnLifetimeMillis" : {Long@3593} -1
     *   "username" : "appadmin"
     */
    /**
     * Get data source configuration.
     *
     * @param dataSource data source
     * @return data source configuration
     */
    public static DataSourceConfiguration getDataSourceConfiguration(final DataSource dataSource) {
        DataSourceConfiguration result = new DataSourceConfiguration(dataSource.getClass().getName());
        result.props.putAll(findAllGetterProperties(dataSource));
        return result;
    }

    @SneakyThrows(ReflectiveOperationException.class)
    private static Map<String, Object> findAllGetterProperties(final Object target) {
        Collection<Method> allGetterMethods = findAllGetterMethods(target.getClass());
        Map<String, Object> result = new LinkedHashMap<>(allGetterMethods.size(), 1);
        for (Method each : allGetterMethods) {
            String propertyName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, each.getName().substring(GETTER_PREFIX.length()));
            if (GENERAL_CLASS_TYPE.contains(each.getReturnType()) && !SKIPPED_PROPERTY_NAMES.contains(propertyName)) {
                Optional.ofNullable(each.invoke(target)).ifPresent(propertyValue -> result.put(propertyName, propertyValue));
            }
        }
        return result;
    }

    private static Collection<Method> findAllGetterMethods(final Class<?> clazz) {
        Method[] methods = clazz.getMethods();
        Collection<Method> result = new HashSet<>(methods.length);
        for (Method each : methods) {
            if (each.getName().startsWith(GETTER_PREFIX) && 0 == each.getParameterTypes().length) {
                result.add(each);
            }
        }
        return result;
    }

    @Override
    public DataSource getStorage() {
        return DataSourceRegistry.getInstance().getDataSource(this);
    }

    /**
     * Create data source.
     *
     * @return data source
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @SneakyThrows(ReflectiveOperationException.class)
    public DataSource createDataSource() {
        DataSource result = (DataSource) Class.forName(dataSourceClassName).getConstructor().newInstance();
        Method[] methods = result.getClass().getMethods();
        for (Entry<String, Object> entry : props.entrySet()) {
            if (SKIPPED_PROPERTY_NAMES.contains(entry.getKey())) {
                continue;
            }
            Optional<Method> setterMethod = findSetterMethod(methods, entry.getKey());
            if (setterMethod.isPresent()) {
                setterMethod.get().invoke(result, entry.getValue());
            }
        }
        Optional<JDBCParameterDecorator> decorator = findJDBCParameterDecorator(result);
        return decorator.isPresent() ? decorator.get().decorate(result) : result;
    }

    @SuppressWarnings("rawtypes")
    private Optional<JDBCParameterDecorator> findJDBCParameterDecorator(final DataSource dataSource) {
        for (JDBCParameterDecorator each : ServiceLoader.load(JDBCParameterDecorator.class)) {
            if (each.getType() == dataSource.getClass()) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }

    private Optional<Method> findSetterMethod(final Method[] methods, final String property) {
        String setterMethodName = Joiner.on("").join(SETTER_PREFIX, CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, property));
        for (Method each : methods) {
            if (each.getName().equals(setterMethodName) && 1 == each.getParameterTypes().length) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || null != obj && getClass() == obj.getClass() && equalsByProperties((DataSourceConfiguration) obj);
    }

    private boolean equalsByProperties(final DataSourceConfiguration dataSourceConfig) {
        return dataSourceClassName.equals(dataSourceConfig.dataSourceClassName) && props.equals(dataSourceConfig.props);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(dataSourceClassName, props);
    }
}