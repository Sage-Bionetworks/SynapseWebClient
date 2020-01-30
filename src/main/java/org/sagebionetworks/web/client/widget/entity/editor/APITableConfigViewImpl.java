package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.List;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.TextBox;
import org.sagebionetworks.web.client.DisplayUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class APITableConfigViewImpl implements APITableConfigView {
	public interface APITableConfigViewImplUiBinder extends UiBinder<Widget, APITableConfigViewImpl> {
	}

	@UiField
	TextBox urlField;
	@UiField
	TextBox pageSizeField;
	@UiField
	TextBox jsonResultsKeyNameField;
	@UiField
	TextBox cssStyleNameField;
	@UiField
	CheckBox isPagingField;
	@UiField
	CheckBox isQueryTableResults;
	@UiField
	CheckBox isShowIfLoggedInOnly;

	@UiField
	SimplePanel columnManagerContainer;

	private APITableColumnManager columnsManager;
	private Widget widget;

	@Inject
	public APITableConfigViewImpl(APITableConfigViewImplUiBinder binder, APITableColumnManager columnsManager) {
		widget = binder.createAndBindUi(this);
		this.columnsManager = columnsManager;
		columnManagerContainer.setWidget(columnsManager.asWidget());
	}

	@Override
	public void initView() {}

	@Override
	public void configure(APITableConfig tableConfig) {
		columnsManager.configure(tableConfig.getColumnConfigs());
		urlField.setValue(tableConfig.getUri());
		isPagingField.setValue(tableConfig.isPaging());
		isQueryTableResults.setValue(tableConfig.isQueryTableResults());
		isShowIfLoggedInOnly.setValue(tableConfig.isShowOnlyIfLoggedIn());
		pageSizeField.setValue(Integer.toString(tableConfig.getPageSize()));
		jsonResultsKeyNameField.setValue(tableConfig.getJsonResultsArrayKeyName());
		cssStyleNameField.setValue(tableConfig.getCssStyleName());
	}

	@Override
	public List<APITableColumnConfig> getConfigs() {
		return columnsManager.getColumnConfigs();
	}

	@Override
	public void checkParams() throws IllegalArgumentException {}

	@Override
	public String getApiUrl() {
		return urlField.getValue();
	}

	@Override
	public String getPageSize() {
		return pageSizeField.getValue();
	}

	@Override
	public Boolean isPaging() {
		return isPagingField.getValue();
	}

	@Override
	public Boolean isQueryTableResults() {
		return isQueryTableResults.getValue();
	}

	@Override
	public Boolean isShowIfLoggedInOnly() {
		return isShowIfLoggedInOnly.getValue();
	}

	@Override
	public String getJsonResultsKeyName() {
		return jsonResultsKeyNameField.getValue();
	}

	@Override
	public String getCssStyle() {
		return cssStyleNameField.getValue();
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void clear() {}
}
