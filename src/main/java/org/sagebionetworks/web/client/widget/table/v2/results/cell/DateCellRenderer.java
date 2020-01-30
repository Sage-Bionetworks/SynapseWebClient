package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import java.util.Date;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.StringUtils;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DateCellRenderer implements Cell {

	StringRendererCellView view;
	DateTimeUtils dateTimeUtils;
	String value;

	@Inject
	public DateCellRenderer(StringRendererCellView view, DateTimeUtils dateTimeUtils) {
		this.view = view;
		this.dateTimeUtils = dateTimeUtils;
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void setValue(String value) {
		// Strings come in as longs
		this.value = StringUtils.emptyAsNull(value);
		if (this.value != null) {
			Date date = new Date(Long.parseLong(this.value));
			view.setValue(dateTimeUtils.getDateTimeString(date));
		} else {
			view.setValue("");
		}
	}

	@Override
	public String getValue() {
		return value;
	}

}
