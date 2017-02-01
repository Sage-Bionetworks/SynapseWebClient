package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import java.util.Date;

import org.sagebionetworks.web.client.StringUtils;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DateCellRendererImpl implements DateCellRenderer {

	public static final String FORMAT = "yyyy/MM/dd HH:mm:ss";
	DateCellRendererView view;
	private Long originalTime;
	
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
			originalTime = Long.parseLong(value);
			Date date = new Date(originalTime);
			view.setValue(date);
		}else{
			view.clear();
		}
		
	}

	@Override
	public String getValue() {
		Date date = view.getValue();
		if (date != null) {
			Long time = date.getTime();
			if (originalTime != null) {
				double originalSeconds = Math.floor(originalTime / 1000);
				double newSeconds = Math.floor(time / 1000);
				if (originalSeconds == newSeconds) {
					time = originalTime;
				}
			}
			
			return Long.toString(time);
		}
		return null;
	}

}
