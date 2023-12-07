package org.sagebionetworks.web.client.widget.accessrequirements;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.repo.model.request.ReferenceList;

public interface EntitySubjectsWidgetView extends IsWidget {
  void setPresenter(Presenter presenter);

  public interface Presenter {
    void onChange(ReferenceList updatedEntityReferences);
  }

  void showEntityHeadersTable(
    ReferenceList entityReferences,
    boolean isEditable
  );

  void setVisible(boolean b);
}
