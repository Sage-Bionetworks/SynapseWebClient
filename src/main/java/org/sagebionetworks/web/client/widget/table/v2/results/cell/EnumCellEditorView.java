package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.IsWidget;


public interface EnumCellEditorView extends IsWidget, TakesValue<String>, Focusable {

	/**
	 * Add an option to this select
	 * @param value
	 */
	void addOption(String value);

}
