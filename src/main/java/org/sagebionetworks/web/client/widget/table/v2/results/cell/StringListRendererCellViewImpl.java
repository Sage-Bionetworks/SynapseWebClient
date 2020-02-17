package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.sagebionetworks.schema.adapter.JSONArrayAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.inject.Inject;

/**
 * A non editable renderer for a list of strings (used for integers, strings, and booleans).
 *
 */
public class StringListRendererCellViewImpl extends Paragraph implements StringListRendererCellView {
	String v;
	JSONObjectAdapter adapter;
	public static final MouseOverHandler STRING_RENDERER_MOUSE_OVER_HANDLER = event -> {
		StringListRendererCellViewImpl src = (StringListRendererCellViewImpl) event.getSource();
		src.removeStyleName("max-height-100 overflowHidden");
	};

	@Inject
	public StringListRendererCellViewImpl(JSONObjectAdapter adapter) {
		super();
		this.adapter = adapter;
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
				newValue.append(parsedJson.get(i));
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
