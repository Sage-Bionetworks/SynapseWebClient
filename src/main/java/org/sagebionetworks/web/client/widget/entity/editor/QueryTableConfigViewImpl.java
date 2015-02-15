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

public class QueryTableConfigViewImpl implements QueryTableConfigView {

	public interface QueryTableConfigViewImplUiBinder extends UiBinder<Widget, QueryTableConfigViewImpl> {}
	private Presenter presenter;
	private APITableColumnManager columnsManager;
	
	private Widget widget;
	@UiField
	TextBox queryField;
	@UiField
	CheckBox isPagingField;
	@UiField
	CheckBox isRowVisibleField;
	
	@UiField
	SimplePanel columnManagerContainer;
	
	@Inject
	public QueryTableConfigViewImpl(QueryTableConfigViewImplUiBinder binder, APITableColumnManager columnsManager) {
		widget = binder.createAndBindUi(this);
		this.columnsManager = columnsManager;
		columnManagerContainer.setWidget(columnsManager.asWidget());
	}
	
	@Override
	public void initView() {
	}
	
	@Override
	public void configure(APITableConfig tableConfig) {
		columnsManager.configure(tableConfig.getColumnConfigs());
		queryField.setValue(tableConfig.getUri());
		isRowVisibleField.setValue(tableConfig.isShowRowNumber());
		isPagingField.setValue(tableConfig.isPaging());
	}
	
	@Override
	public List<APITableColumnConfig> getConfigs() {
		return columnsManager.getColumnConfigs();
	}
	
	@Override
	public void checkParams() throws IllegalArgumentException {
	}
	
	@Override
	public String getQueryString() {
		return queryField.getValue();
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
		return widget;
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
