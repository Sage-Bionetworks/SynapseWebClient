package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.sagebionetworks.repo.model.table.ColumnModel;

public interface JSONListCellEditorView extends CellEditorView {
  public interface Presenter {
    void onEditButtonClick();
    ColumnModel getColumnModel();
  }

  void setPresenter(Presenter editor);
}
