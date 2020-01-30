package org.sagebionetworks.web.client.widget.table.api;

import org.sagebionetworks.repo.model.table.ColumnType;

public enum ApiTableColumnType {
	STRING(ColumnType.STRING), USERID(ColumnType.USERID), DATE(ColumnType.DATE), ENTITYID(ColumnType.ENTITYID), LARGETEXT(ColumnType.LARGETEXT), MARKDOWN_LINK(null), CANCEL_CONTROL(null);

	private ColumnType columnType;

	ApiTableColumnType(ColumnType columnType) {
		this.columnType = columnType;
	}

	public ColumnType getSynapseTableColumnType() {
		return columnType;
	}
}
