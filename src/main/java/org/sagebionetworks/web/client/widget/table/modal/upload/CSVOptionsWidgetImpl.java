package org.sagebionetworks.web.client.widget.table.modal.upload;

import org.sagebionetworks.repo.model.table.CsvTableDescriptor;
import org.sagebionetworks.repo.model.table.UploadToTablePreviewRequest;
import org.sagebionetworks.repo.model.table.UploadToTableRequest;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CSVOptionsWidgetImpl implements CSVOptionsWidget {
	
	CSVOptionsView view;

	@Inject
	public CSVOptionsWidgetImpl(CSVOptionsView view) {
		super();
		this.view = view;
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void configure(UploadToTablePreviewRequest options,
			ChangeHandler handler) {
		CsvTableDescriptor descriptor = options.getCsvTableDescriptor();
		Delimiter delimiter = Delimiter.findDelimiter(descriptor.getSeparator());
		view.setSeparator(delimiter);
		if(Delimiter.OTHER.equals(delimiter)){
			view.setOtherSeparatorValue(descriptor.getSeparator());
		}
	}

	@Override
	public UploadToTableRequest getCurrentOptions() {
		UploadToTableRequest request = new UploadToTableRequest();
		CsvTableDescriptor descriptor = new CsvTableDescriptor();
		request.setCsvTableDescriptor(descriptor);
		Delimiter delimiter = view.getSeparator();
		if(Delimiter.OTHER.equals(delimiter)){
			descriptor.setSeparator(view.getOtherSeparatorValue());
		}else{
			descriptor.setSeparator(delimiter.getDelimiter());
		}
		return request;
	}

}
