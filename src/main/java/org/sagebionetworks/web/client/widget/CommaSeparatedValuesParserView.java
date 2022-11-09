package org.sagebionetworks.web.client.widget;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import java.util.List;
import java.util.function.Consumer;

public interface CommaSeparatedValuesParserView extends IsWidget {
  void hide();

  void show();

  public interface Presenter {
    void configure(Consumer<List<String>> onAddCallback);
    void onCancel();
    void onAdd();

    List<String> parseToStringList();

    void show();

    Widget asWidget();
  }

  String getText();

  /**
   * Clears the text box
   */
  void clearTextBox();

  void setPresenter(Presenter presenter);
}
