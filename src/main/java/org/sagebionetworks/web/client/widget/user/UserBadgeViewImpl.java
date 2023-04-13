package org.sagebionetworks.web.client.widget.user;

import static org.sagebionetworks.web.client.DisplayUtils.DO_NOTHING_CLICKHANDLER;
import static org.sagebionetworks.web.client.DisplayUtils.newWindow;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import org.gwtbootstrap3.client.ui.constants.Emphasis;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.JSON;
import org.sagebionetworks.web.client.jsinterop.MenuAction;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.jsinterop.UserCardProps;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;

public class UserBadgeViewImpl extends Div implements UserBadgeView {

  private String userId;
  public static PlaceChanger placeChanger = null;
  public static final CallbackP<String> STANDARD_HANDLER = userId -> {
    placeChanger.goTo(new Profile(userId));
  };
  private String extraCssClassStrings = "";
  public static final CallbackP<String> NEW_WINDOW_HANDLER = userId -> {
    newWindow("#!Profile:" + userId, "_blank", "");
  };
  boolean showAvatar = false;
  boolean showCardOnHover = true;
  AdapterFactory adapterFactory;
  SynapseJSNIUtils jsniUtils;
  SynapseReactClientFullContextPropsProvider propsProvider;
  BadgeType badgeType = BadgeType.SMALL_CARD;
  AvatarSize avatarSize = AvatarSize.MEDIUM;
  FocusPanel userBadgeContainer = new FocusPanel();
  ReactComponentDiv userBadgeReactDiv = new ReactComponentDiv();
  List<MenuAction> menuActionsArray = new ArrayList<>();
  AuthenticationController authController;
  HandlerRegistration clickHandlerRegistration;

  @Inject
  public UserBadgeViewImpl(
    GlobalApplicationState globalAppState,
    SynapseJSNIUtils jsniUtils,
    AdapterFactory adapterFactory,
    AuthenticationController authController,
    final SynapseReactClientFullContextPropsProvider propsProvider
  ) {
    placeChanger = globalAppState.getPlaceChanger();
    this.adapterFactory = adapterFactory;
    this.jsniUtils = jsniUtils;
    this.authController = authController;
    this.propsProvider = propsProvider;
    setMarginRight(2);
    setMarginLeft(2);
    addStyleName("UserBadge");
    addStyleName("vertical-align-middle");
    clickHandlerRegistration =
      userBadgeContainer.addClickHandler(event -> {
        event.preventDefault();
        event.stopPropagation();
        STANDARD_HANDLER.invoke(userId);
      });
    userBadgeContainer.add(userBadgeReactDiv);
  }

  @Override
  public void configure(
    UserProfile profile,
    String pictureUrl,
    Boolean isCertified,
    Boolean isValidated
  ) {
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

    Object userProfileObject = JSON.parse(profileJson);

    UserCardProps props = UserCardProps.create(
      userProfileObject,
      badgeType.getUserCardType(),
      avatarSize.getAvatarSize(),
      showCardOnHover,
      menuActionsArray.toArray(),
      pictureUrl,
      !authController.isLoggedIn(),
      "#!Profile:" + userId,
      isCertified == null ? false : isCertified.booleanValue(),
      isValidated == null ? false : isValidated.booleanValue(),
      showAvatar,
      extraCssClassStrings
    );

    ReactNode component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.UserCard,
      props,
      propsProvider.getJsInteropContextProps()
    );
    userBadgeReactDiv.render(component);
  }

  @Override
  public void setOpenInNewWindow() {
    clickHandlerRegistration.removeHandler();
    clickHandlerRegistration =
      userBadgeContainer.addClickHandler(event -> {
        event.preventDefault();
        event.stopPropagation();
        NEW_WINDOW_HANDLER.invoke(userId);
      });
  }

  @Override
  public void setCustomClickHandler(final ClickHandler clickHandler) {
    clickHandlerRegistration.removeHandler();
    clickHandlerRegistration =
      userBadgeContainer.addClickHandler(event -> {
        event.preventDefault();
        clickHandler.onClick(event);
      });
  }

  @Override
  public void doNothingOnClick() {
    clickHandlerRegistration.removeHandler();
    clickHandlerRegistration =
      userBadgeContainer.addClickHandler(DO_NOTHING_CLICKHANDLER);
  }

  @Override
  public void setShowCardOnHover(boolean showCardOnHover) {
    this.showCardOnHover = showCardOnHover;
  }

  @Override
  public void setBadgeType(BadgeType badgeType) {
    this.badgeType = badgeType;
    switch (this.badgeType) {
      case SMALL_CARD:
        removeStyleName("vertical-align-middle");
        addStyleName("inline-user-badge");
        break;
      case MEDIUM_CARD:
      case LARGE_CARD:
        removeStyleName("vertical-align-middle");
        removeStyleName("inline-user-badge");
        // if medium or large, we must rely on the react component
        clickHandlerRegistration.removeHandler();
        break;
      case AVATAR:
        addStyleName("vertical-align-middle");
        addStyleName("inline-user-badge");
        break;
    }
  }

  @Override
  public void setShowAvatar(boolean showAvatar) {
    this.showAvatar = showAvatar;
  }

  @Override
  public void setAvatarSize(AvatarSize avatarSize) {
    this.avatarSize = avatarSize;
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
    menuActionsArray.add(
      MenuAction.create(commandName, () -> callback.invoke())
    );
  }
}
