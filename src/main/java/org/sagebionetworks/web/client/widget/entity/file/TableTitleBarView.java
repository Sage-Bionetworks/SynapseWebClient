package org.sagebionetworks.web.client.widget.entity.file;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.web.client.SynapseView;

public interface TableTitleBarView extends IsWidget, SynapseView {
  void createTitlebar(Entity entity);

  void setEntityName(String name);

  void setVersionLabel(String label);

  void setPresenter(Presenter p);

  void setVersionUIToggleVisible(boolean visible);

  void setVersionHistoryLinkText(String text);

  /**
   * Presenter interface
   */
  interface Presenter {
    void toggleShowVersionHistory();
  }
}
