package org.sagebionetworks.web.unitclient.widget.table.v2.schema;

import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnTypeViewEnum;

public class ColumnTypeViewEnumTest {

	/**
	 * All types must be supported.
	 * 
	 */
	@Test
	public void testAllTypes() {
		for (ColumnType type : ColumnType.values()) {
			assertNotNull(ColumnTypeViewEnum.getViewForType(type));
		}
	}
}
