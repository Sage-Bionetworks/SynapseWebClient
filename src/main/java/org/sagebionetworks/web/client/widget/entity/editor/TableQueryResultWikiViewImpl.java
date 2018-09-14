package org.sagebionetworks.web.client.widget.entity.editor;

import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.TextBox;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TableQueryResultWikiViewImpl implements TableQueryResultWikiView {
	public interface TableQueryResultViewUiBinder extends UiBinder<Widget, TableQueryResultWikiViewImpl> {}
	private Widget widget;
	@UiField
	TextBox queryField;
	@UiField
	CheckBox isQueryVisible;
	@Inject
	public TableQueryResultWikiViewImpl(TableQueryResultViewUiBinder binder) {
		widget = binder.createAndBindUi(this);
	}
	
	@Override
	public void initView() {
		queryField.setValue("");
	}
	@Override
	public void checkParams() throws IllegalArgumentException {
	}
	
	@Override
	public String getSql() {
		return queryField.getValue();
	}
	@Override
	public void setSql(String sql) {
		queryField.setValue(sql);
	}
	@Override
	public Boolean isQueryVisible() {
		return isQueryVisible.getValue();
	}
	
	@Override
	public void setQueryVisible(boolean value) {
		isQueryVisible.setValue(value);
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
	public void showLoading() {
	}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void clear() {
	}
	
	/*
	 * Private Methods
	 */

}
