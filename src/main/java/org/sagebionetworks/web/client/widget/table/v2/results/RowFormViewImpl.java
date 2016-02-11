package org.sagebionetworks.web.client.widget.table.v2.results;

import org.gwtbootstrap3.client.ui.Form;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.FormLabel;
import org.sagebionetworks.web.client.view.bootstrap.table.TableRow;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.Cell;

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
public class RowFormViewImpl implements RowFormView {
	
	public interface Binder extends UiBinder<TableRow, RowFormViewImpl> {	}
	
	@UiField
	Form form;
	
	Widget w;
	
	@Inject
	public RowFormViewImpl(Binder binder){
		w = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return w;
	}

	@Override
	public void setPresenter(final Presenter presenter) {
	}

	@Override
	public void addCell(String labelText, Cell cell) {
		FormGroup fg = new FormGroup();
		FormLabel label = new FormLabel();
		label.setText(labelText);
		fg.add(label);
		fg.add(cell);
		form.add(fg);
	}
	
	@Override
	public void clear() {
		form.clear();
	}
}
