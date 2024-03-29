package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.web.client.utils.Callback;

/**
 * Abstraction for the CreateTableModalWidget.
 *
 * @author John
 *
 */
public interface RenameEntityModalWidget extends IsWidget {
  /**
   * Show the create table modal dialog.
   */
  public void onRename(Entity toRename, Callback handler);
}
