package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class ApplicationSessionContextJsObject {

  @JsNullable
  public String token;

  @JsNullable
  public String realmId;

  @JsNullable
  public String userId;

  @JsNullable
  public String termsOfServiceStatus;

  @JsNullable
  public String twoFactorStatus;

  public boolean isAuthenticated;

  public boolean hasInitializedSession;

  public RefreshSessionFunction refreshSession;

  @JsNullable
  public TwoFactorAuthSSOErrorResponse twoFactorAuthSSOErrorResponse;

  public ClearSessionFunction clearSession;

  public boolean isLoadingSSO;

  @JsFunction
  public interface RefreshSessionFunction {
    void refreshSession();
  }

  @JsFunction
  public interface ClearSessionFunction {
    void clearSession();
  }

  @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
  public static class TwoFactorAuthSSOErrorResponse {

    public String errorCode;
    public String twoFaToken;
    public int userId;
    public String reason;
  }

  @JsOverlay
  public static ApplicationSessionContextJsObject create(
    String token,
    String realmId,
    String userId,
    boolean isAuthenticated,
    boolean hasInitializedSession,
    RefreshSessionFunction refreshSession,
    ClearSessionFunction clearSession,
    boolean isLoadingSSO
  ) {
    ApplicationSessionContextJsObject context =
      new ApplicationSessionContextJsObject();
    context.token = token;
    context.realmId = realmId;
    context.userId = userId;
    context.isAuthenticated = isAuthenticated;
    context.hasInitializedSession = hasInitializedSession;
    context.refreshSession = refreshSession;
    context.clearSession = clearSession;
    context.isLoadingSSO = isLoadingSSO;
    return context;
  }
}
