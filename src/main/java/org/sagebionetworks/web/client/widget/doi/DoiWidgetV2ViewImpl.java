package org.sagebionetworks.web.client.widget.doi;

import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.security.AuthenticationController;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DoiWidgetV2ViewImpl implements DoiWidgetV2View {
	
	GlobalApplicationState globalApplicationState;
	AuthenticationController authenticationController;
	@UiField
	Icon clipboardIcon;
	@UiField
	TextBox doi;
	@UiField
	Span doiLabel;
	@UiField
	Span synAlertContainer;

	Widget widget;
	
	public interface Binder extends UiBinder<Widget, DoiWidgetV2ViewImpl> {}
	
	@Inject
	public DoiWidgetV2ViewImpl(GlobalApplicationState globalApplicationState,
							   AuthenticationController authenticationController,
							   Binder uiBinder) {
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		widget = uiBinder.createAndBindUi(this);

		doi.addClickHandler(event -> copyDoiToClipboard());

		clipboardIcon.addClickHandler(event -> copyDoiToClipboard());
	}

	/**
	 * See https://stackoverflow.com/questions/1317052/how-to-copy-to-clipboard-with-gwt
	 */
	private void copyDoiToClipboard() {
		doi.setFocus(true);
		doi.selectAll();
		boolean success = copyToClipboard();
		if (success) {
			clipboardIcon.setType(IconType.CHECK);
		}
	}

	private static native boolean copyToClipboard() /*-{
        return $doc.execCommand('copy');
    }-*/;

	@Override
	public void showDoiCreated(String doiText) {
		widget.setVisible(true);
		doi.setVisible(true);
		doi.setText(doiText);
		doiLabel.setVisible(true);
	}

	@Override
	public void showLoading() {
	}

	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public void clear() {
		doi.setText("");
		doi.setVisible(false);
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
	public void setVisible(boolean visible) {
		widget.setVisible(visible);
	}
	@Override
	public void setSynAlert(IsWidget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}
}
