package org.sagebionetworks.web.client.widget.table.modal.upload;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.Row;
import org.sagebionetworks.repo.model.table.UploadToTablePreviewResult;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UploadPreviewWidgetImpl implements UploadPreviewWidget {
	
	UploadPreviewView view;

	@Inject
	public UploadPreviewWidgetImpl(UploadPreviewView view) {
		super();
		this.view = view;
	}

	@Override
	public Widget asWidget() {
		return this.view.asWidget();
	}

	@Override
	public void configure(UploadToTablePreviewResult preview) {
		// Create a list of headers
		List<String> headers = new ArrayList<String>();
		for(ColumnModel cm: preview.getSuggestedColumns()){
			StringBuilder builder = new StringBuilder();
			builder.append(cm.getName());
			builder.append(" (");
			builder.append(cm.getColumnType().name());
			builder.append(")");
			headers.add(builder.toString());
		}
		view.setHeaders(headers);
		// add each row
		for(Row row: preview.getSampleRows()){
			view.addRow(row.getValues());
		}
	}

	@Override
	public List<ColumnModel> getCurrentModel() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
