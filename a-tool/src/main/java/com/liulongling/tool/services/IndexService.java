package com.liulongling.tool.services;

import com.liulongling.tool.common.logback.ConsoleLogAppender;
import com.liulongling.tool.controller.IndexController;
import com.liulongling.tool.model.PluginJarInfo;
import com.liulongling.tool.plugin.PluginLoader;
import com.liulongling.tool.plugin.PluginManager;
import com.liulongling.tool.utils.Config;
import com.liulongling.tool.utils.FxmlUtils;
import com.xwintop.xcore.javafx.dialog.FxAlerts;

import java.util.Locale;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Setter
public class IndexService {

    private IndexController indexController;

    public IndexService(IndexController indexController) {
        this.indexController = indexController;
    }

    public void setLanguageAction(String languageType) throws Exception {
        if ("简体中文".equals(languageType)) {
            Config.set(Config.Keys.Locale, Locale.SIMPLIFIED_CHINESE);
        } else if ("English".equals(languageType)) {
            Config.set(Config.Keys.Locale, Locale.US);
        }

        FxAlerts.info("", indexController.getBundle().getString("SetLanguageText"));
    }

    public ContextMenu getSelectContextMenu(String selectText) {
        selectText = selectText.toLowerCase();
        ContextMenu contextMenu = new ContextMenu();
        for (MenuItem menuItem : indexController.getMenuItemMap().values()) {
            if (menuItem.getText().toLowerCase().contains(selectText)) {
                MenuItem menu_tab = new MenuItem(menuItem.getText(), menuItem.getGraphic());
                menu_tab.setOnAction(event1 -> {
                    menuItem.fire();
                });
                contextMenu.getItems().add(menu_tab);
            }
        }
        return contextMenu;
    }

    public void addNodepadAction(ActionEvent event) {
        Parent notepad = FxmlUtils.load("/com/liulongling/tool/fxmlView/notepad.fxml");
        Tab tab = new Tab(indexController.getBundle().getString("addNodepad"));
        tab.setContent(notepad);
        indexController.getTabPaneMain().getTabs().add(tab);
        if (event != null) {
            indexController.getTabPaneMain().getSelectionModel().select(tab);
        }
    }

    public void addLogConsoleAction(ActionEvent event) {
        TextArea textArea = new TextArea();
        textArea.setFocusTraversable(true);
        ConsoleLogAppender.textAreaList.add(textArea);
        Tab tab = new Tab(indexController.getBundle().getString("addLogConsole"));
        tab.setContent(textArea);
        indexController.getTabPaneMain().getTabs().add(tab);
        if (event != null) {
            indexController.getTabPaneMain().getSelectionModel().select(tab);
        }
        tab.setOnCloseRequest((Event event1) -> {
            ConsoleLogAppender.textAreaList.remove(textArea);
        });
    }

    /**
     * 添加Content内容
     */
    public void addContent(String title, String fxmlPath, String resourceBundleName, String iconPath) {

        PluginJarInfo pluginJarInfo = PluginManager.getInstance().getPluginByFxmlPath(fxmlPath);
        if (pluginJarInfo == null) {
            FxAlerts.error("打开失败", "没有找到指定的插件");
            return;
        }

        PluginLoader.loadPluginAsTab(pluginJarInfo, indexController.getTabPaneMain());
    }

    /**
     * @Title: addWebView
     * @Description: 添加WebView视图
     */
    public void addWebView(String title, String url, String iconPath) {
        WebView browser = new WebView();
        WebEngine webEngine = browser.getEngine();
        if (url.startsWith("http")) {
            webEngine.load(url);
        } else {
            webEngine.load(IndexController.class.getResource(url).toExternalForm());
        }

        Tab tab = new Tab(title);
        if (StringUtils.isNotEmpty(iconPath)) {
            ImageView imageView = new ImageView(new Image(iconPath));
            imageView.setFitHeight(18);
            imageView.setFitWidth(18);
            tab.setGraphic(imageView);
        }
        tab.setContent(browser);
        indexController.getTabPaneMain().getTabs().add(tab);
        indexController.getTabPaneMain().getSelectionModel().select(tab);
    }
}
