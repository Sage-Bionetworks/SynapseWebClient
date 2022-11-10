package org.sagebionetworks.web.client;

import org.sagebionetworks.web.client.jsinterop.ToastMessageOptions;
import org.sagebionetworks.web.client.utils.Callback;

/**
 * PopupUtilsView is a simple interface on top of DisplayUtils for pushing notifications. The primary benefit of this is
 * that unlike DisplayUtils static methods, PopupUtilsView can be mocked in environments without a JavaScript engine, e.g. in JUnit tests.
 */
public interface PopupUtilsView {
  void notify(
    String message,
    DisplayUtils.NotificationVariant variant,
    ToastMessageOptions options
  );

  void notify(
    String title,
    String message,
    DisplayUtils.NotificationVariant notificationVariant
  );

  void showInfo(String message);

  void showInfo(String message, Integer timeout);

  void showError(String message, Integer timeout);

  void showErrorMessage(String message);

  void showErrorMessage(String title, String message);

  void showInfoDialog(String title, String message, Callback okCallback);

  void showConfirmDialog(
    String title,
    String message,
    Callback yesCallback,
    Callback noCallback
  );

  void showConfirmDialog(String title, String message, Callback yesCallback);

  void openInNewWindow(String url);

  void showConfirmDelete(String message, Callback callback);

  void showInfo(String message, String href, String buttonText);
}
