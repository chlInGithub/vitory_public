package com.chl.victory.dao.manager.router;

/**
 * 动态数据源设置
 */
public class DynamicDataSourceHolder {

    private static final ThreadLocal<String> DATASOURCE_HOLDER = new ThreadLocal<>();

    public static void setDataSource(String dataSourceType) {
        DATASOURCE_HOLDER.set(dataSourceType);
    }

    public static String getDataSource() {
        return DATASOURCE_HOLDER.get();
    }

    public static void clearDataSource() {
        DATASOURCE_HOLDER.remove();
    }
}
