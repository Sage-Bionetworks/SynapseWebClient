package org.sagebionetworks.web.client.widget.table.v2.results;

import java.util.Iterator;
import java.util.LinkedList;
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
 * UiBound implementation of a TableView;
 * @author John
 *
 */
public class TablePageViewImpl implements TablePageView {

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
			th.add(new Text(value));
			header.add(th);
		}
	}

	@Override
	public void addRow(RowView newRow) {
		body.add(newRow);
	}

	@Override
	public Iterable<RowView> getRows() {
		List<RowView> list = new LinkedList<RowView>();
		Iterator<Widget> it = body.iterator();
		while(it.hasNext()){
			list.add((RowView) it.next());
		}
		return list;
	}

	@Override
	public void deleteRow(RowView toDelete) {
		body.remove(toDelete);
	}

}
