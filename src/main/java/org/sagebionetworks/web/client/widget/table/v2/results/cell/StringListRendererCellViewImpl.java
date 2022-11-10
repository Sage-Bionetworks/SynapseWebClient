package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import com.google.inject.Inject;
import org.sagebionetworks.schema.adapter.JSONArrayAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;

/**
 * A non editable renderer for a list of strings (used for integers, strings, and booleans).
 *
 */
public class StringListRendererCellViewImpl
  extends StringRendererCellViewImpl
  implements StringListRendererCellView {

  String v;
  JSONObjectAdapter adapter;

  @Inject
  public StringListRendererCellViewImpl(JSONObjectAdapter adapter) {
    super();
    this.adapter = adapter;
  }

  @Override
  public void setValue(String jsonValue) {
    this.v = jsonValue;
    // try to parse out json values
    try {
      JSONArrayAdapter parsedJson = adapter.createNewArray(jsonValue);
      StringBuilder newValue = new StringBuilder();
      int arrayLength = parsedJson.length();
      for (int i = 0; i < arrayLength; i++) {
        newValue.append(parsedJson.get(i));
        if (i != arrayLength - 1) {
          newValue.append(", ");
        }
      }
      super.setValue(newValue.toString());
    } catch (Exception e) {
      super.setValue(jsonValue);
    }
  }

  @Override
  public String getValue() {
    return v;
  }
}
