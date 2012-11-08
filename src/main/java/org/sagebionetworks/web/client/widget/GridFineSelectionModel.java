package org.sagebionetworks.web.client.widget;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;

public class GridFineSelectionModel<M extends ModelData> extends GridSelectionModel<M> {
	private boolean userLocked = false;

	public GridFineSelectionModel() {
		super();
	}

	public boolean isUserLocked() {
		return userLocked;
	}

	public void setUserLocked(boolean userLocked) {
		this.userLocked = userLocked;
	}

	@Override
	protected void handleMouseClick(GridEvent<M> e) {
		if (isUserLocked()) return;
		super.handleMouseClick(e);
	}

	@Override
	protected void handleMouseDown(GridEvent<M> e) {
		if (isUserLocked()) return;
		super.handleMouseClick(e);
	}
}

