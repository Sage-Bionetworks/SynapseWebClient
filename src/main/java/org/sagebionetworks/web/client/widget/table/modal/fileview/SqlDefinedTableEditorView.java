package org.sagebionetworks.web.client.widget.table.modal.fileview;

import com.google.gwt.user.client.ui.IsWidget;

public interface SqlDefinedTableEditorView extends IsWidget {
  String getName();
  void setName(String name);
  String getDescription();
  void setDescription(String description);
  String getDefiningSql();
  void setModalTitle(String title);
  void setDefiningSql(String definingSql);
  void setHelp(String helpMarkdown, String helpUrl);
  void setSynAlert(IsWidget w);
  void setLoading(boolean loading);
  void show();
  void hide();
  void reset();

  public interface Presenter {
    void onSave();
  }

  void setPresenter(Presenter presenter);
}
