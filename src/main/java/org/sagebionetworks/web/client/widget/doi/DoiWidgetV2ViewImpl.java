package org.sagebionetworks.web.client.widget.doi;

import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.TextBoxWithCopyToClipboardWidget;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DoiWidgetV2ViewImpl implements DoiWidgetV2View {

	@UiField
	TextBoxWithCopyToClipboardWidget copyToClipboardWidget;
	@UiField
	Span doiLabel;
	@UiField
	Span synAlertContainer;
	boolean isLabelVisible = true;

	Widget widget;

	public interface Binder extends UiBinder<Widget, DoiWidgetV2ViewImpl> {
	}

	@Inject
	public DoiWidgetV2ViewImpl(Binder uiBinder) {
		widget = uiBinder.createAndBindUi(this);
	}

	@Override
	public void showDoi(String doiText) {
		widget.setVisible(true);
		copyToClipboardWidget.setVisible(true);
		copyToClipboardWidget.setText(doiText);
		doiLabel.setVisible(isLabelVisible);
	}

	@Override
	public void showLoading() {}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void clear() {
		copyToClipboardWidget.setText("");
		copyToClipboardWidget.setVisible(false);
		doiLabel.setVisible(false);
	}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void hide() {
		widget.setVisible(false);
	}

	@Override
	public void setLabelVisible(boolean visible) {
		isLabelVisible = visible;
	}
}
