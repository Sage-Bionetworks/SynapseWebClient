package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.List;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;

import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class QueryTableConfigViewImpl extends LayoutContainer implements QueryTableConfigView {

	private Presenter presenter;
	private TextField<String> queryField, rowNumbersColumnNameField;
	private CheckBox isPagingField, isRowVisibleField;
	private APITableColumnManager columnsManager;
	
	@Inject
	public QueryTableConfigViewImpl(APITableColumnManager columnsManager) {
		this.columnsManager = columnsManager;
	}
	
	@Override
	public void initView() {
		//build the view
		FlowPanel flowpanel = new FlowPanel();
		flowpanel.addStyleName("margin-left-5");
		flowpanel.addStyleName("margin-top-5");
		queryField = new TextField<String>();
		isRowVisibleField = new CheckBox(DisplayConstants.SYNAPSE_API_CALL_SHOW_ROW_NUMBERS_COL);
		isRowVisibleField.addStyleName("apitable");

		isPagingField = new CheckBox(DisplayConstants.SYNAPSE_API_CALL_IS_PAGING);
		isPagingField.addStyleName("apitable");

		rowNumbersColumnNameField = new TextField<String>();
		
		
		initNewField(DisplayConstants.SYNAPSE_API_CALL_QUERY_LABEL, queryField, flowpanel);
		queryField.setAllowBlank(false);
		
		flowpanel.add(new SimplePanel(isPagingField));
		
		flowpanel.add(new SimplePanel(isRowVisibleField));
		//initNewField(DisplayConstants.SYNAPSE_API_CALL_ROW_NUMBERS_COL_NAME, rowNumbersColumnNameField, flowpanel);
		
		flowpanel.add(columnsManager.asWidget());
		
		add(flowpanel);
	}
	
	public static void showDialog(APITableColumnManager columnManager) {
        final Dialog window = new Dialog();
        window.setMaximizable(false);
        window.setSize(400, 400);
        window.setPlain(true); 
        window.setModal(true); 
        
        window.setHeading("Column Configs"); 
        window.setButtons(Dialog.OK);
        window.setHideOnButtonClick(true);

        window.setLayout(new FitLayout());
        ScrollPanel scrollPanelWrapper = new ScrollPanel();
        scrollPanelWrapper.add(columnManager.asWidget());
	    window.add(scrollPanelWrapper);
	    window.show();		
	}
	
	@Override
	public void configure(APITableConfig tableConfig) {
		columnsManager.configure(tableConfig.getColumnConfigs());
		queryField.setValue(tableConfig.getUri());
		isRowVisibleField.setValue(tableConfig.isShowRowNumber());
		rowNumbersColumnNameField.setValue(tableConfig.getRowNumberColName());
		isPagingField.setValue(tableConfig.isPaging());
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
		if (!queryField.isValid())
			throw new IllegalArgumentException(queryField.getErrorMessage());
	}
	@Override
	public String getQueryString() {
		return queryField.getValue();
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
	public Boolean isShowRowNumbers() {
		return isRowVisibleField.getValue();
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
	public int getDisplayHeight() {
		return 230;
	}
	@Override
	public int getAdditionalWidth() {
		return 0;
	}
	
	@Override
	public void clear() {
	}
	
	/*
	 * Private Methods
	 */

}
