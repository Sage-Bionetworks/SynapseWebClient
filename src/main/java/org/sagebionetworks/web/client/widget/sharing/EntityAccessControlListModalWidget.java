package org.sagebionetworks.web.client.widget.sharing;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.web.client.jsinterop.EntityAclEditorModalProps;

public interface EntityAccessControlListModalWidget extends IsWidget {
  void configure(
    String entityId,
    EntityAclEditorModalProps.Callback onUpdateSuccess
  );

  void configure(
    String entityId,
    EntityAclEditorModalProps.Callback onUpdateSuccess,
    boolean isAfterUpload
  );

  void setOpen(boolean open);
}
