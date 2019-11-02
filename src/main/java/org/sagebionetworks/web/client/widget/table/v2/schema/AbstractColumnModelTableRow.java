package org.sagebionetworks.web.client.widget.table.v2.schema;

import org.gwtbootstrap3.client.ui.CheckBox;
import org.sagebionetworks.web.client.view.bootstrap.table.TableRow;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiField;

/**
 * Abstract ColumnModelTableRow
 * 
 * @author John
 *
 */
public abstract class AbstractColumnModelTableRow implements ColumnModelTableRow {

	@UiField
	CheckBox select;
	SelectionPresenter selectionPresenter;
	/*
	 * The actual <tr> for this editor.
	 */
	TableRow row;


	@Override
	public void setSelected(boolean select) {
		this.select.setValue(select);
	}

	@Override
	public boolean isSelected() {
		return this.select.getValue();
	}

	@Override
	public void setSelectionPresenter(SelectionPresenter selectionPresenterin) {
		this.selectionPresenter = selectionPresenterin;
		select.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				selectionPresenter.selectionChanged(event.getValue());
			}
		});
	}


	@Override
	public void delete() {
		row.removeFromParent();
	}

	@Override
	public void setSelectVisible(boolean visible) {
		select.setVisible(visible);
	}

}
