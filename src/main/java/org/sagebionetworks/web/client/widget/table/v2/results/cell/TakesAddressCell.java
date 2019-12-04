package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.sagebionetworks.web.shared.table.CellAddress;


/**
 * A type of cell than needs to know its full address.
 * 
 * @author John
 *
 */
public interface TakesAddressCell extends Cell {

	/**
	 * The address will be set before the value. s
	 * 
	 * @param address
	 */
	public void setCellAddresss(CellAddress address);
}
