package org.sagebionetworks.web.client.widget.table.v2.results;

import java.util.List;

import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.RowSet;
import org.sagebionetworks.web.client.view.bootstrap.table.TBody;
import org.sagebionetworks.web.client.view.bootstrap.table.THead;
import org.sagebionetworks.web.client.view.bootstrap.table.Table;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnTypeViewEnum;

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
	public void setTableVisible(boolean visible) {
		table.setVisible(visible);
	}

	@Override
	public void resetTableData(List<String> headers,
			List<ColumnTypeViewEnum> types, List<List<String>> rows) {
		table.clear();
		table.add(new StringTableHeader(headers));
		
	}

}
