package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.web.client.utils.Callback;

public interface EditProjectMetadataModalWidget extends IsWidget {
  public void configure(
    Project project,
    boolean canChangeSettings,
    Callback handler
  );
}
