package com.chl.victory.dao.manager.router;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.stereotype.Component;

/**
 * 动态数据源处理
 *
 */
//@Component
@Primary
//@ConfigurationProperties(prefix = "dynamicDatasource")
public class DynamicDataSource extends AbstractRoutingDataSource implements ApplicationContextAware {
    private ApplicationContext applicationContext;
    private String defaultDataSource;

    @Override
    public void afterPropertiesSet() {
        Map<String, javax.sql.DataSource> dataSources = applicationContext.getBeansOfType(javax.sql.DataSource.class);
        if (dataSources.size() == 0) {
            throw new IllegalStateException("Datasource can not found!!!");
        }
        Map<Object, Object> targetDataSources = new HashMap<>(16);
        Iterator<String> keys = dataSources.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            // 去掉当前的DataSource
            if (!(dataSources.get(key) instanceof DynamicDataSource)) {
                targetDataSources.put(key, dataSources.get(key));
            }
        }
        setTargetDataSources(targetDataSources);

        // 默认数据源设置
        setDefaultTargetDataSource(targetDataSources.get(getDefaultDataSource()));

        super.afterPropertiesSet();
    }

    /**
     * {@link AbstractRoutingDataSource#determineTargetDataSource}
     *
     * @return datasource name
     */
    @Override
    protected Object determineCurrentLookupKey() {
        return DynamicDataSourceHolder.getDataSource();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public String getDefaultDataSource() {
        return defaultDataSource;
    }

    public void setDefaultDataSource(String defaultDataSource) {
        this.defaultDataSource = defaultDataSource;
    }
}
