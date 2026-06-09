package org.sagebionetworks.web.client.widget.entity.download;

import com.google.gwt.user.client.ui.Widget;

/**
 * Interface for the upload dialog widget. Extracted to decouple
 * {@link EntityActionControllerImpl} from the concrete GWT Widget subclass,
 * enabling plain-Mockito unit tests.
 */
public interface UploadDialogWidget {
  void configure(String entityId);

  void show();

  void clearDragAndDropHandlers();

  Widget asWidget();
}
