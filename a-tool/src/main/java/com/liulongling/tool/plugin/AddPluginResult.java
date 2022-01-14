package com.liulongling.tool.plugin;

import com.liulongling.tool.model.PluginJarInfo;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AddPluginResult {

    private PluginJarInfo pluginJarInfo;

    private boolean newPlugin;
}
