package org.sagebionetworks.web.unitclient.widget.table.v2.results;

import org.sagebionetworks.web.client.widget.table.v2.results.RowView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.Cell;
import com.google.gwt.user.client.ui.Widget;

/**
 * Simple stub of a RowView.
 * 
 * @author John
 *
 */
public class RowViewStub implements RowView {

	Presenter presenter;
	boolean isSelected = false;

	@Override
	public Widget asWidget() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSelected() {
		return isSelected;
	}

	@Override
	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
		this.presenter.onSelectionChanged();
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void addCell(Cell cell) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSelectVisible(boolean visible) {
		// TODO Auto-generated method stub

	}

}
