package com.liulongling.tool.services;

import cn.hutool.core.lang.Assert;
import com.liulongling.tool.controller.ShardingJdbcController;
import com.liulongling.tool.execute.SqlScriptExecute;
import com.liulongling.tool.model.Configuration;
import com.liulongling.tool.model.DataBaseConfig;
import com.liulongling.tool.model.ShardingJdbcTableBean;
import com.alibaba.fastjson.JSON;
import com.liulongling.tool.utils.ToolFileUtils;
import com.xwintop.xcore.util.ConfigureUtil;
import com.xwintop.xcore.util.javafx.FileChooserUtil;
import com.xwintop.xcore.util.javafx.TooltipUtil;
import javafx.collections.ObservableList;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: liulongling
 * @Date: 2021/7/13 16:40
 */
@Data
@Slf4j
public class ShardingJdbcService {

    private ShardingJdbcController shardingJdbcController;
    private ObservableList<ShardingJdbcTableBean> tableData;

    public ShardingJdbcService(ShardingJdbcController shardingJdbcController) {
        this.shardingJdbcController = shardingJdbcController;
    }

    public void saveConfigure() throws Exception {
        saveConfigure(ConfigureUtil.getConfigureFile("shardingJdbcConfigure.properties"));
    }

    public void setTableData(ObservableList<ShardingJdbcTableBean> tableData) {
        this.tableData = tableData;
    }

    public void saveConfigure(File file) throws Exception {
        FileUtils.touch(file);
        PropertiesConfiguration xmlConfigure = new PropertiesConfiguration(file);
        xmlConfigure.clear();
        for (int i = 0; i < tableData.size(); i++) {
            xmlConfigure.setProperty("tableBean" + i, tableData.get(i).getPropertys());
        }
        xmlConfigure.save();
        TooltipUtil.showToast("保存配置成功,保存在：" + file.getPath());
    }

    public void otherSaveConfigureAction() throws Exception {
        String fileName = "shardingJdbcConfigure.properties";
        File file = FileChooserUtil.chooseSaveFile(fileName, new FileChooser.ExtensionFilter("All File", "*.*"),
                new FileChooser.ExtensionFilter("Properties", "*.properties"));
        if (file != null) {
            saveConfigure(file);
            TooltipUtil.showToast("保存配置成功,保存在：" + file.getPath());
        }
    }

    public void loadingConfigure() {
        loadingConfigure(ConfigureUtil.getConfigureFile("shardingJdbcConfigure.properties"));
    }

    public void loadingConfigure(File file) {
        try {
            tableData.clear();
            PropertiesConfiguration xmlConfigure = new PropertiesConfiguration(file);
            xmlConfigure.getKeys().forEachRemaining(t -> tableData.add(new ShardingJdbcTableBean(xmlConfigure.getString(t))));
        } catch (Exception e) {
            TooltipUtil.showToast("加载配置失败：" + e.getMessage());
        }
    }

    public void loadingConfigureAction() {
        File file = FileChooserUtil.chooseFile(new FileChooser.ExtensionFilter("All File", "*.*"),
                new FileChooser.ExtensionFilter("Properties", "*.properties"));
        if (file != null) {
            loadingConfigure(file);
        }
    }

    public void makeSql(ShardingJdbcTableBean tableBean, TextArea textArea, boolean isRun) {
        Assert.notNull(textArea, "textArea can not be empty!");
        if (StringUtils.isEmpty(textArea.getText())) {
            return;
        }
        DataBaseConfig dataBaseConfig = loadLocalPluginConfiguration(tableBean.getDbOriginalPath());
        if (dataBaseConfig.isProdEnv() && isRun) {
            log.warn("生成环境无法执行sql,请选择生成sql文件手动执行");
            TooltipUtil.showToast("生成环境无法执行sql,请选择生成sql文件手动执行");
            return;
        }
        Configuration configuration = Configuration.builder()
                .dataBaseConfig(dataBaseConfig)
                .dbOriginalPath(tableBean.getDbOriginalPath())
                .dbTargetPath(tableBean.getDbTargetPath())
                .isRun(isRun)
                .sql(textArea.getText()).build();
        new SqlScriptExecute(configuration).execute();
    }

    public void makeuidSql(ShardingJdbcTableBean tableBean, TextArea textArea, boolean isRun, TextArea uidTextArea) {
        Assert.notNull(textArea, "textArea can not be empty!");
        if (StringUtils.isEmpty(textArea.getText())) {
            return;
        }
        DataBaseConfig dataBaseConfig = loadLocalPluginConfiguration(tableBean.getDbOriginalPath());
        if (dataBaseConfig.isProdEnv() && isRun) {
            log.warn("生成环境无法执行sql,请选择生成sql文件手动执行");
            TooltipUtil.showToast("生成环境无法执行sql,请选择生成sql文件手动执行");
            return;
        }
        Configuration configuration = Configuration.builder()
                .dataBaseConfig(dataBaseConfig)
                .dbOriginalPath(tableBean.getDbOriginalPath())
                .dbTargetPath(tableBean.getDbTargetPath())
                .isRun(isRun)
                .parameterValue(uidTextArea.getText())
                .sql(textArea.getText()).build();
        new SqlScriptExecute(configuration).execute();
    }

    /**
     * 根据ID查找所在数据库
     *
     * @param tableBean
     * @param textArea
     */
    public void findDBById(ShardingJdbcTableBean tableBean, TextArea textArea) {
        if (StringUtils.isEmpty(textArea.getText())) {
            return;
        }
        DataBaseConfig shardingJdbc = loadLocalPluginConfiguration(tableBean.getDbOriginalPath());
        String[] ids = textArea.getText().split(",");

        int db = 0;
        int dbCount = shardingJdbc.getIps().size();
        Map<String, StringBuilder> map;
        for (int i = 0; i < dbCount; i++) {
            String ip;
            if (dbCount == 1) {
                ip = shardingJdbc.getIps().get(0);
            } else {
                ip = shardingJdbc.getIps().get(i);
            }
            String[] tables = getTables("table", db, dbCount);
            for (String table : tables) {
                map = new HashMap<>();
                for (String id : ids) {
                    if (Integer.parseInt(id) % dbCount != db) {
                        continue;
                    }
                    if (table.equals(getTable("table", Integer.parseInt(id), dbCount))) {
                        StringBuilder builder = map.computeIfAbsent(table,
                                t -> new StringBuilder());
                        builder.append(Integer.parseInt(id)).append(",");
                    }
                }
                if (!map.isEmpty()) {
                    map.forEach((key, value) -> {
                        log.info("ids:[{}] --> ip:[{}] table:[{}]", value.toString(), ip, table);
                    });
                }
            }
            db++;
        }
    }

    /**
     * 从配置文件中加载本地插件信息
     */
    private DataBaseConfig loadLocalPluginConfiguration(String localDBPath) {
        DataBaseConfig dataBaseConfig = null;
        try {
            Path path = Paths.get(localDBPath);
            if (!Files.exists(path)) {
                return null;
            }

            String json = new String(Files.readAllBytes(path), ToolFileUtils.DEFAULT_CHARSET);
            dataBaseConfig = JSON.parseObject(json, DataBaseConfig.class);
        } catch (IOException e) {
            log.error("读取插件配置失败", e);
        }
        return dataBaseConfig;
    }


    /**
     * 查询数据库下所有表名
     *
     * @param table
     * @param db
     * @param dbCount
     * @return
     */
    public static String[] getTables(String table, int db, int dbCount) {
        String[] tables;
        List<String> set = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            if (i % dbCount == db) {
                int tableNum = i % 100;
                set.add(table + "_" + (tableNum < 10 ? "0" + tableNum : String.valueOf(tableNum)));
            }
        }
        tables = new String[set.size()];
        set.toArray(tables);
        return tables;
    }

    /**
     * 查询数据库下该角色指定的表名
     *
     * @param table
     * @param uid
     * @param dbCount
     * @return
     */
    public static String getTable(String table, int uid, int dbCount) {
        if (dbCount != 1) {
            int tableNum = uid % 100;
            return table + "_" + (tableNum < 10 ? "0" + tableNum : tableNum);
        }
        return null;
    }
}
