package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.CheckBoxState;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class APITableColumnManager implements APITableColumnManagerView.Presenter, SynapseWidgetPresenter {

	private APITableColumnManagerView view;
	private PortalGinInjector ginInjector;
	private boolean changingSelection = false;
	private Callback selectionChangedCallback;
	private List<APITableColumnConfigView> columns;

	@Inject
	public APITableColumnManager(APITableColumnManagerView view, PortalGinInjector ginInjector) {
		this.view = view;
		this.ginInjector = ginInjector;
		view.setPresenter(this);
		selectionChangedCallback = new Callback() {
			@Override
			public void invoke() {
				checkSelectionState();
			}
		};
	}

	@Override
	public void configure(List<APITableColumnConfig> configs) {
		columns = new ArrayList<APITableColumnConfigView>();
		if (configs != null) {
			for (APITableColumnConfig data : configs) {
				APITableColumnConfigView column = ginInjector.getAPITableColumnConfigView();
				column.setSelectionChangedCallback(selectionChangedCallback);
				column.configure(data);
				columns.add(column);
			}
		}
		refreshColumns();
	}


	private void refreshColumns() {
		view.clearColumns();
		for (APITableColumnConfigView column : columns) {
			view.addColumn(column.asWidget());
		}
		boolean columnsVisible = columns.size() > 0;
		view.setButtonToolbarVisible(columnsVisible);
		view.setHeaderColumnsVisible(columnsVisible);
		view.setNoColumnsUIVisible(!columnsVisible);
		checkSelectionState();
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void addColumnConfig() {
		APITableColumnConfigView column = ginInjector.getAPITableColumnConfigView();
		column.setSelectionChangedCallback(selectionChangedCallback);
		column.configure(new APITableColumnConfig());
		columns.add(column);
		refreshColumns();
	}


	public void selectAll() {
		changeAllSelection(true);
	}

	public void selectNone() {
		changeAllSelection(false);
	}

	public void onMoveUp() {
		int index = findFirstSelected();
		APITableColumnConfigView sourceEditor = columns.get(index);
		columns.remove(index);
		columns.add(index - 1, sourceEditor);
		refreshColumns();
	}

	public void onMoveDown() {
		int index = findFirstSelected();
		APITableColumnConfigView sourceEditor = columns.get(index);
		columns.remove(index);
		columns.add(index + 1, sourceEditor);
		refreshColumns();
	}

	public void deleteSelected() {
		Iterator<APITableColumnConfigView> it = columns.iterator();
		while (it.hasNext()) {
			APITableColumnConfigView column = it.next();
			if (column.isSelected()) {
				it.remove();
			}
		}
		refreshColumns();
	}

	/**
	 * Find the first selected row.
	 * 
	 * @return
	 */
	private int findFirstSelected() {
		int index = 0;
		for (APITableColumnConfigView row : columns) {
			if (row.isSelected()) {
				return index;
			}
			index++;
		}
		throw new IllegalStateException("Nothing selected");
	}

	public void selectionChanged(boolean isSelected) {
		checkSelectionState();
	}

	/**
	 * Change the selection state of all rows to the passed value.
	 * 
	 * @param select
	 */
	private void changeAllSelection(boolean select) {
		try {
			changingSelection = true;
			// Select all
			for (APITableColumnConfigView column : columns) {
				column.setSelected(select);
			}
		} finally {
			changingSelection = false;
		}
		checkSelectionState();
	}

	/**
	 * The current selection state determines which buttons are enabled.
	 */
	public void checkSelectionState() {
		if (!changingSelection) {
			int index = 0;
			int count = 0;
			int lastIndex = 0;
			for (APITableColumnConfigView column : columns) {
				if (column.isSelected()) {
					count++;
					lastIndex = index;
				}
				index++;
			}
			view.setCanDelete(count > 0);
			view.setCanMoveUp(count == 1 && lastIndex > 0);
			view.setCanMoveDown(count == 1 && lastIndex < columns.size() - 1);

			CheckBoxState state = CheckBoxState.getStateFromCount(count, columns.size());
			view.setSelectionState(state);
		}
	}

	// expose for unit testing purposes
	public List<APITableColumnConfig> getColumnConfigs() {
		List<APITableColumnConfig> newConfigs = new ArrayList<APITableColumnConfig>();
		for (APITableColumnConfigView column : columns) {
			newConfigs.add(column.getConfig());
		}
		return newConfigs;
	}

	public List<APITableColumnConfigView> getColumnEditors() {
		return columns;
	}
}
