package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import java.util.Date;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.GlobalApplicationStateImpl;
import org.sagebionetworks.web.client.StringUtils;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DateCellEditor implements CellEditor {

	private DateCellEditorView view;
	private Long originalTime;
	GlobalApplicationState globalAppState;

	@Inject
	public DateCellEditor(DateCellEditorView view, GlobalApplicationState globalAppState) {
		this.view = view;
		this.globalAppState = globalAppState;
	}

	@Override
	public boolean isValid() {
		// The editor will not allow bad values.
		return true;
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void setValue(String value) {
		value = StringUtils.emptyAsNull(value);
		Date date = null;
		originalTime = null;
		if (value != null) {
			originalTime = Long.parseLong(value);
			Long time = originalTime;
			if (globalAppState.isShowingUTCTime()) {
				time += GlobalApplicationStateImpl.getTimezoneOffsetMs();
			}
			date = new Date(time);
		}
		view.setValue(date);
	}

	@Override
	public String getValue() {
		Date date = view.getValue();
		if (date != null) {
			Long time = date.getTime();
			if (globalAppState.isShowingUTCTime()) {
				time -= GlobalApplicationStateImpl.getTimezoneOffsetMs();
			}
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

	@Override
	public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
		return view.addKeyDownHandler(handler);
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		view.fireEvent(event);
	}

	@Override
	public int getTabIndex() {
		return view.getTabIndex();
	}

	@Override
	public void setAccessKey(char key) {
		view.setAccessKey(key);
	}

	@Override
	public void setFocus(boolean focused) {
		view.setFocus(focused);
	}

	@Override
	public void setTabIndex(int index) {
		view.setTabIndex(index);
	}

}
