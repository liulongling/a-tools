package com.liulongling.tool.process;


import com.liulongling.tool.exception.ToolException;
import com.liulongling.tool.model.Configuration;
import com.liulongling.tool.model.DataBaseConfig;
import com.liulongling.tool.model.SqlModule;
import com.liulongling.tool.model.TableSqlModel;
import com.liulongling.tool.utils.ToolStringUtils;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.alibaba.druid.util.JdbcConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @Author: liulongling
 * @Date: 2021/7/24 17:28
 */
@Slf4j
public class SqlModelProcess extends AbstractProcess {


    public SqlModelProcess(Configuration configuration) {
        super(configuration);
    }

    @Override
    public SqlModule process() {
        SqlModule sqlModule = new SqlModule();
        Map<String, List<TableSqlModel>> map = new HashMap<>();
        DataBaseConfig dataBaseConfig = config.getDataBaseConfig();
        List<String> ips = dataBaseConfig.getIps();

        if (StringUtils.isEmpty(config.getSql())) {
            log.info("sql can not be empty!");
            return null;
        }

        int db = 0;

        for (int i = 0; i < config.getDataBaseConfig().getDbCount(); i++) {
            String ip;
            if (ips.size() < 10) {
                ip = ips.get(i % ips.size());
            } else {
                ip = ips.get(i);
            }

            TableSqlModel tableSqlModel;
            if (!StringUtils.isEmpty(config.getParameterValue())) {
                tableSqlModel = createTableSqlModelHaveParameter(db);
            } else {
                tableSqlModel = createTableSqlModel(db);
            }

            List<TableSqlModel> list = map.computeIfAbsent(ip,
                    id -> new ArrayList<>());
            list.add(tableSqlModel);
            map.put(ip, list);
            db++;
        }
        sqlModule.setMap(map);
        return sqlModule;
    }

    /**
     * ????????????????????????sql??????????????????update???select???delete
     * (??????????????????ID?????????where ???????????????????????????)
     *
     * @param db ?????????0~100
     * @return
     */
    private TableSqlModel createTableSqlModelHaveParameter(int db) {
        String[] parameterValues = config.getParameterValue().split("\\\n");

        TableSqlModel tableSqlModel = new TableSqlModel();
        tableSqlModel.setUdbName(config.getDataBaseConfig().getDbName() + "_" + (db < 10 ? "0" + db : db));

        String sql = config.getSql() + "\r\n";
        List<SQLStatement> sqlStatements = SQLUtils.parseStatements(sql, JdbcConstants.MYSQL);
        sqlStatements.forEach(sqlStatement -> {
            MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
            sqlStatement.accept(visitor);
            Map<TableStat.Name, TableStat> tables = visitor.getTables();
            Collection<TableStat.Column> columns = visitor.getColumns();
            List<TableStat.Condition> conditions = visitor.getConditions();
            //sql?????? where ????????????????????????????????????ID
            int findIdIndex = findIdIndex(conditions.get(0).getColumn().getName(), columns);
            Set<String> existValue = checkColumnsValuesIsNull(conditions);
            Set<TableStat.Name> tableNameSet = tables.keySet();
            String[] tableIndexs = getTableIndexs(db, config.getDataBaseConfig().getDbCount());
            for (String tableIndex : tableIndexs) {
                for (String parameterValue : parameterValues) {
                    String[] parameter = parameterValue.split(",");
                    int id = Integer.parseInt(parameter[findIdIndex]);
                    if (id % config.getDataBaseConfig().getDbCount() != db) {
                        continue;
                    }
                    String str = sql;
                    for (TableStat.Name name : tableNameSet) {
                        if(name.getName().indexOf("`") == -1){
                            throw new ToolException("???????????????'`'??????");
                        }
                        String sourceTable = filterTable(name.getName());
                        String replaceTable = filterTable(name.getName(), tableIndex);

                        String userTableName = filterTable(getTable(name.getName(), id, config.getDataBaseConfig().getDbCount()));
                        log.debug("{},{},{},{},{}", id, tableIndex, name.getName(), replaceTable, userTableName);
                        if (replaceTable.equals(userTableName)) {
                            str = StringUtils.replace(str, sourceTable, replaceTable);
                            Iterator iterator = columns.iterator();
                            while (iterator.hasNext()) {
                                //?????????????????????
                                TableStat.Column column = (TableStat.Column) iterator.next();
                                if (existValue.contains(column.getName())) {
                                    continue;
                                }
                                log.debug("before sql:{} name:{}", str, column.getName());
                                String str1 = ToolStringUtils.subString(str, column.getName(), "?");
                                log.debug("after sql:{}", str1);
                                String str2 = StringUtils.replace(str1, "?", parameter[findIdIndex(column.getName(), columns)]);
                                str = StringUtils.replace(str, str1, str2);
                                log.debug("name:{} p:{} p1:{}", column.getName(), str1, str2);
                            }
                            log.debug("end sql:{}", str);
                            tableSqlModel.getList().add(str);
                        }
                    }
                }
            }
        });
        return tableSqlModel;
    }

    private TableSqlModel createTableSqlModel(int db) {
        TableSqlModel tableSqlModel = new TableSqlModel();
        tableSqlModel.setUdbName(config.getDataBaseConfig().getDbName() + "_" + (db < 10 ? "0" + db : db));

        String[] sqlArr = config.getSql().split("\\\n");
        StringBuilder tableBuilder = new StringBuilder();
        for (String s : sqlArr) {
            tableBuilder.append(s);
            tableBuilder.append("\r\n");
            if (s.endsWith(";")) {
                String sql = tableBuilder.toString();
                String dbType = JdbcConstants.MYSQL;
                List<SQLStatement> sqlStatements = SQLUtils.parseStatements(sql, dbType);
                sqlStatements.forEach(sqlStatement -> {
                    MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
                    sqlStatement.accept(visitor);
                    Map<TableStat.Name, TableStat> tables = visitor.getTables();
                    Set<TableStat.Name> tableNameSet = tables.keySet();
                    //??????????????????????????????
                    String[] tableIndexs = getTableIndexs(db, config.getDataBaseConfig().getDbCount());
                    for (String tableIndex : tableIndexs) {
                        String str = sql;
                        for (TableStat.Name name : tableNameSet) {
                            if(name.getName().indexOf("`") == -1){
                                throw new ToolException("???????????????'`'??????");
                            }
                            String sourceTable = filterTable(name.getName(), null);
                            String replaceTable = filterTable(name.getName(), tableIndex);
                            str = StringUtils.replace(str, sourceTable, replaceTable);
                        }
                        tableSqlModel.getList().add(str);
                    }
                });
                tableBuilder = new StringBuilder();
            }
        }
        return tableSqlModel;
    }
}
