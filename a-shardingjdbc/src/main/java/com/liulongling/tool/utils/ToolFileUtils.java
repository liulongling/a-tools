package com.liulongling.tool.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * 文件管理
 *
 * @Author: liulongling
 * @Date: 2021/7/15 15:41
 */
public class ToolFileUtils {
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    public static void createNewdir(String newdir) {
        File file = new File(newdir);
        if (!file.exists()) {
            file.mkdir();
        }
    }

    public static File createNewFile(String newFile) {
        File file = new File(newFile);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    /**
     * 拷贝文件
     *
     * @param source 源文件
     * @param dest   目标文件
     * @throws IOException
     */
    public static void copyFileUsingJava7Files(File source, File dest)
            throws IOException {
        Files.copy(source.toPath(), dest.toPath());
    }
}
