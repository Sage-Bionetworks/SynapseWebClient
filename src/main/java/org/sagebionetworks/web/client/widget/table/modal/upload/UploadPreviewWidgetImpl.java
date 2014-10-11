package org.sagebionetworks.web.client.widget.table.modal.upload;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.Row;
import org.sagebionetworks.repo.model.table.UploadToTablePreviewRequest;
import org.sagebionetworks.repo.model.table.UploadToTablePreviewResult;
import org.sagebionetworks.repo.model.table.UploadToTableRequest;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UploadPreviewWidgetImpl implements UploadPreviewWidget {
	
	public static final double COLUMN_SIZE_BUFFER = 0.25;
	public static final int MAX_CHARS_PER_CELL = 10;
	UploadPreviewView view;
	UploadToTablePreviewRequest previewRequest;
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
	public void configure(UploadToTablePreviewRequest previewRequest, UploadToTablePreviewResult preview) {
		this.previewRequest = previewRequest;
		columns = preProcessColumns(preview.getSuggestedColumns());
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

	@Override
	public List<ColumnModel> getCurrentModel() {
		return columns;
	}

	@Override
	public UploadToTableRequest getUploadRequest() {
		UploadToTableRequest request = new UploadToTableRequest();
		request.setCsvTableDescriptor(previewRequest.getCsvTableDescriptor());
		request.setLinesToSkip(previewRequest.getLinesToSkip());
		request.setUploadFileHandleId(previewRequest.getUploadFileHandleId());
		return request;
	}

	/**
	 * Pre-process the passed columns.  Returns a cloned list of ColumnModels, each modified as needed.
	 * @param adapter
	 * @param columns
	 * @return
	 * @throws JSONObjectAdapterException
	 */
	public List<ColumnModel> preProcessColumns(List<ColumnModel> columns) {
		for(ColumnModel cm: columns){
			if(cm.getMaximumSize() != null){
				// Add a buffer to the max size
				double startingMax = cm.getMaximumSize();
				cm.setMaximumSize((long)(startingMax+(startingMax*COLUMN_SIZE_BUFFER)));
			}
		}
		return columns;
	}
	
}
