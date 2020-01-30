package org.sagebionetworks.web.client.widget.table.v2.results;

import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.FormLabel;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.Cell;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * The UiBound implementation of RowView with zero business logic.
 * 
 * @author Jay
 *
 */
public class RowFormViewImpl implements RowFormView {

	public interface Binder extends UiBinder<Widget, RowFormViewImpl> {
	}

	@UiField
	Div form;

	Widget w;

	@Inject
	public RowFormViewImpl(Binder binder) {
		w = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return w;
	}

	@Override
	public void setPresenter(final Presenter presenter) {}

	@Override
	public void addCell(String labelText, Cell cell) {
		FormGroup fg = new FormGroup();
		fg.addStyleName("margin-top-30");
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
