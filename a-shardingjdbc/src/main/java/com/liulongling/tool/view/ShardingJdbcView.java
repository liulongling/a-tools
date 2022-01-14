package com.liulongling.tool.view;


import com.liulongling.tool.model.ShardingJdbcTableBean;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

public abstract class ShardingJdbcView implements Initializable {
	@FXML
	protected TextArea textAreaSql;
	@FXML
	protected TextArea textAreaFindDBById;
	@FXML
	protected TextField textFieldDBOriginalPath;
	@FXML
	protected TextField textFieldDBTargetPath;
	@FXML
	protected Button buttonChooseOriginalPath;
	@FXML
	protected Button buttonChooseTargetPath;
	@FXML
	protected TableView<ShardingJdbcTableBean> tableViewMain;
	@FXML
	protected TableColumn<ShardingJdbcTableBean, String> tableColumnDBOriginalPath;
	@FXML
	protected TableColumn<ShardingJdbcTableBean, Boolean> tableColumnViewDBOriginalPath;
	@FXML
	protected TableColumn<ShardingJdbcTableBean, String> tableColumnDBTargetPath;
	@FXML
	protected TableColumn<ShardingJdbcTableBean, Boolean> tableColumnViewDBTargetPath;
	@FXML
	protected TableColumn<ShardingJdbcTableBean, Boolean> tableColumnRun;
	@FXML
	protected TableColumn<ShardingJdbcTableBean, Boolean> tableColumnRunToDB;
	@FXML
	protected Button buttonSaveConfigure;
	@FXML
	protected Button otherSaveConfigureButton;
	@FXML
	protected Button loadingConfigureButton;
}
