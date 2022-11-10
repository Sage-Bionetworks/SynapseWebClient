package org.sagebionetworks.web.client.widget.entity.controller;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import java.util.List;

public interface ProvenanceListWidgetView {
  Widget asWidget();

  public interface Presenter {
    Widget asWidget();

    void addEntityRow();

    void addURLRow();

    void configure(
      List<ProvenanceEntry> provEntries,
      ProvenanceType provenanceType
    );
  }

  void removeRow(IsWidget toRemove);

  void addRow(IsWidget newRow);

  void setPresenter(Presenter presenter);

  void setEntityFinder(IsWidget entityFinder);

  void setURLDialog(IsWidget urlDialog);

  void clear();
}
