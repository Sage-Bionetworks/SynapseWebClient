package org.sagebionetworks.web.client.widget.profile;

import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProfileCertifiedValidatedViewImpl implements ProfileCertifiedValidatedView {
	
	public interface Binder extends UiBinder<Widget, ProfileCertifiedValidatedViewImpl> {}
	
	@UiField
	Icon validatedIcon;
	@UiField
	Image certifiedIcon;
	@UiField
	Paragraph errorMessage;
	
	Widget widget;
	Callback onAttachCallback;
	@Inject
	public ProfileCertifiedValidatedViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
		widget.addAttachHandler(new AttachEvent.Handler() {
			@Override
			public void onAttachOrDetach(AttachEvent event) {
				if (event.isAttached() && onAttachCallback != null) {
					onAttachCallback.invoke();
				}
			}
		});
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
	public boolean isAttached() {
		return widget.isAttached();
	}
	
	@Override
	public boolean isInViewport() {
		return DisplayUtils.isInViewport(widget);
	}
	
	@Override
	public void setOnAttachCallback(Callback onAttachCallback) {
		this.onAttachCallback = onAttachCallback;
	}
	@Override
	public void setError(String error) {
		errorMessage.setText(error);
	}
}
