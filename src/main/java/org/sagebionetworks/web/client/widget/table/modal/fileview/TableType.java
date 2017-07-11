package org.sagebionetworks.web.client.widget.table.modal.fileview;

import org.sagebionetworks.repo.model.table.ViewType;

public enum TableType {
	table(null),
	fileview(ViewType.file),
	projectview(ViewType.project);

	private ViewType viewType;

	TableType(ViewType viewType) {
		this.viewType = viewType;
	}

	public ViewType getViewType() {
		return viewType;
	}
}