package org.sagebionetworks.web.client.widget.table.modal.upload;

import org.sagebionetworks.repo.model.table.CsvTableDescriptor;
import org.sagebionetworks.repo.model.table.UploadToTablePreviewRequest;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CSVOptionsWidgetImpl implements CSVOptionsWidget, CSVOptionsView.Presenter{
	
	CSVOptionsView view;

	String fileHandleId;
	Boolean doFullScan;
	
	ChangeHandler handler;
	
	@Inject
	public CSVOptionsWidgetImpl(CSVOptionsView view) {
		super();
		this.view = view;
		this.view.setPresenter(this);
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void configure(UploadToTablePreviewRequest options, ChangeHandler handler) {
		this.handler = handler;
		CsvTableDescriptor descriptor = options.getCsvTableDescriptor();
		Delimiter delimiter = Delimiter.findDelimiter(descriptor.getSeparator());
		view.setSeparator(delimiter);
		if(Delimiter.OTHER.equals(delimiter)){
			view.setOtherSeparatorValue(descriptor.getSeparator());
		}
		if(descriptor.getIsFirstLineHeader() == null){
			view.setFirsLineIsHeader(true);
		}else{
			view.setFirsLineIsHeader(descriptor.getIsFirstLineHeader());
		}
		this.fileHandleId = options.getUploadFileHandleId();
		this.doFullScan = options.getDoFullFileScan();
		onSeparatorChanged();
	}

	@Override
	public UploadToTablePreviewRequest getCurrentOptions() {
		UploadToTablePreviewRequest request = new UploadToTablePreviewRequest();
		CsvTableDescriptor descriptor = new CsvTableDescriptor();
		request.setCsvTableDescriptor(descriptor);
		Delimiter delimiter = view.getSeparator();
		if(Delimiter.OTHER.equals(delimiter)){
			descriptor.setSeparator(view.getOtherSeparatorValue());
		}else{
			descriptor.setSeparator(delimiter.getDelimiter());
		}
		request.setUploadFileHandleId(this.fileHandleId);
		descriptor.setIsFirstLineHeader(view.getIsFristLineHeader());
		request.setDoFullFileScan(this.doFullScan);
		return request;
	}

	@Override
	public void onSeparatorChanged() {
		if(Delimiter.OTHER.equals(view.getSeparator())){
			view.setOtherSeparatorTextEnabled(true);
		}else{
			view.setOtherSeparatorTextEnabled(false);
			view.clearOtherSeparatorText();
		}
	}

	@Override
	public void onRefreshPreview() {
		handler.optionsChanged();
	}

}
