package org.sagebionetworks.web.client.widget;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import org.sagebionetworks.web.client.widget.csv.PapaCSVParser;
import org.sagebionetworks.web.client.widget.csv.PapaParseResult;

public class CommaSeparatedValuesParser
  implements CommaSeparatedValuesParserView.Presenter {

  private CommaSeparatedValuesParserView view;
  private Consumer<List<String>> onAddCallback;
  private PapaCSVParser papaCSVParser;

  @Inject
  public CommaSeparatedValuesParser(
    CommaSeparatedValuesParserView view,
    PapaCSVParser papaCSVParser
  ) {
    this.view = view;
    this.papaCSVParser = papaCSVParser;
    view.setPresenter(this);
  }

  @Override
  public void configure(Consumer<List<String>> onAddCallback) {
    this.onAddCallback = onAddCallback;
  }

  @Override
  public void onCancel() {
    view.hide();
  }

  @Override
  public void onAdd() {
    if (this.onAddCallback != null) {
      this.onAddCallback.accept(parseToStringList());
    }
    view.clearTextBox();
    view.hide();
  }

  @Override
  public List<String> parseToStringList() {
    String text = view.getText();
    PapaParseResult parsed = papaCSVParser.parse(text.trim());
    List<String> result = new ArrayList<>();
    for (String[] row : parsed.data) {
      Collections.addAll(result, row);
    }
    return result;
  }

  @Override
  public void show() {
    view.show();
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }
}
