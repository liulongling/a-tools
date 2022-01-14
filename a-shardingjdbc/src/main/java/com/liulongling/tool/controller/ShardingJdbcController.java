package com.liulongling.tool.controller;

import com.liulongling.tool.model.ShardingJdbcTableBean;
import com.liulongling.tool.services.ShardingJdbcService;
import com.liulongling.tool.view.ShardingJdbcView;
import com.xwintop.xcore.util.javafx.FileChooserUtil;
import com.xwintop.xcore.util.javafx.JavaFxSystemUtil;
import com.xwintop.xcore.util.javafx.JavaFxViewUtil;
import com.xwintop.xcore.util.javafx.TooltipUtil;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseButton;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * @Author: liulongling
 * @Date: 2021/7/13 16:40
 */
@Slf4j
public class ShardingJdbcController extends ShardingJdbcView {
    private final ShardingJdbcService shardingJdbcService = new ShardingJdbcService(this);
    private ObservableList<ShardingJdbcTableBean> tableData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initView();
        initEvent();
    }

    private void initView() {
        shardingJdbcService.setTableData(tableData);
        shardingJdbcService.loadingConfigure();

        tableColumnDBOriginalPath.setCellValueFactory(new PropertyValueFactory<>("dbOriginalPath"));
        tableColumnDBOriginalPath.setCellFactory(TextFieldTableCell.forTableColumn());
        tableColumnDBOriginalPath.setOnEditCommit((CellEditEvent<ShardingJdbcTableBean, String> t) -> {
            t.getRowValue().setDbOriginalPath(t.getNewValue());
        });

        JavaFxViewUtil.setTableColumnButonFactory(tableColumnViewDBOriginalPath, "查看", (me, index) -> {
            JavaFxSystemUtil.openDirectory(tableData.get(index).getDbOriginalPath());
        });

        tableColumnDBTargetPath.setCellValueFactory(new PropertyValueFactory<>("dbTargetPath"));
        tableColumnDBTargetPath.setCellFactory(TextFieldTableCell.forTableColumn());
        tableColumnDBTargetPath.setOnEditCommit((CellEditEvent<ShardingJdbcTableBean, String> t) -> {
            t.getRowValue().setDbTargetPath(t.getNewValue());
        });

        JavaFxViewUtil.setTableColumnButonFactory(tableColumnViewDBTargetPath, "查看", (me, index) -> {
            JavaFxSystemUtil.openDirectory(tableData.get(index).getDbTargetPath());
        });

        JavaFxViewUtil.setTableColumnButonFactory(tableColumnRun, "生成sql文件", (me, index) -> {
            if (!StringUtils.isEmpty(textAreaSql.getText()) && !StringUtils.isEmpty(textAreaFindDBById.getText())) {
                try {
                    shardingJdbcService.makeuidSql(tableData.get(index), textAreaSql, false, textAreaFindDBById);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("创建分库分表sql失败：" + e.getMessage());
                    TooltipUtil.showToast("创建分库分表sql失败：" + e.getMessage());
                }

            } else {
                try {
                    shardingJdbcService.makeSql(tableData.get(index), textAreaSql, false);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("创建分库分表sql失败：" + e.getMessage());
                    TooltipUtil.showToast("创建分库分表sql失败：" + e.getMessage());
                }
                try {
                    shardingJdbcService.findDBById(tableData.get(index), textAreaFindDBById);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("查找数据库失败：" + e.getMessage());
                    TooltipUtil.showToast("查找数据库失败：" + e.getMessage());
                }
            }
        });


        JavaFxViewUtil.setTableColumnButonFactory(tableColumnRunToDB, "生成sql文件并执行", (me, index) -> {
            try {
                shardingJdbcService.makeSql(tableData.get(index), textAreaSql, true);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("执行失败：" + e.getMessage());
                TooltipUtil.showToast("执行失败：" + e.getMessage());
            }
        });

        tableViewMain.setItems(tableData);
    }

    private void initEvent() {
        FileChooserUtil.setOnDrag(textFieldDBOriginalPath, FileChooserUtil.FileType.FILE);
        FileChooserUtil.setOnDrag(textFieldDBTargetPath, FileChooserUtil.FileType.FOLDER);
        tableData.addListener((Change<? extends ShardingJdbcTableBean> tableBean) -> {
            try {
                saveConfigure(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        tableViewMain.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                MenuItem menu_Copy = new MenuItem("复制选中行");
                menu_Copy.setOnAction(event1 -> {
                    ShardingJdbcTableBean tableBean = tableViewMain.getSelectionModel().getSelectedItem();
                    ShardingJdbcTableBean tableBean2 = new ShardingJdbcTableBean(tableBean.getPropertys());
                    tableData.add(tableViewMain.getSelectionModel().getSelectedIndex(), tableBean2);
                });
                MenuItem menu_Remove = new MenuItem("删除选中行");
                menu_Remove.setOnAction(event1 -> deleteSelectRowAction(null));
                MenuItem menu_RemoveAll = new MenuItem("删除所有");
                menu_RemoveAll.setOnAction(event1 -> tableData.clear());
                tableViewMain.setContextMenu(new ContextMenu(menu_Copy, menu_Remove, menu_RemoveAll));
            }
        });
    }

    @FXML
    private void chooseOriginalPathAction() {
        File file = FileChooserUtil.chooseDirectory();
        if (file != null) {
            textFieldDBOriginalPath.setText(file.getPath());
            if (!textFieldDBOriginalPath.getText().isEmpty()) {
                File file1 = new File(file.getPath());
                File[] childFiles = file1.listFiles();
                for (File childFile : childFiles) {
                    tableData.add(new ShardingJdbcTableBean(file.getPath() + File.separator + childFile.getName(), textFieldDBTargetPath.getText()));
                }
            }
        }
    }

    @FXML
    private void chooseTargetPathAction() {
        File file = FileChooserUtil.chooseDirectory();
        if (file != null) {
            textFieldDBTargetPath.setText(file.getPath());
            if (!textFieldDBOriginalPath.getText().isEmpty()) {
                File file1 = new File(textFieldDBOriginalPath.getText());
                File[] childFiles = file1.listFiles();
                for (File childFile : childFiles) {
                    tableData.add(new ShardingJdbcTableBean(textFieldDBOriginalPath.getText() + File.separator + childFile.getName(), textFieldDBTargetPath.getText()));
                }
            }
        }
    }


    @FXML
    private void deleteSelectRowAction(ActionEvent event) {
        tableData.remove(tableViewMain.getSelectionModel().getSelectedItem());
    }

    @FXML
    private void saveConfigure(ActionEvent event) throws Exception {
        shardingJdbcService.saveConfigure();
    }

    @FXML
    private void otherSaveConfigureAction(ActionEvent event) throws Exception {
        shardingJdbcService.otherSaveConfigureAction();
    }

    @FXML
    private void loadingConfigureAction(ActionEvent event) {
        shardingJdbcService.loadingConfigureAction();
    }
}
