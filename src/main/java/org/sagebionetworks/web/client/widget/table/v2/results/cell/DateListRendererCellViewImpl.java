package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import java.util.Date;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.sagebionetworks.schema.adapter.JSONArrayAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.DateTimeUtils;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.inject.Inject;

/**
 * A non editable renderer for a list of dates
 *
 */
public class DateListRendererCellViewImpl extends Paragraph implements DateListRendererCellView {
	String v;
	JSONObjectAdapter adapter;
	DateTimeUtils dateTimeUtils;
	public static final MouseOverHandler STRING_RENDERER_MOUSE_OVER_HANDLER = event -> {
		DateListRendererCellViewImpl src = (DateListRendererCellViewImpl) event.getSource();
		src.removeStyleName("max-height-100 overflowHidden");
	};

	@Inject
	public DateListRendererCellViewImpl(JSONObjectAdapter adapter, DateTimeUtils dateTimeUtils) {
		super();
		this.adapter = adapter;
		this.dateTimeUtils = dateTimeUtils;
		addStyleName("max-height-100 overflowHidden");
		addDomHandler(STRING_RENDERER_MOUSE_OVER_HANDLER, MouseOverEvent.getType());
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
			this.setText(newValue.toString());
		} catch (Exception e) {
			this.setText(jsonValue);
		}
	}

	@Override
	public String getValue() {
		return v;
	}
}
