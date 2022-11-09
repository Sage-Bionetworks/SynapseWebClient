package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.sagebionetworks.web.client.widget.CommaSeparatedValuesParser;

public interface EditJSONListModalView extends IsWidget {
  void setPresenter(Presenter presenter);

  void showError(String message);

  void clearEditors();

  void addCommaSeparatedValuesParser(Widget asWidget);

  void showEditor();

  void hideEditor();

  void addNewEditor(CellEditor editor);

  void moveAddNewAnnotationValueButtonToRowToLastRow();

  public interface Presenter {
    void onSave();

    void onClickPasteNewValues();

    void onAddNewEmptyValue();

    void addNewValues(Iterable<String> values);

    void onValueDeleted(CellEditor editor);

    Widget asWidget();
  }
}
