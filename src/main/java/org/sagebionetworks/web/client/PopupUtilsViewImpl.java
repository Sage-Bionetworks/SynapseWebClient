package org.sagebionetworks.web.client;

import com.google.gwt.user.client.Window;
import org.sagebionetworks.web.client.jsinterop.ToastMessageOptions;
import org.sagebionetworks.web.client.utils.Callback;

public class PopupUtilsViewImpl implements PopupUtilsView {

  @Override
  public void notify(
    String message,
    DisplayUtils.NotificationVariant notificationVariant,
    ToastMessageOptions toastMessageOptions
  ) {
    DisplayUtils.notify(message, notificationVariant, toastMessageOptions);
  }

  @Override
  public void notify(
    String title,
    String message,
    DisplayUtils.NotificationVariant notificationVariant
  ) {
    DisplayUtils.notify(title, message, notificationVariant);
  }

  @Override
  public void showInfo(String message) {
    DisplayUtils.showInfo(message);
  }

  @Override
  public void showInfo(String message, Integer timeout) {
    ToastMessageOptions options = new ToastMessageOptions.Builder()
      .setAutoCloseInMs(timeout)
      .build();
    DisplayUtils.notify(
      message,
      DisplayUtils.NotificationVariant.INFO,
      options
    );
  }

  @Override
  public void showInfo(String message, String href, String buttonText) {
    ToastMessageOptions options = new ToastMessageOptions.Builder()
      .setPrimaryButton(buttonText, href)
      .build();
    DisplayUtils.notify(
      message,
      DisplayUtils.NotificationVariant.INFO,
      options
    );
  }

  @Override
  public void showError(String message, Integer timeout) {
    DisplayUtils.showErrorToast(message, timeout);
  }

  @Override
  public void showErrorMessage(String message) {
    DisplayUtils.showErrorMessage(message);
  }

  @Override
  public void showErrorMessage(String title, String message) {
    DisplayUtils.showErrorMessage(title, message);
  }

  @Override
  public void showInfoDialog(
    String title,
    String message,
    Callback okCallback
  ) {
    DisplayUtils.showInfoDialog(title, message, okCallback);
  }

  @Override
  public void showConfirmDialog(
    String title,
    String message,
    Callback yesCallback,
    Callback noCallback
  ) {
    DisplayUtils.showConfirmDialog(title, message, yesCallback, noCallback);
  }

  @Override
  public void showConfirmDialog(
    String title,
    String message,
    Callback yesCallback
  ) {
    DisplayUtils.showConfirmDialog(title, message, yesCallback);
  }

  @Override
  public void openInNewWindow(String url) {
    Window.open(url, "_blank", "");
  }

  @Override
  public void showConfirmDelete(String message, Callback callback) {
    DisplayUtils.confirmDelete(message, callback);
  }
}
