package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import java.util.List;

public interface EnumCellEditor extends CellEditor {
	
	/**
	 * Must configure this editor before use.
	 * 
	 * @param validValues The list of valid values for this enumeration.
	 */
	public void configure(List<String> validValues);

}
