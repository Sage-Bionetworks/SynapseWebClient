package org.sagebionetworks.web.client.widget.entity.editor;

import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.TextBox;
import org.sagebionetworks.web.client.DisplayUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DetailsSummaryConfigViewImpl implements DetailsSummaryConfigView {
	public interface DetailsSummaryConfigViewImplUiBinder extends UiBinder<Widget, DetailsSummaryConfigViewImpl> {
	}

	@UiField
	TextBox summaryField;
	@UiField
	TextArea detailsField;

	private Widget widget;

	@Inject
	public DetailsSummaryConfigViewImpl(DetailsSummaryConfigViewImplUiBinder binder) {
		widget = binder.createAndBindUi(this);
	}

	@Override
	public void initView() {}

	@Override
	public void checkParams() throws IllegalArgumentException {
		if (summaryField.getValue().trim().isEmpty() || detailsField.getValue().trim().isEmpty())
			throw new IllegalArgumentException("Please specify both a summary and details.");
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

	@Override
	public String getDetails() {
		return detailsField.getValue();
	}

	@Override
	public String getSummary() {
		return summaryField.getValue();
	}
}
