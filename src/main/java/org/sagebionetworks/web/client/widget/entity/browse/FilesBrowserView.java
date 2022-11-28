package org.sagebionetworks.web.client.widget.entity.browse;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.utils.CallbackP;

public interface FilesBrowserView extends IsWidget, SynapseView {
  /**
   * Configure the view with the parent id
   *
   * @param entityId
   */
  void configure(String entityId);

  void setEntityClickedHandler(CallbackP<String> callback);

  void setActionMenu(IsWidget w);

  void setAddToDownloadListWidget(IsWidget w);
}
