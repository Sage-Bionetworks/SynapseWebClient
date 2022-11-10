package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class UserCardProps extends ReactComponentProps {

  @JsFunction
  public interface Callback {
    void run();
  }

  Object userProfile;
  String size;
  String avatarSize;
  boolean showCardOnHover;
  Object menuActions;
  String preSignedURL;
  boolean hideEmail;
  String link;
  boolean isCertified;
  boolean isValidated;
  boolean withAvatar;
  String className;

  @JsOverlay
  public static UserCardProps create(
    Object userProfile,
    String size,
    String avatarSize,
    boolean showCardOnHover,
    Object menuActions,
    String preSignedURL,
    boolean hideEmail,
    String link,
    boolean isCertified,
    boolean isValidated,
    boolean withAvatar,
    String className
  ) {
    UserCardProps props = new UserCardProps();
    props.userProfile = userProfile;
    props.size = size;
    props.avatarSize = avatarSize;
    props.showCardOnHover = showCardOnHover;
    props.menuActions = menuActions;
    props.preSignedURL = preSignedURL;
    props.hideEmail = hideEmail;
    props.link = link;
    props.isCertified = isCertified;
    props.isValidated = isValidated;
    props.withAvatar = withAvatar;
    props.className = className;
    return props;
  }
}
