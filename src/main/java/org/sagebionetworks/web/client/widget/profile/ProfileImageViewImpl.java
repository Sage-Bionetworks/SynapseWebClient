package org.sagebionetworks.web.client.widget.profile;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Image;
import org.sagebionetworks.web.client.utils.Callback;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProfileImageViewImpl implements ProfileImageView {

	public interface Binder extends UiBinder<Widget, ProfileImageViewImpl> {
	}

	@UiField
	Icon defaultIcon;
	@UiField
	Image image;
	@UiField
	Button removePicture;

	ProfileImageWidget presenter;
	Callback removePictureCallback;
	Widget widget;

	@Inject
	public ProfileImageViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
		removePicture.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (presenter != null) {
					presenter.onRemovePicture();
				}
			}
		});
	}

	@Override
	public void setPresenter(ProfileImageWidget presenter) {
		this.presenter = presenter;
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

	@Override
	public void setRemovePictureButtonVisible(boolean isVisible) {
		removePicture.setVisible(isVisible);
	}
}
