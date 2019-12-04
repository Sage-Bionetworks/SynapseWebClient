package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import java.util.List;
import com.google.gwt.event.dom.client.HasKeyDownHandlers;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.IsWidget;

public interface ListCellEdtiorView extends IsWidget, TakesValue<Integer>, HasKeyDownHandlers, Focusable {
	/**
	 * Configure before using it.
	 * 
	 * @param values The possible values in the list. Selection is based on the index of each item.
	 */
	public void configure(List<String> values);

}
