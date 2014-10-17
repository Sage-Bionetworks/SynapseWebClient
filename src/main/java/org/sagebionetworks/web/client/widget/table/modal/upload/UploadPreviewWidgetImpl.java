package org.sagebionetworks.web.client.widget.table.modal.upload;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.Row;
import org.sagebionetworks.repo.model.table.UploadToTablePreviewResult;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UploadPreviewWidgetImpl implements UploadPreviewWidget {

	public static final int MAX_CHARS_PER_CELL = 10;
	UploadPreviewView view;
	List<ColumnModel> columns;

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
		columns = preview.getSuggestedColumns();
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
		if(preview.getSampleRows() != null){
			for(Row row: preview.getSampleRows()){
				List<String> values = new ArrayList<String>(row.getValues().size());
				for(String value: row.getValues()){
					values.add(truncateValues(value));
				}
				view.addRow(values);
			}
		}

	}
	
	private String truncateValues(String in){
		if(in == null){
			return null;
		}
		if(in.length() > MAX_CHARS_PER_CELL){
			return in.substring(0, MAX_CHARS_PER_CELL-1)+"...";
		}
		return in;
	}
}
