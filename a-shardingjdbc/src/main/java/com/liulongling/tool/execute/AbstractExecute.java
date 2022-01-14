package com.liulongling.tool.execute;

import cn.hutool.core.lang.Assert;
import com.liulongling.tool.model.Configuration;
import com.liulongling.tool.model.TableSqlModel;

/**
 * @Author: liulongling
 * @Date: 2021/7/23 16:32
 */
public abstract class AbstractExecute implements Execute {
    protected Configuration config;

    public AbstractExecute(Configuration config) {
        Assert.notNull(config, "Configuration can not be empty!");
        this.config = config;
    }

    public String getFileName(TableSqlModel tableSqlModel) {
        String fileName = "";
        //只有一个ip数据库所有sql存放到一个.sql文件
        if (this.config.getDataBaseConfig().getIps().size() == 1 && !this.config.getDataBaseConfig().isProdEnv()) {
            fileName = this.config.getDataBaseConfig().getDbName() + ".sql";
        } else {
            fileName = tableSqlModel.getUdbName() + ".sql";
        }
        return fileName;
    }

}
