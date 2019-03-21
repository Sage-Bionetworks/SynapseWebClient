package org.sagebionetworks.web.client.widget.user;

import static org.sagebionetworks.web.client.DisplayUtils.DO_NOTHING_CLICKHANDLER;
import static org.sagebionetworks.web.client.DisplayUtils.newWindow;

import org.gwtbootstrap3.client.ui.constants.Emphasis;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.Profile;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UserBadgeViewImpl extends Div implements UserBadgeView {
	
	HandlerRegistration handlerRegistration;
	boolean isTextHidden = false;
	AdapterFactory adapterFactory;
	SynapseJSNIUtils jsniUtils;
	BadgeSize badgeSize = BadgeSize.DEFAULT;
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
	FocusPanel userBadgeContainer = new FocusPanel();
	@Inject
	public UserBadgeViewImpl(
			GlobalApplicationState globalAppState, 
			SynapseJSNIUtils jsniUtils,
			AdapterFactory adapterFactory) {
		placeChanger = globalAppState.getPlaceChanger();
		this.adapterFactory = adapterFactory;
		this.jsniUtils = jsniUtils;
		handlerRegistration = userBadgeContainer.addClickHandler(STANDARD_CLICKHANDLER);
		addAttachHandler(event -> {
			if (!event.isAttached()) {
				//detach event, clean up react component
				jsniUtils.unmountComponentAtNode(userBadgeContainer.getElement());
			}
		});
	}
	
	@Override
	public void configure(UserProfile profile) {
		clear();
		userBadgeContainer.getElement().setAttribute(USER_ID_ATTRIBUTE, profile.getOwnerId());
		add(userBadgeContainer);
		String profileJson = "";
		try {
			JSONObjectAdapter jsonObjectAdapter = adapterFactory.createNew();
			profile.writeToJSONObject(jsonObjectAdapter);
			profileJson = jsonObjectAdapter.toJSONString();
		} catch (Throwable e) {
			jsniUtils.consoleError(e);
		}
		_showBadge(userBadgeContainer.getElement(), profileJson, badgeSize.reactClientSize, isTextHidden);
	}
	
	public void setClickHandler(ClickHandler clickHandler) {
		handlerRegistration.removeHandler();
		handlerRegistration = userBadgeContainer.addClickHandler(clickHandler);
	}
	
	@Override
	public void setOpenInNewWindow() {
		setClickHandler(NEW_WINDOW_CLICKHANDLER);
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
		setClickHandler(DO_NOTHING_CLICKHANDLER);
	}
	@Override
	public void setTextHidden(boolean isTextHidden) {
		this.isTextHidden = isTextHidden;
	}
	@Override
	public void setSize(BadgeSize size) {
		this.badgeSize = size;
	}
	
	@Override
	public void showLoadError(String error) {
		clear();
		Paragraph errorParagraph = new Paragraph();
		errorParagraph.setEmphasis(Emphasis.DANGER);
		errorParagraph.setText("Error loading profile: " + error);
		add(errorParagraph);
	}
	
	@Override
	public void showLoading() {
		clear();
		add(new Text("Loading..."));
	}

	@Override
	public void showInfo(String message) {
		// TODO Auto-generated method stub
	}

	@Override
	public void showErrorMessage(String message) {
		// TODO Auto-generated method stub
	}
	
	private static native void _showBadge(Element el, String userProfileJson, String reactClientSize, boolean isTextHidden) /*-{
		
		try {
			function onClick(userProfile) {
				//do nothing, parent FocusPanel will handle button click events
			}
			
			var userCardProps = {
				userProfile: JSON.parse(userProfileJson),
				size: reactClientSize,
				hideText: isTextHidden,
				profileClickHandler: onClick
			};
			
			$wnd.ReactDOM.render(
				$wnd.React.createElement($wnd.SRC.SynapseComponents.UserCard, userCardProps, null), 
				el
			);
		} catch (err) {
			console.error(err);
		}
	}-*/;
	
}
