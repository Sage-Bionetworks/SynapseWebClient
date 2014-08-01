package org.sagebionetworks.web.unitclient.widget.table.v2;

import static org.junit.Assert.*;

import org.junit.Test;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.web.client.widget.table.v2.ColumnTypeViewEnum;

public class ColumnTypeViewEnumTest {

	/**
	 * All types must be supported.
	 * 
	 */
	@Test
	public void testAllTypes(){
		for(ColumnType type: ColumnType.values()){
			assertNotNull(ColumnTypeViewEnum.getViewForType(type));
		}
	}
}
