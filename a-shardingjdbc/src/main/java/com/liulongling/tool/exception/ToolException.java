package com.liulongling.tool.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * @Author: liulongling
 * @Date: 2021/7/16 15:42
 */
@Slf4j
public class ToolException extends RuntimeException {
    public ToolException(String msg) {
        super("工具异常:[" + msg + "]");
    }
}
