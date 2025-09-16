package org.sagebionetworks.web.client.widget;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.repo.model.grid.CreateGridRequest;

public interface CreateGridSessionDialog extends IsWidget {
  /**
   * Creates a grid session and redirects to the grid page.
   * @param request
   */
  void createGridSession(CreateGridRequest request);
}
