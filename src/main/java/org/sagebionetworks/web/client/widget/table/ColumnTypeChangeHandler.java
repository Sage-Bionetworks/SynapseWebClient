package org.sagebionetworks.web.client.widget.table;

import org.sagebionetworks.repo.model.table.ColumnType;

public interface ColumnTypeChangeHandler {

	void onChange(ColumnType selectedType);
}
