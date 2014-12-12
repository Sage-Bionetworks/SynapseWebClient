package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;

import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TabbedTableConfigViewImpl extends LayoutContainer implements TabbedTableConfigView {
	private Presenter presenter;
	private TextArea tableContents;
	
	@Inject
	public TabbedTableConfigViewImpl() {
	}
	
	@Override
	public void initView() {
		VerticalPanel vp = new VerticalPanel();
		vp.setStyleName("margin-top-left-10");
		tableContents = new TextArea();
		tableContents.setAllowBlank(false);
		Label tableLabel = new Label(DisplayConstants.TABLE_LABEL);
		tableLabel.setWidth(500);
		tableContents.setWidth(530);
		tableContents.setHeight(400);
		vp.add(tableLabel);
		vp.add(tableContents);
		
		add(vp);
	}

	@Override
	public void checkParams() throws IllegalArgumentException {
		if (!tableContents.isValid())
			throw new IllegalArgumentException(tableContents.getErrorMessage());
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
		if (tableContents != null)
			tableContents.setValue("");
	}

	@Override
	public String getTableContents() {
		return tableContents.getValue();
	}
	
	/*
	 * Private Methods
	 */

}
