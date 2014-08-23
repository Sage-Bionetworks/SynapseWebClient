package org.sagebionetworks.web.client.widget.table.v2;

import org.sagebionetworks.web.client.view.bootstrap.table.TBody;
import org.sagebionetworks.web.client.view.bootstrap.table.THead;
import org.sagebionetworks.web.client.view.bootstrap.table.Table;

import com.google.gwt.uibinder.client.UiField;

public class TableQueryResultViewImpl implements TableQueryResultView {
	
	@UiField
	Table table;
	
	Presenter presenter;
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void configureTableData(THead header, TBody body) {
		table.clear();
		table.add(header);
		table.add(body);
	}

	@Override
	public void setTableVisible(boolean visible) {
		table.setVisible(visible);
	}

}
