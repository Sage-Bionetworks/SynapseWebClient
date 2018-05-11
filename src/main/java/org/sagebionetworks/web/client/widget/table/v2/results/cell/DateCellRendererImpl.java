package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import java.util.Date;

import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.StringUtils;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DateCellRendererImpl implements DateCellRenderer {

	StringRendererCell view;
	DateTimeUtils dateTimeUtils;
	String value;
	@Inject
	public DateCellRendererImpl(StringRendererCell view, DateTimeUtils dateTimeUtils) {
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
		this.value = StringUtils.trimWithEmptyAsNull(value);
		if (this.value != null) {
			Date date = new Date(Long.parseLong(this.value));
			view.setValue(dateTimeUtils.getDateTimeString(date));
		}else{
			view.setValue("");
		}
	}

	@Override
	public String getValue() {
		return value;
	}

}
