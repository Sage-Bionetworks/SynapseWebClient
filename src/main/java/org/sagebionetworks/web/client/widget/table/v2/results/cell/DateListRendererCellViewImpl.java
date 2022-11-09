package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import com.google.inject.Inject;
import java.util.Date;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.sagebionetworks.schema.adapter.JSONArrayAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.DateTimeUtils;

/**
 * A non editable renderer for a list of dates
 *
 */
public class DateListRendererCellViewImpl
  extends StringRendererCellViewImpl
  implements DateListRendererCellView {

  String v;
  JSONObjectAdapter adapter;
  DateTimeUtils dateTimeUtils;

  @Inject
  public DateListRendererCellViewImpl(
    JSONObjectAdapter adapter,
    DateTimeUtils dateTimeUtils
  ) {
    super();
    this.adapter = adapter;
    this.dateTimeUtils = dateTimeUtils;
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
        long time = Long.parseLong(parsedJson.get(i).toString());
        newValue.append(dateTimeUtils.getDateTimeString(new Date(time)));
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
