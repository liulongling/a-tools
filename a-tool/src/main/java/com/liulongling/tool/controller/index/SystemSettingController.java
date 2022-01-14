package com.liulongling.tool.controller.index;

import com.liulongling.tool.view.index.SystemSettingView;
import com.liulongling.tool.utils.Config;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * @ClassName: SystemSettingController
 * @Description: 设置页面
 * @author: xufeng
 * @date: 2020/2/25 0025 16:44
 */

@Getter
@Setter
@Slf4j
public class SystemSettingController extends SystemSettingView {

    private Stage newStage = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initView();
    }

    private void initView() {
        try {
            exitShowAlertCheckBox.setSelected(Config.getBoolean(Config.Keys.ConfirmExit, true));
            addNotepadCheckBox.setSelected(Config.getBoolean(Config.Keys.NotepadEnabled, true));
            saveStageBoundCheckBox.setSelected(Config.getBoolean(Config.Keys.RememberWindowLocation, true));
            chkNewLauncher.setSelected(Config.getBoolean(Config.Keys.NewLauncher, false));
        } catch (Exception e) {
            log.error("加载配置失败：", e);
        }
    }

    public void applySettings() {
        try {
            Config.set(Config.Keys.ConfirmExit, exitShowAlertCheckBox.isSelected());
            Config.set(Config.Keys.NotepadEnabled, addNotepadCheckBox.isSelected());
            Config.set(Config.Keys.RememberWindowLocation, saveStageBoundCheckBox.isSelected());
            Config.set(Config.Keys.NewLauncher, chkNewLauncher.isSelected());

            if (newStage != null) {
                newStage.close();
            }
        } catch (Exception e) {
            log.error("保存配置失败：", e);
        }
    }
}
