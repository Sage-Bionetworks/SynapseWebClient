package org.sagebionetworks.web.client.widget;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class SelectionToolbarPresenter implements SelectableListView.Presenter {
	
	protected List<SelectableListItem> items = new ArrayList<SelectableListItem>();
	private boolean changingSelection = false;
	/**
	 * Fill in way to refresh row data.
	 */
	public abstract void refresh();
	public abstract SelectableListView getView();
	
	public void selectAll() {
		changeAllSelection(true);
	}

	public void selectNone() {
		changeAllSelection(false);
	}

	public void onMoveUp() {
		int index = findFirstSelected();
		SelectableListItem item = items.get(index);
		items.remove(index);
		items.add(index-1, item);
		refresh();
	}

	public void onMoveDown() {
		int index = findFirstSelected();
		SelectableListItem item = items.get(index);
		items.remove(index);
		items.add(index+1, item);
		refresh();
	}

	public void deleteSelected() {
		Iterator<SelectableListItem> it = items.iterator();
		while(it.hasNext()){
			SelectableListItem row = it.next();
			if(row.isSelected()){
				it.remove();
			}
		}
		refresh();
	}

	/**
	 * Find the first selected row.
	 * @return
	 */
	public int findFirstSelected(){
		int index = 0;
		for(SelectableListItem row: items){
			if(row.isSelected()){
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
	private void changeAllSelection(boolean select){
		try{
			changingSelection = true;
			// Select all 
			for(SelectableListItem row: items){
				row.setSelected(select);
			}
		}finally{
			changingSelection = false;
		}
		checkSelectionState();
	}
	
	/**
	 * The current selection state determines which buttons are enabled.
	 */
	public void checkSelectionState(){
		if(!changingSelection && getView() != null){
			int index = 0;
			int count = 0;
			int lastIndex = 0;
			for(SelectableListItem row: items) {
				if(row.isSelected()){
					count++;
					lastIndex = index;
				}
				index++;
			}
			getView().setCanDelete(count > 0);
			getView().setCanMoveUp(count == 1 && lastIndex > 0);
			getView().setCanMoveDown(count == 1 && lastIndex < items.size()-1);
		}
	}
}
