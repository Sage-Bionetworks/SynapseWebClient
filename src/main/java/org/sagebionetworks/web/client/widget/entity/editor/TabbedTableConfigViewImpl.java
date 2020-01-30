package org.sagebionetworks.web.client.widget.entity.editor;

import org.gwtbootstrap3.client.ui.TextArea;
import org.sagebionetworks.web.client.DisplayUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TabbedTableConfigViewImpl implements TabbedTableConfigView {
	public interface TabbedTableConfigViewImplUiBinder extends UiBinder<Widget, TabbedTableConfigViewImpl> {
	}

	private Presenter presenter;

	@UiField
	public TextArea tableContents;

	public Widget widget;

	@Inject
	public TabbedTableConfigViewImpl(TabbedTableConfigViewImplUiBinder binder) {
		widget = binder.createAndBindUi(this);
	}

	@Override
	public void initView() {}

	@Override
	public void checkParams() throws IllegalArgumentException {
		if (!DisplayUtils.isDefined(tableContents.getValue()))
			throw new IllegalArgumentException("Please enter the table data and try again.");
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
	public void showLoading() {}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void clear() {
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
