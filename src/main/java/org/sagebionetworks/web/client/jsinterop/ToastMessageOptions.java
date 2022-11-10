package org.sagebionetworks.web.client.jsinterop;

import com.google.gwt.user.client.Window;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class ToastMessageOptions extends ReactComponentProps {

  @JsOverlay
  public static final int DEFAULT_TOAST_TIMEOUT_MS = 1000 * 15;

  @JsFunction
  public interface Callback {
    void run();
  }

  @JsNullable
  public String title;

  @JsNullable
  public int autoCloseInMs;

  @JsNullable
  public AlertButtonConfig primaryButtonConfig;

  @JsNullable
  public AlertButtonConfig secondaryButtonConfig;

  @JsNullable
  boolean dismissOnPrimaryButtonClick;

  @JsNullable
  boolean dismissOnSecondaryButtonClick;

  @JsOverlay
  public static ToastMessageOptions create(
    String title,
    Integer autoCloseInMs,
    AlertButtonConfig primaryButtonConfig,
    boolean dismissOnPrimaryButtonClick,
    AlertButtonConfig secondaryButtonConfig,
    boolean dismissOnSecondaryButtonClick
  ) {
    if (autoCloseInMs == null) {
      autoCloseInMs = DEFAULT_TOAST_TIMEOUT_MS;
    }

    ToastMessageOptions options = new ToastMessageOptions();
    options.title = title;
    options.autoCloseInMs = autoCloseInMs;
    options.primaryButtonConfig = primaryButtonConfig;
    options.secondaryButtonConfig = secondaryButtonConfig;
    options.dismissOnPrimaryButtonClick = dismissOnPrimaryButtonClick;
    options.dismissOnSecondaryButtonClick = dismissOnSecondaryButtonClick;
    return options;
  }

  @JsOverlay
  public static ToastMessageOptions create(
    String title,
    Integer autoCloseInMs,
    String primaryButtonText,
    AlertButtonConfig.Callback onPrimaryButtonClick
  ) {
    if (autoCloseInMs == null) {
      autoCloseInMs = DEFAULT_TOAST_TIMEOUT_MS;
    }

    ToastMessageOptions options = new ToastMessageOptions();
    options.title = title;
    options.autoCloseInMs = autoCloseInMs;
    options.primaryButtonConfig =
      AlertButtonConfig.create(primaryButtonText, onPrimaryButtonClick);
    return options;
  }

  public static class Builder {

    private String title;
    private Integer autoCloseInMs;
    private String primaryButtonText;
    private AlertButtonConfig.Callback onPrimaryButtonClick;
    private boolean dismissOnPrimaryButtonClick;
    private String secondaryButtonText;
    private AlertButtonConfig.Callback onSecondaryButtonClickOrHref;
    private boolean dismissOnSecondaryButtonClick;

    public Builder() {}

    public Builder setTitle(String title) {
      this.title = title;
      return this;
    }

    public Builder setAutoCloseInMs(Integer autoCloseInMs) {
      this.autoCloseInMs = autoCloseInMs;
      return this;
    }

    public Builder setPrimaryButton(
      String text,
      AlertButtonConfig.Callback onClick,
      boolean dismissOnPrimaryButtonClick
    ) {
      this.primaryButtonText = text;
      this.onPrimaryButtonClick = onClick;
      this.dismissOnPrimaryButtonClick = dismissOnPrimaryButtonClick;
      return this;
    }

    public Builder setPrimaryButton(
      String text,
      AlertButtonConfig.Callback onClick
    ) {
      return setPrimaryButton(text, onClick, false);
    }

    /**
     * Clicking the primary button opens the provided link in the current window.
     * @param text
     * @param href
     * @return
     */
    public Builder setPrimaryButton(String text, String href) {
      return setPrimaryButton(text, href, true);
    }

    /**
     * Clicking the primary button opens the provided link in the current window or a new tab.
     * @param text
     * @param href
     * @return
     */
    public Builder setPrimaryButton(
      String text,
      String href,
      boolean currentWindow,
      boolean dismissOnPrimaryButtonClick
    ) {
      return setPrimaryButton(
        text,
        getCallbackForHref(href, currentWindow),
        dismissOnPrimaryButtonClick
      );
    }

    public Builder setPrimaryButton(
      String text,
      String href,
      boolean currentWindow
    ) {
      return setPrimaryButton(text, href, currentWindow, false);
    }

    public Builder setSecondaryButton(String text, String href) {
      return setSecondaryButton(text, href, true);
    }

    public Builder setSecondaryButton(
      String text,
      String href,
      boolean currentWindow
    ) {
      return setSecondaryButton(text, getCallbackForHref(href, currentWindow));
    }

    public Builder setSecondaryButton(
      String text,
      AlertButtonConfig.Callback onClick,
      boolean dismissOnClick
    ) {
      this.secondaryButtonText = text;
      this.onSecondaryButtonClickOrHref = onClick;
      this.dismissOnSecondaryButtonClick = dismissOnClick;
      return this;
    }

    public Builder setSecondaryButton(
      String text,
      AlertButtonConfig.Callback onClick
    ) {
      return setSecondaryButton(text, onClick, false);
    }

    public ToastMessageOptions build() {
      AlertButtonConfig primaryButtonConfig = null;
      AlertButtonConfig secondaryButtonConfig = null;
      if (primaryButtonText != null && onPrimaryButtonClick != null) {
        primaryButtonConfig =
          AlertButtonConfig.create(primaryButtonText, onPrimaryButtonClick);
      }
      if (secondaryButtonText != null && onSecondaryButtonClickOrHref != null) {
        secondaryButtonConfig =
          AlertButtonConfig.create(
            secondaryButtonText,
            onSecondaryButtonClickOrHref
          );
      }
      return ToastMessageOptions.create(
        title,
        autoCloseInMs,
        primaryButtonConfig,
        dismissOnPrimaryButtonClick,
        secondaryButtonConfig,
        dismissOnSecondaryButtonClick
      );
    }

    private AlertButtonConfig.Callback getCallbackForHref(
      String href,
      boolean currentWindow
    ) {
      if (currentWindow) {
        return () -> Window.Location.assign(href);
      } else {
        return () -> Window.open(href, "_blank", "");
      }
    }
  }
}
