package org.sagebionetworks.web.client.widget.user;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.base.HasHref;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.gwtbootstrap3.client.ui.html.Strong;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.bootstrap.table.TableData;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UserBadgeViewImpl implements UserBadgeView {
	public interface Binder extends UiBinder<Widget, UserBadgeViewImpl> {	}
	
	@UiField
	TableData loadingUI;
	@UiField
	FocusPanel defaultUserPicture;
	@UiField
	Image userPicture;
	@UiField
	Anchor usernameLink;
	@UiField
	Paragraph description;
	@UiField
	Paragraph errorLoadingUI;
	@UiField
	Strong defaultUserPictureLetter;
	@UiField
	Icon squareIcon;
	
	private Presenter presenter;
	Widget widget;
	Callback onAttachCallback;
	
	@Inject
	public UserBadgeViewImpl(Binder uiBinder) {
		widget = uiBinder.createAndBindUi(this);
		ClickHandler badgeClicked = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.badgeClicked(event);
			}
		};
		userPicture.addClickHandler(badgeClicked);
		defaultUserPicture.addClickHandler(badgeClicked);
		
		userPicture.addErrorHandler(new ErrorHandler() {
			@Override
			public void onError(ErrorEvent event) {
				presenter.onImageLoadError();
			}
		});
		widget.addAttachHandler(new AttachEvent.Handler() {
			@Override
			public void onAttachOrDetach(AttachEvent event) {
				if (event.isAttached()) {
					onAttach();
				}
			}
		});
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
	
	private void onAttach() {
		if (onAttachCallback != null) {
			onAttachCallback.invoke();
		}
	}
	
	@Override
	public void setDefaultPictureColor(String color) {
		squareIcon.setColor(color);
		defaultUserPictureLetter.setColor(color);
	}
	@Override
	public void setDefaultPictureLetter(String letter) {
		defaultUserPictureLetter.setText(letter);
	}
	public void clear() {
		loadingUI.setVisible(false);
		defaultUserPicture.setVisible(false);
		userPicture.setVisible(false);
		description.setVisible(false);
		errorLoadingUI.setVisible(false);
	}
	
	@Override
	public void showAnonymousUserPicture() {
		userPicture.setVisible(false);
		defaultUserPicture.setVisible(true);
	}
	
	@Override
	public void showCustomUserPicture(String url) {
		defaultUserPicture.setVisible(false);
		userPicture.setVisible(true);
		userPicture.setUrl(url);
	}
	
	@Override
	public void setSize(BadgeSize size) {
		if (DisplayUtils.isDefined(size.getDefaultPictureStyle())) {
			defaultUserPicture.addStyleName(size.getDefaultPictureStyle());	
		}
		usernameLink.setStyleName(size.textStyle());
		userPicture.setHeight(size.pictureHeight());
		usernameLink.setVisible(size.isTextVisible());
	}

	@Override
	public void setDisplayName(String displayName, String shortDisplayName) {
		loadingUI.setVisible(false);
		usernameLink.setText(shortDisplayName);
	}
	
	@Override
	public void showLoadError(String error) {
		loadingUI.setVisible(false);
		errorLoadingUI.setText("Error loading profile: " + error);
		errorLoadingUI.setVisible(true);	
	}
	
	@Override
	public void showLoading() {
		loadingUI.setVisible(true);
	}

	@Override
	public void showInfo(String title, String message) {
		// TODO Auto-generated method stub
	}

	@Override
	public void showErrorMessage(String message) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;		
	}

	@Override
	public void showDescription(String descriptionText) {
		description.setText(descriptionText);
		description.setVisible(true);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}
	@Override
	public void setHref(String href) {
		usernameLink.setHref(href);
	}
	
	@Override
	public void clearHref() {
		usernameLink.setHref(HasHref.EMPTY_HREF);
	}
	
	@Override
	public void openNewWindow(String url) {
		DisplayUtils.newWindow(url, "_blank", "");
		
	}
	
	@Override
	public void setOpenNewWindow(boolean value) {
		usernameLink.setTarget(value ? "_blank" : "");
	}
	
	/*
	 * Private Methods
	 */

}
