# 项目结构

```CSS
/a-tools/
├── config                                     #-- 不同环境配置文件
│ ├── game1                                    #-- 游戏一
│ ├── game2                                    #-- 游戏二
├── libs                                       #-- 不同工具jar包
│ ├── a-JExcel-0.0.1.jar                       #-- 策划表转json
│ ├── a-Shardingjdbc-0.0.1.jar                 #-- 分库分表相关功能(创建sql、查询玩家所在数据库)
├── logs                                       #-- 运行日志 
├── templates                                  #-- 模板
├── GameTools.exe                              #-- 启动服务
├── system_plugin_list.json                    #-- 管理不同工具版本
```



# a-Shardingjdbc

## 工具说明

- 工具支持以下功能：
  - 输入sql语句，转换成分库分表可执行sql语句。

- 本地环境可直连数据库执行分库分表sql语句，无需手动拷贝sql到Navicat Premium工具运行。

- 输入sql语句和参数值，转换成分库分表可执行sql语句，支持update、select、delete语句。

## 工具使用和配置文档

### 1. 运行AccessTools.exe服务

工具->小工具->分库分表工具

### 2. 配置shardingJdbcConfigure文件

点击左上角帮助->打开配置目录 跳转到配置路径，参考下面内容配置shardingJdbcConfigure.properties文件，正常情况下拷贝以下内容修改下硬盘路径即可!

```
#第X行 = 数据源配置:目标路径
tableBean0 = E:\\Game\\a-tools\\config\\china\\dbm\\test.json__E:\\a-tools\\config\\game\\china\\sql
tableBean1 = E:\\Game\\a-tools\\config\\china\\dbm\\dev.json__E:\\a-tools\\config\\game\\china\\sql
tableBean2 = E:\\Game\\a-tools\\config\\china\\dev.json__E:\\a-tools\\config\\game\\china\\sql
```

### 3. 文字输入框填写sql内容

- **输入sql语句，字段无可变参数**

```SQL
#正确示例
CREATE TABLE `table`
(
    `id`      int(10) NOT NULL,
    `column`  int(10) NOT NULL,
    `column1`     json,
    `column2`     int(10),
    `column3`   int(10),
    PRIMARY KEY (`id`)
);
# 错误示例
update table set column = column3 + ? where id = ?;
```

- 输入sql语句，字段有可变参数
  - 只支持update、select、delete语句。

- 分库分表策略ID，必须放在where后面作为第一个查询条件。

- 多个参数，用","分割。

```Perl
#正确示例
  输入sql内容：update table set column = column + ? where id = ? and column2 = 2;
  输入参数格式{column,id}：50,100005
# 错误示例
ALTER TABLE `table` ADD COLUMN `column` int(11) NOT NULL DEFAULT 0;
```

- **注意事项**
  - 每条sql末尾必须添加“;”。

- 表名前后必须添加符号``。

### 4. 执行操作，生成sql

- 执行操作1或操作2生成sql文件
- 生产环境只允许执行操作1
- 本地测试环境可执行操作2

### 5. sql检查

- 点击查看跳转到sql存储路径检查sql
- sql上传到测试服进行测试验证

### 6. 上传git

- 修改版本号和db.migration sql文件一致
- 上传到git