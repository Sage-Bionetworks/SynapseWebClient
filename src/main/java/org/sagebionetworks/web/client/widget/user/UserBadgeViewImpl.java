package org.sagebionetworks.web.client.widget.user;

import static org.sagebionetworks.web.client.DisplayUtils.DO_NOTHING_CLICKHANDLER;
import static org.sagebionetworks.web.client.DisplayUtils.newWindow;
import org.gwtbootstrap3.client.ui.constants.Emphasis;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.inject.Inject;

public class UserBadgeViewImpl extends Div implements UserBadgeView {
	private String userId;
	public static PlaceChanger placeChanger = null;
	public static final CallbackP<String> STANDARD_HANDLER = userId -> {
		placeChanger.goTo(new Profile(userId));
	};

	public static final CallbackP<String> NEW_WINDOW_HANDLER = userId -> {
		newWindow("#!Profile:" + userId, "_blank", "");
	};
	boolean isTextHidden = false;
	boolean isTooltipHidden = false;
	AdapterFactory adapterFactory;
	SynapseJSNIUtils jsniUtils;
	BadgeSize badgeSize = BadgeSize.DEFAULT;
	CallbackP<String> currentClickHandler = STANDARD_HANDLER;
	FocusPanel userBadgeContainer = new FocusPanel();
	JsArray<JavaScriptObject> menuActionsArray = null;
	AuthenticationController authController;
	boolean isReactHandlingClickEvents = false;

	@Inject
	public UserBadgeViewImpl(GlobalApplicationState globalAppState, SynapseJSNIUtils jsniUtils, AdapterFactory adapterFactory, AuthenticationController authController) {
		placeChanger = globalAppState.getPlaceChanger();
		this.adapterFactory = adapterFactory;
		this.jsniUtils = jsniUtils;
		this.authController = authController;
		setMarginRight(2);
		setMarginLeft(2);
		currentClickHandler = STANDARD_HANDLER;
		addAttachHandler(event -> {
			if (!event.isAttached()) {
				// detach event, clean up react component
				jsniUtils.unmountComponentAtNode(userBadgeContainer.getElement());
			}
		});
		userBadgeContainer.addClickHandler(event -> {
			if (!isReactHandlingClickEvents) {
				event.preventDefault();
				currentClickHandler.invoke(userId);
			}
		});
	}

	@Override
	public void configure(UserProfile profile) {
		userId = profile.getOwnerId();
		clear();
		add(userBadgeContainer);
		String profileJson = "";
		try {
			JSONObjectAdapter jsonObjectAdapter = adapterFactory.createNew();
			profile.writeToJSONObject(jsonObjectAdapter);
			profileJson = jsonObjectAdapter.toJSONString();
		} catch (Throwable e) {
			jsniUtils.consoleError(e);
		}
		String pictureUrl = profile.getProfilePicureFileHandleId() != null ? jsniUtils.getFileHandleAssociationUrl(profile.getOwnerId(), FileHandleAssociateType.UserProfileAttachment, profile.getProfilePicureFileHandleId()) : null;

		_showBadge(userBadgeContainer.getElement(), profileJson, userId, badgeSize.reactClientSize, isTextHidden, isTooltipHidden, pictureUrl, !authController.isLoggedIn(), menuActionsArray, this);
	}

	public void setClickHandler(ClickHandler clickHandler) {
		currentClickHandler = userId -> {
			clickHandler.onClick(null);
		};
	}

	@Override
	public void setOpenInNewWindow() {
		currentClickHandler = NEW_WINDOW_HANDLER;
	}

	@Override
	public void setCustomClickHandler(final ClickHandler clickHandler) {
		setClickHandler(event -> {
			if (event != null) {
				event.preventDefault();
			}
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
	public void setTooltipHidden(boolean isTooltipHidden) {
		this.isTooltipHidden = isTooltipHidden;
	}

	@Override
	public void setSize(BadgeSize size) {
		this.badgeSize = size;
		if (badgeSize.equals(BadgeSize.DEFAULT)) {
			isReactHandlingClickEvents = false;
			addStyleName("inline-block vertical-align-middle");
		} else {
			isReactHandlingClickEvents = true;
			removeStyleName("inline-block");
		}
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

	@Override
	public void addContextCommand(String commandName, Callback callback) {
		if (menuActionsArray == null) {
			menuActionsArray = _initJsArray();
		}
		// add to menu actions array
		_addToMenuActionsArray(commandName, callback, menuActionsArray);
	}

	private static native void _showBadge(Element el, String userProfileJson, String userId, String reactClientSize, boolean isTextHidden, boolean isTooltipHidden, String pictureUrl, boolean isEmailHidden, JsArray<JavaScriptObject> menuActionsArray, UserBadgeViewImpl userBadgeView) /*-{

		try {
			var userProfileObject = JSON.parse(userProfileJson);
			var userCardProps = {
				userProfile : userProfileObject,
				size : reactClientSize,
				hideText : isTextHidden,
				hideTooltip : isTooltipHidden,
				menuActions : menuActionsArray,
				preSignedURL : pictureUrl,
				hideEmail : isEmailHidden,
				link : '#!Profile:' + userId
			};

			$wnd.ReactDOM.render($wnd.React.createElement(
					$wnd.SRC.SynapseComponents.UserCard, userCardProps, null),
					el);
		} catch (err) {
			console.error(err);
		}
	}-*/;

	private static native JsArray<JavaScriptObject> _initJsArray() /*-{
		return [];
	}-*/;

	private static native void _addToMenuActionsArray(String commandName, Callback callback, JsArray<JavaScriptObject> menuActionsArray) /*-{
		function onMenuActionClick(userProfile) {
			callback.@org.sagebionetworks.web.client.utils.Callback::invoke()();
		}
		menuActionsArray.push({
			field : commandName,
			callback : onMenuActionClick
		});
	}-*/;
}
