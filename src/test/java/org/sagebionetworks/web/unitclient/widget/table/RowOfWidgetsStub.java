package org.sagebionetworks.web.unitclient.widget.table;

import java.util.List;
import org.sagebionetworks.web.client.widget.table.KeyboardNavigationHandler.RowOfWidgets;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * Simply wraps a list of widgets.
 * 
 * @author John
 *
 */
public class RowOfWidgetsStub implements RowOfWidgets {

	List<IsWidgetStub> widgets;

	public RowOfWidgetsStub(List<IsWidgetStub> widgets) {
		this.widgets = widgets;
	}

	@Override
	public IsWidget getWidget(int index) {
		return this.widgets.get(index);
	}

	@Override
	public int getWidgetCount() {
		return this.widgets.size();
	}

}
