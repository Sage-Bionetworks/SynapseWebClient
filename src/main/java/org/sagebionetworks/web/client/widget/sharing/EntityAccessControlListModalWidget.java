package org.sagebionetworks.web.client.widget.sharing;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.web.client.jsinterop.EntityAclEditorModalProps;
import org.sagebionetworks.web.client.utils.Callback;

public interface EntityAccessControlListModalWidget extends IsWidget {
  void configure(
    String entityId,
    EntityAclEditorModalProps.Callback onUpdateSuccess
  );

  void setOpen(boolean open);
}
