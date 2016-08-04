package com.enation.app.base.core.service.dbsolution;

import java.sql.Connection;

/**
 * 数据库导入导出解决方案接口
 *
 * @author panjn
 * @date 2016/7/30
 */
public interface IDBSolution {

    public boolean setConnection(Connection conn);

    /**
     * 导入一个xml文件到数据库中
     *
     * @param xml xml文件路径
     * @return
     */
    public boolean dbImport(String xml);

    public boolean dbExport(String[] tables, String xml);

    public String dbExport(String[] tables, boolean dataOnly);

    public int deleteTable(String table);

    public void setPrefix(String prefix);
}
