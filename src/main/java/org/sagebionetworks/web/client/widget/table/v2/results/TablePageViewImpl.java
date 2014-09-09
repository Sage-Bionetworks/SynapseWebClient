package org.sagebionetworks.web.client.widget.table.v2.results;

import java.util.List;

import org.gwtbootstrap3.client.ui.html.Strong;
import org.sagebionetworks.web.client.view.bootstrap.table.TBody;
import org.sagebionetworks.web.client.view.bootstrap.table.TableHeader;
import org.sagebionetworks.web.client.view.bootstrap.table.TableRow;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * UiBound implementation of a TableView with zero business logic.
 * @author John
 *
 */
public class TablePageViewImpl implements TablePageView {
	
	private static String MIN_WIDTH = "75px";

	public interface Binder extends UiBinder<ScrollPanel, TablePageViewImpl> {}
	
	@UiField
	TableRow header;
	@UiField
	TBody body;
	
	ScrollPanel scrollPanel;
	
	@Inject
	public TablePageViewImpl(Binder binder){
		scrollPanel = binder.createAndBindUi(this);
	}
	
	@Override
	public Widget asWidget() {
		return scrollPanel;
	}

	@Override
	public void setTableHeaders(List<String> headers) {
		header.clear();
		body.clear();
		// Blank header for the selection.
		header.add(new TableHeader());
		for(String value: headers){
			TableHeader th = new TableHeader();
			th.setMinimumWidth(MIN_WIDTH);
			th.add(new Strong(value));
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
