package org.sagebionetworks.web.client.widget.table.v2.results;

import org.gwtbootstrap3.client.ui.CheckBox;
import org.sagebionetworks.web.client.view.bootstrap.table.TableData;
import org.sagebionetworks.web.client.view.bootstrap.table.TableRow;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.Cell;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * The UiBound implementation of RowView with zero business logic.
 * 
 * @author John
 *
 */
public class RowViewImpl implements RowView {

	public interface Binder extends UiBinder<TableRow, RowViewImpl> {
	}

	@UiField
	CheckBox select;

	TableRow row;

	@Inject
	public RowViewImpl(Binder binder) {
		row = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return row;
	}


	@Override
	public boolean isSelected() {
		return select.getValue();
	}


	@Override
	public void setSelected(boolean isSelected) {
		this.select.setValue(isSelected);
	}

	@Override
	public void setPresenter(final Presenter presenter) {
		this.select.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onSelectionChanged();
			}
		});
	}

	@Override
	public void addCell(Cell cell) {
		TableData td = new TableData();
		td.add(cell);
		row.add(td);
	}

	@Override
	public void setSelectVisible(boolean visible) {
		this.select.setVisible(visible);
	}

}
