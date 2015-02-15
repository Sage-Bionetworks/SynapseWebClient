package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import java.util.Date;

import org.sagebionetworks.web.client.StringUtils;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DateCellRendererImpl implements DateCellRenderer {

	public static final String FORMAT = "yyyy/MM/dd HH:mm:ss";
	DateCellRendererView view;

	@Inject
	public DateCellRendererImpl(DateCellRendererView view) {
		this.view = view;
		this.view.setFormat(FORMAT);
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void setValue(String value) {
		// Strings come in as longs
		value = StringUtils.trimWithEmptyAsNull(value);
		if (value != null) {
			Date date = new Date(Long.parseLong(value));
			view.setValue(date);
		}else{
			view.clear();
		}
		
	}

	@Override
	public String getValue() {
		Date date = view.getValue();
		if (date != null) {
			return Long.toString(date.getTime());
		}
		return null;
	}

}
