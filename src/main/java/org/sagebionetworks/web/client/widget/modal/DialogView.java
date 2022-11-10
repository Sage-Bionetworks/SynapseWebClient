package org.sagebionetworks.web.client.widget.modal;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.web.client.widget.modal.Dialog.Callback;

public interface DialogView extends IsWidget {
  void configure(
    String title,
    IsWidget body,
    String primaryButtonText,
    String defaultButtonText,
    Callback callback,
    boolean autoHide
  );
  void addStyleName(String style);
  void show();
}
