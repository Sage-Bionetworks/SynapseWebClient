package org.sagebionetworks.web.client.widget.profile;

import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Image;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProfileImageViewImpl implements ProfileImageView {
	
	public interface Binder extends UiBinder<Widget, ProfileImageViewImpl> {}
	
	@UiField
	Icon defaultIcon;
	@UiField
	Image image;
	
	Widget widget;
	
	@Inject
	public ProfileImageViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
	}

	@Override
	public void showDefault() {
		image.setVisible(false);
		defaultIcon.setVisible(true);
	}

	@Override
	public void setImageUrl(String url) {
		defaultIcon.setVisible(false);
		image.setUrl(url);
		image.setVisible(true);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}
}
