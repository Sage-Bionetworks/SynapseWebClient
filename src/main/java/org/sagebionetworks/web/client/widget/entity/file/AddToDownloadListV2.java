package org.sagebionetworks.web.client.widget.entity.file;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.repo.model.table.Query;

public interface AddToDownloadListV2 extends IsWidget {
  void configure(String entityId, Query query);
  void configure(String folderId);
}
