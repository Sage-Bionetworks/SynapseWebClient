package org.sagebionetworks.web.client.widget.entity.controller;

import com.google.gwt.user.client.ui.IsWidget;
import org.gwtbootstrap3.extras.bootbox.client.callback.PromptCallback;
import org.sagebionetworks.web.client.ShowsErrors;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.PromptForValuesModalView;

/**
 * Abstraction for the view
 *
 * @author John
 *
 */
public interface EntityActionControllerView extends ShowsErrors, IsWidget {
  /**
   * Show the user a confirm dialog.
   *
   * @param message
   * @param callback
   */
  void showConfirmDeleteDialog(String message, Callback callback);

  /**
   * Show info to the user.
   *
   * @param message
   */
  void showInfo(String message);

  /**
   * Show success notification to the user.
   *
   * @param message
   */
  void showSuccess(String message);

  /**
   * Show info dialog to the user.
   */
  void showInfoDialog(String header, String message);

  /**
   * Prompt the user to enter a string value.
   *
   * @param title
   * @param callback
   */
  void showPromptDialog(
    String title,
    String initialValue,
    PromptCallback callback,
    PromptForValuesModalView.InputType inputType
  );

  void setUploadDialogWidget(IsWidget w);

  void addWidget(IsWidget asWidget);

  void showMultiplePromptDialog(
    PromptForValuesModalView.Configuration configuration
  );

  void hideMultiplePromptDialog();

  void setCreateVersionDialogJobTrackingWidget(IsWidget w);

  void showCreateVersionDialog();

  void hideCreateVersionDialog();
}
