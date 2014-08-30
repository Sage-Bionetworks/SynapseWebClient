package org.sagebionetworks.web.client.widget.table.v2.results;

import java.util.List;

import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.web.client.view.bootstrap.table.TBody;
import org.sagebionetworks.web.client.view.bootstrap.table.Table;
import org.sagebionetworks.web.client.view.bootstrap.table.TableHeader;
import org.sagebionetworks.web.client.view.bootstrap.table.TableRow;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * UiBound implementation of a TableView with zero business logic.
 * @author John
 *
 */
public class TablePageViewImpl implements TablePageView {
	
	private static String MIN_WIDTH = "75px";

	public interface Binder extends UiBinder<Table, TablePageViewImpl> {}
	
	@UiField
	TableRow header;
	@UiField
	TBody body;
	
	Table table;
	
	@Inject
	public TablePageViewImpl(Binder binder){
		table = binder.createAndBindUi(this);
	}
	
	@Override
	public Widget asWidget() {
		return table;
	}

	@Override
	public void setTableHeaders(List<String> headers) {
		header.clear();
		// Blank header for the selection.
		header.add(new TableHeader());
		for(String value: headers){
			TableHeader th = new TableHeader();
			th.setMinimumWidth(MIN_WIDTH);
			th.add(new Text(value));
			header.add(th);
		}
	}

	@Override
	public void addRow(RowWidget newRow) {
		body.add(newRow);
	}

	@Override
	public void removeRow(RowWidget row) {
		body.remove(row);
	}

}
