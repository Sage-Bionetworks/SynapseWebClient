package org.sagebionetworks.web.client.widget.profile;

import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProfileCertifiedValidatedViewImpl implements ProfileCertifiedValidatedView {

	public interface Binder extends UiBinder<Widget, ProfileCertifiedValidatedViewImpl> {
	}

	@UiField
	Icon validatedIcon;
	@UiField
	Image certifiedIcon;
	@UiField
	Paragraph errorMessage;

	Widget widget;

	@Inject
	public ProfileCertifiedValidatedViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setCertifiedVisible(boolean visible) {
		certifiedIcon.setVisible(visible);
	}

	@Override
	public void setVerifiedVisible(boolean visible) {
		validatedIcon.setVisible(visible);
	}

	@Override
	public void setError(String error) {
		errorMessage.setText(error);
	}
}
