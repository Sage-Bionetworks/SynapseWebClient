package org.sagebionetworks.web.client.widget.user;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.IconSize;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.gwtbootstrap3.client.ui.html.Span;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UserBadgeViewImpl implements UserBadgeView {
	public interface Binder extends UiBinder<Widget, UserBadgeViewImpl> {	}
	
	@UiField
	Span loadingUI;
	@UiField
	Anchor anonymousUserLink;
	@UiField
	Image userPicture;
	@UiField
	Tooltip usernameTooltip;
	@UiField
	Anchor usernameLink;
	@UiField
	Paragraph description;
	@UiField
	Paragraph errorLoadingUI;
	
	private Presenter presenter;
	Widget widget;
	
	@Inject
	public UserBadgeViewImpl(Binder uiBinder) {
		widget = uiBinder.createAndBindUi(this);
		ClickHandler badgeClicked = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.badgeClicked(event);
			}
		};
		anonymousUserLink.addClickHandler(badgeClicked);
		userPicture.addClickHandler(badgeClicked);
		usernameLink.addClickHandler(badgeClicked);
	}
	
	public void clear() {
		loadingUI.setVisible(false);
		anonymousUserLink.setVisible(false);
		userPicture.setVisible(false);
		description.setVisible(false);
		errorLoadingUI.setVisible(false);
	}
	
	@Override
	public void showAnonymousUserPicture() {
		userPicture.setVisible(false);
		anonymousUserLink.setVisible(true);
	}
	
	@Override
	public void showCustomUserPicture(String url) {
		userPicture.setVisible(true);
		userPicture.setUrl(url);
	}
	
	@Override
	public void setSize(BadgeSize size) {
		String sizeInPx = "";
		switch (size) {
			case LARGE:
				anonymousUserLink.setIconSize(IconSize.TIMES5);
				usernameLink.setStyleName("font-size-20");
				sizeInPx="64px";
				break;
			case SMALL:
				anonymousUserLink.setIconSize(IconSize.LARGE);
				usernameLink.setStyleName("font-size-15");
				sizeInPx="24px";
				break;
			case EXTRA_SMALL:
				anonymousUserLink.setIconSize(IconSize.NONE);
				usernameLink.setVisible(false);
				sizeInPx="16px";
				break;
			case DEFAULT:
			default:
				anonymousUserLink.setIconSize(IconSize.TIMES2);
				usernameLink.setStyleName("font-size-17");
				sizeInPx="32px";
				break;
		}
		userPicture.setHeight(sizeInPx);
	}

	@Override
	public void setDisplayName(String displayName, String shortDisplayName) {
		loadingUI.setVisible(false);
		usernameLink.setText(shortDisplayName);
		usernameTooltip.setText(displayName);
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

	/*
	 * Private Methods
	 */

}
