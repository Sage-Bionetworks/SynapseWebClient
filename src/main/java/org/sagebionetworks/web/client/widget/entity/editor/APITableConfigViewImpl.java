package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.List;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;

import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class APITableConfigViewImpl extends FlowPanel implements APITableConfigView {

	private Presenter presenter;
	private TextField<String> urlField, rowNumbersColumnNameField, pageSizeField, jsonResultsKeyNameField, cssStyleNameField;
	private CheckBox isPagingField, isRowVisibleField, isQueryTableResults, isShowIfLoggedInOnly;
	private APITableColumnManager columnsManager;
	
	@Inject
	public APITableConfigViewImpl(APITableColumnManager columnsManager) {
		this.columnsManager = columnsManager;
	}
	
	@Override
	public void initView() {
		//build the view
		FlowPanel flowpanel = new FlowPanel();
		flowpanel.addStyleName("margin-left-5");
		flowpanel.addStyleName("margin-top-5");
		urlField = new TextField<String>();
		isRowVisibleField = new CheckBox(DisplayConstants.SYNAPSE_API_CALL_SHOW_ROW_NUMBERS_COL);
		isRowVisibleField.addStyleName("apitable");
		rowNumbersColumnNameField = new TextField<String>();
		isPagingField = new CheckBox(DisplayConstants.SYNAPSE_API_CALL_IS_PAGING);
		isPagingField.addStyleName("apitable");
		isQueryTableResults = new CheckBox(DisplayConstants.SYNAPSE_API_CALL_IS_QUERY_TABLE_RESULTS);
		isQueryTableResults.addStyleName("apitable");
		isShowIfLoggedInOnly = new CheckBox(DisplayConstants.SYNAPSE_API_CALL_IS_SHOW_IF_LOGGED_IN_ONLY);
		isShowIfLoggedInOnly.addStyleName("apitable");
		
		pageSizeField = new TextField<String>();
		jsonResultsKeyNameField = new TextField<String>();
		cssStyleNameField = new TextField<String>();
		
		initNewField(DisplayConstants.SYNAPSE_API_CALL_URL_LABEL, urlField, flowpanel);
		urlField.setAllowBlank(false);

		initNewField(DisplayConstants.SYNAPSE_API_CALL_ROW_NUMBERS_COL_NAME, rowNumbersColumnNameField, flowpanel);

		flowpanel.add(DisplayUtils.wrapInDiv(isPagingField));
		flowpanel.add(DisplayUtils.wrapInDiv(isRowVisibleField));
		
		initNewField(DisplayConstants.SYNAPSE_API_CALL_PAGE_SIZE, pageSizeField, flowpanel);
		
		initNewField(DisplayConstants.SYNAPSE_API_CALL_JSON_REUSLTS_KEY_NAME, jsonResultsKeyNameField, flowpanel);
		initNewField(DisplayConstants.SYNAPSE_API_CALL_CSS_STYLE_NAME, cssStyleNameField, flowpanel);
		
		flowpanel.add(DisplayUtils.wrapInDiv(isQueryTableResults));
		flowpanel.add(DisplayUtils.wrapInDiv(isShowIfLoggedInOnly));
		
		flowpanel.add(columnsManager.asWidget());
		
		add(flowpanel);
	}
	
	@Override
	public void configure(APITableConfig tableConfig) {
		columnsManager.configure(tableConfig.getColumnConfigs());
		urlField.setValue(tableConfig.getUri());
		isPagingField.setValue(tableConfig.isPaging());
		isQueryTableResults.setValue(tableConfig.isQueryTableResults());
		isShowIfLoggedInOnly.setValue(tableConfig.isShowOnlyIfLoggedIn());
		isRowVisibleField.setValue(tableConfig.isShowRowNumber());
		rowNumbersColumnNameField.setValue(tableConfig.getRowNumberColName());
		pageSizeField.setValue(Integer.toString(tableConfig.getPageSize()));
		jsonResultsKeyNameField.setValue(tableConfig.getJsonResultsArrayKeyName());
		cssStyleNameField.setValue(tableConfig.getCssStyleName());
	}
	
	@Override
	public List<APITableColumnConfig> getConfigs() {
		return columnsManager.getColumnConfigs();
	}
	
	private LayoutContainer initNewField(String label, Field field, FlowPanel container) {
		HorizontalPanel hp= new HorizontalPanel();
		
		Label labelField = new Label(label);
		labelField.setWidth(140);
		field.setWidth(198);
		hp.add(labelField);
		hp.add(field);
		
		container.add(hp);
		return hp;
	}
	
	
	@Override
	public void checkParams() throws IllegalArgumentException {
		if (!urlField.isValid())
			throw new IllegalArgumentException(urlField.getErrorMessage());
	}
	@Override
	public String getApiUrl() {
		return urlField.getValue();
	}
	
	@Override
	public String getPageSize() {
		return pageSizeField.getValue();
	}
	
	@Override
	public String getRowNumberColumnName() {
		return rowNumbersColumnNameField.getValue();
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
	public Boolean isShowRowNumbers() {
		return isRowVisibleField.getValue();
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
		return this;
	}	
	
	@Override 
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
		
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void clear() {
	}
	
	/*
	 * Private Methods
	 */

}
