package org.sagebionetworks.web.client.widget.entity.editor;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.TextBox;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.table.v2.QueryInputViewImpl;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TableQueryResultWikiViewImpl implements TableQueryResultWikiView {
	public interface TableQueryResultViewUiBinder extends UiBinder<Widget, TableQueryResultWikiViewImpl> {}
	private Widget widget;
	private Presenter presenter;
	@UiField
	TextBox queryField;
	@UiField
	Button helpButton;
	
	@Inject
	public TableQueryResultWikiViewImpl(TableQueryResultViewUiBinder binder) {
		widget = binder.createAndBindUi(this);
		helpButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				DisplayUtils.newWindow(QueryInputViewImpl.REST_DOC_URL, "", "");
			}
		});
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
