package com.liulongling.tool.model;

import javafx.beans.property.SimpleStringProperty;
import lombok.Data;

@Data
public class ShardingJdbcTableBean {
    /**
     * 数据库数据源路径
     */
    private SimpleStringProperty dbOriginalPath;
    /**
     * sql存储路径
     */
    private SimpleStringProperty dbTargetPath;

    public ShardingJdbcTableBean(String dbOriginalPath, String dbTargetPath) {
        this.dbOriginalPath = new SimpleStringProperty(dbOriginalPath);
        this.dbTargetPath = new SimpleStringProperty(dbTargetPath);
    }

    public ShardingJdbcTableBean(String propertys) {
        String[] strings = propertys.split("__", 8);
        this.dbOriginalPath = new SimpleStringProperty(strings[0]);
        this.dbTargetPath = new SimpleStringProperty(strings[1]);
    }

    public String getPropertys() {
        return dbOriginalPath.get() + "__" + dbTargetPath.get();
    }

    public String getDbOriginalPath() {
        return dbOriginalPath.get();
    }

    public void setDbOriginalPath(String dbOriginalPath) {
        this.dbOriginalPath.set(dbOriginalPath);
    }

    public String getDbTargetPath() {
        return dbTargetPath.get();
    }

    public void setDbTargetPath(String dbTargetPath) {
        this.dbTargetPath.set(dbTargetPath);
    }
}
