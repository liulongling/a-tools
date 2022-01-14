package com.liulongling.tool.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: liulongling
 * @Date: 2021/7/27 15:45
 */
@Data
public class TableSqlModel {

    private String udbName;
    private List<String> list = new ArrayList<>();
}
