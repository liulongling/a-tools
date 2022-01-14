package com.liulongling.tool.execute;

import com.liulongling.tool.model.Configuration;
import com.liulongling.tool.model.DataBaseConfig;
import com.liulongling.tool.model.SqlModule;
import com.liulongling.tool.model.TableSqlModel;
import com.liulongling.tool.process.SqlModelProcess;
import com.liulongling.tool.utils.DateUtils;
import com.liulongling.tool.utils.ToolFileUtils;
import com.liulongling.tool.utils.SqlUtil;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.xwintop.xcore.util.javafx.TooltipUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 生成可执行的sql脚本
 * <p>
 * 存放文件夹层次：
 * 一层 项目 _ 版本_维护日期
 * 二层 平台
 * 三层 IP
 * 样例：
 * Game_1.8_20180731
 * ├── bili
 * │ ├── 192.168.0.2
 * │ │ ├── udb_10.sql
 * │ │ └── udb_20.sql
 * │ ├── 192.168.0.4
 * │ │ ├── udb_30.sql
 * │ │ └── udb_40.sql
 * ├── uo
 * │ ├── 192.168.1.2
 * │ │ ├── udb_10.sql
 * │ │ └── udb_20.sql
 * │ ├── 192.168.1.4
 * │ ├── udb_30.sql
 * │ └── udb_40.sql
 *
 * @Author: liulongling
 * @Date: 2021/7/23 16:42
 */
@Slf4j
public class SqlScriptExecute extends AbstractExecute {
    public SqlScriptExecute(Configuration configuration) {
        super(configuration);
    }

    @Override
    public void execute() {
        DataBaseConfig dataBaseConfig = config.getDataBaseConfig();
        String plat = dataBaseConfig.getPlat();
        log.info("开始创建平台{}->sql", plat);
        try {
            //准备sql
            SqlModule dataModule = new SqlModelProcess(config).process();

            dataModule.getMap().forEach((key, value) -> {
                String ip = key;
                value.forEach(
                        item -> {
                            if (!item.getList().isEmpty()) {
                                //创建文件夹、文件
                                File file = createSqlFile(ip, item);
                                //写入到文件
                                try {
                                    writeToFile(file, item);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                //数据库执行sql文件
                                if (config.isRun()) {
                                    runSql(ip, item);
                                }
                            }
                        }
                );
            });

        } catch (Exception e) {
            e.printStackTrace();
            TooltipUtil.showToast(e.getMessage());
        }

        log.info("创建平台{}->sql 结束", plat);
    }

    /**
     * 运行sql
     *
     * @param tableSqlModel
     */
    private void runSql(String ip, final TableSqlModel tableSqlModel) {
        try {
            DruidPooledConnection connection = SqlUtil.getDruidDataSource(
                    ip,
                    config.getDataBaseConfig().getPort(),
                    config.getDataBaseConfig().getUsername(),
                    config.getDataBaseConfig().getPassword(),
                    tableSqlModel.getUdbName());

            tableSqlModel.getList().forEach(
                    sql -> {
                        try {
                            PreparedStatement statement = connection.prepareStatement(sql);
                            statement.execute();
                        } catch (SQLException throwables) {
                            throwables.printStackTrace();
                            log.error("数据库：{} 执行sql：{} 失败!", tableSqlModel.getUdbName(), sql);
                        }
                    });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建sql文件目录和存放文件
     *
     * @param ip            数据库ip
     * @param tableSqlModel sql文件详细信息
     * @throws IOException
     */
    public File createSqlFile(String ip, TableSqlModel tableSqlModel) {
        String dirName = this.config.getDataBaseConfig().getGame() + "_" + DateUtils.getSqlDate();

        StringBuilder builder = new StringBuilder();

        //一层:项目_版本_维护日期
        builder.append(this.config.getDbTargetPath());
        builder.append(File.separator);
        builder.append(dirName);
        ToolFileUtils.createNewdir(builder.toString());

        //二层 平台
        builder.append(File.separator);
        builder.append(this.config.getDataBaseConfig().getPlat());
        ToolFileUtils.createNewdir(builder.toString());

        //三层 IP
        builder.append(File.separator);
        builder.append(ip);
        ToolFileUtils.createNewdir(builder.toString());

        //创建sql文件
        builder.append(File.separator);
        builder.append(getFileName(tableSqlModel));

        return ToolFileUtils.createNewFile(builder.toString());
    }

    /**
     * 执行后sql写入到文件
     *
     * @param file
     * @param tableSqlModel
     * @throws IOException
     */
    public void writeToFile(File file, final TableSqlModel tableSqlModel) throws IOException {
        FileOutputStream fos = new FileOutputStream(file, true);
        //指定以UTF-8格式写入文件
        OutputStreamWriter osw = new OutputStreamWriter(fos, ToolFileUtils.DEFAULT_CHARSET);

//        osw.write("create database " + tableSqlModel.getUdbName() + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;");
//        osw.write("\r\n");

        osw.write("use " + tableSqlModel.getUdbName() + ";");
        osw.write("\r\n");

        if (!StringUtils.isEmpty(config.getParameterValue())) {
            osw.write("START TRANSACTION;");
            osw.write("\r\n");
        }

        try {
            // 按行读取字符串
            tableSqlModel.getList().forEach(item ->
                    {
                        try {
                            log.info("udbName: {} sql: {}", tableSqlModel.getUdbName(), item);
                            osw.write(item);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
            );
            //写入完成关闭流
            if (!StringUtils.isEmpty(config.getParameterValue())) {
                osw.write("COMMIT;");
            }
            osw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
