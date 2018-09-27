package org.sagebionetworks.web.client.widget.user;

import static org.sagebionetworks.web.client.DisplayUtils.DO_NOTHING_CLICKHANDLER;
import static org.sagebionetworks.web.client.DisplayUtils.isDefined;
import static org.sagebionetworks.web.client.DisplayUtils.newWindow;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.Emphasis;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.client.ui.html.Strong;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.place.Profile;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UserBadgeViewImpl implements UserBadgeView {
	public interface Binder extends UiBinder<Widget, UserBadgeViewImpl> {	}
	
	@UiField
	FocusPanel pictureFocusPanel;
	@UiField
	Span defaultUserPicture;
	@UiField
	Image userPicture;
	@UiField
	Anchor usernameLink;
	@UiField
	Strong defaultUserPictureLetter;
	@UiField
	Icon squareIcon;
	@UiField
	Span otherWidgets;
	@UiField
	Span pictureSpan;
	private Presenter presenter;
	Widget widget;
	
	HandlerRegistration handlerRegistration, pictureHandlerRegistration;
	
	public static final String USER_ID_ATTRIBUTE = "data-profile-user-id";
	public static PlaceChanger placeChanger = null;
	public static final ClickHandler STANDARD_CLICKHANDLER = event -> {
		if (!DisplayUtils.isAnyModifierKeyDown(event)) {
			event.preventDefault();
			Widget panel = (Widget)event.getSource();
			String userId = panel.getElement().getAttribute(USER_ID_ATTRIBUTE);
			placeChanger.goTo(new Profile(userId));
		}
	};
	
	public static final ClickHandler NEW_WINDOW_CLICKHANDLER = event -> {
		event.preventDefault();
		Widget panel = (Widget)event.getSource();
		String userId = panel.getElement().getAttribute(USER_ID_ATTRIBUTE);
		newWindow("#!Profile:" + userId, "_blank", "");
	};
	
	@Inject
	public UserBadgeViewImpl(Binder uiBinder, GlobalApplicationState globalAppState) {
		widget = uiBinder.createAndBindUi(this);
		placeChanger = globalAppState.getPlaceChanger();
		String px = (BadgeSize.LARGE.pictureHeightPx() + 4) + "px";
		pictureSpan.setHeight(px);
		pictureSpan.setWidth(px);
		handlerRegistration = usernameLink.addClickHandler(STANDARD_CLICKHANDLER);
		pictureHandlerRegistration = pictureFocusPanel.addClickHandler(STANDARD_CLICKHANDLER);
		userPicture.addErrorHandler(new ErrorHandler() {
			@Override
			public void onError(ErrorEvent event) {
				presenter.onImageLoadError();
			}
		});
	}
	
	@Override
	public void setUserId(String userId) {
		pictureFocusPanel.getElement().setAttribute(USER_ID_ATTRIBUTE, userId);
		usernameLink.getElement().setAttribute(USER_ID_ATTRIBUTE, userId);
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
		otherWidgets.clear();
		defaultUserPicture.setVisible(false);
		userPicture.setVisible(false);
	}
	
	@Override
	public void showAnonymousUserPicture() {
		userPicture.setVisible(false);
		defaultUserPicture.setVisible(true);
	}
	
	public void setClickHandler(ClickHandler clickHandler) {
		handlerRegistration.removeHandler();
		pictureHandlerRegistration.removeHandler();
		handlerRegistration = usernameLink.addClickHandler(clickHandler);
		pictureHandlerRegistration = pictureFocusPanel.addClickHandler(clickHandler);
	}
	
	@Override
	public void setCustomClickHandler(final ClickHandler clickHandler) {
		setClickHandler(event -> {
			event.preventDefault();
			clickHandler.onClick(event);
		});
	}
	
	@Override
	public void doNothingOnClick() {
		handlerRegistration.removeHandler();
		handlerRegistration = usernameLink.addClickHandler(DO_NOTHING_CLICKHANDLER);
		pictureHandlerRegistration.removeHandler();
		pictureHandlerRegistration = pictureFocusPanel.addClickHandler(DO_NOTHING_CLICKHANDLER);
	}
	
	@Override
	public void setOpenInNewWindow() {
		setClickHandler(NEW_WINDOW_CLICKHANDLER);
	}
	
	@Override
	public void showCustomUserPicture(String url) {
		defaultUserPicture.setVisible(false);
		userPicture.setVisible(true);
		userPicture.setUrl(url);
	}
	
	@Override
	public void setSize(BadgeSize size) {
		if (isDefined(size.getDefaultPictureStyle())) {
			defaultUserPicture.addStyleName(size.getDefaultPictureStyle());	
		}
		usernameLink.setStyleName(size.textStyle());
		int pictureHeightPx = size.pictureHeightPx();
		userPicture.setHeight(pictureHeightPx + "px");
		userPicture.setWidth(pictureHeightPx + "px");
		usernameLink.setVisible(size.isTextVisible());
		pictureSpan.setHeight((pictureHeightPx + 6) + "px");
		pictureSpan.setWidth((pictureHeightPx + 6) + "px");
	}

	@Override
	public void setDisplayName(String displayName, String shortDisplayName) {
		otherWidgets.clear();
		usernameLink.setText(shortDisplayName);
	}
	
	@Override
	public void showLoadError(String error) {
		otherWidgets.clear();
		Paragraph errorParagraph = new Paragraph();
		errorParagraph.setEmphasis(Emphasis.DANGER);
		errorParagraph.setText("Error loading profile: " + error);
		otherWidgets.add(errorParagraph);
	}
	
	@Override
	public void showLoading() {
		otherWidgets.clear();
		otherWidgets.add(new Text("Loading..."));
	}

	@Override
	public void showInfo(String message) {
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
		otherWidgets.clear();
		Paragraph descriptionParagraph = new Paragraph();
		descriptionParagraph.setText(descriptionText);
		otherWidgets.add(descriptionParagraph);
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
	public void setStyleNames(String style) {
		widget.addStyleName(style);
	}
	@Override
	public void setHeight(String height) {
		widget.setHeight(height);
	}
	@Override
	public void addUsernameLinkStyle(String style) {
		usernameLink.addStyleName(style);
	}
}
