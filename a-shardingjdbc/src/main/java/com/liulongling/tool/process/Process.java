package com.liulongling.tool.process;

import com.liulongling.tool.model.SqlModule;

/**
 * @Author: liulongling
 * @Date: 2021/7/24 17:33
 */
public interface Process {
    SqlModule process() throws Exception;
}
