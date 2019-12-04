package org.sagebionetworks.web.client.widget.table.modal.upload;

import org.sagebionetworks.repo.model.table.CsvTableDescriptor;
import org.sagebionetworks.repo.model.table.UploadToTablePreviewRequest;
import org.sagebionetworks.web.client.utils.Callback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CSVOptionsWidget implements CSVOptionsView.Presenter, IsWidget {

	CSVOptionsView view;

	String fileHandleId;
	Boolean doFullScan;

	Callback handler;

	@Inject
	public CSVOptionsWidget(CSVOptionsView view) {
		super();
		this.view = view;
		this.view.setPresenter(this);
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	public void configure(UploadToTablePreviewRequest options, Callback handler) {
		this.handler = handler;
		CsvTableDescriptor descriptor = options.getCsvTableDescriptor();
		Delimiter delimiter = Delimiter.findDelimiter(descriptor.getSeparator());
		view.setSeparator(delimiter);
		if (Delimiter.OTHER.equals(delimiter)) {
			view.setOtherSeparatorValue(descriptor.getSeparator());
		}
		EscapeCharacter escapeCharacter = EscapeCharacter.findCharacter(descriptor.getEscapeCharacter());
		view.setEscapeCharacter(escapeCharacter);
		if (EscapeCharacter.OTHER.equals(escapeCharacter)) {
			view.setOtherEscapeCharacterValue(descriptor.getEscapeCharacter());
		}

		if (descriptor.getIsFirstLineHeader() == null) {
			view.setFirsLineIsHeader(true);
		} else {
			view.setFirsLineIsHeader(descriptor.getIsFirstLineHeader());
		}
		this.fileHandleId = options.getUploadFileHandleId();
		this.doFullScan = options.getDoFullFileScan();
		onSeparatorChanged();
		onEscapeCharacterChanged();
	}

	public UploadToTablePreviewRequest getCurrentOptions() {
		UploadToTablePreviewRequest request = new UploadToTablePreviewRequest();
		CsvTableDescriptor descriptor = new CsvTableDescriptor();
		request.setCsvTableDescriptor(descriptor);
		Delimiter delimiter = view.getSeparator();
		if (Delimiter.OTHER.equals(delimiter)) {
			descriptor.setSeparator(view.getOtherSeparatorValue());
		} else {
			descriptor.setSeparator(delimiter.getDelimiter());
		}

		EscapeCharacter escapeCharacter = view.getEscapeCharacter();
		if (EscapeCharacter.OTHER.equals(escapeCharacter)) {
			descriptor.setEscapeCharacter(view.getOtherEscapeCharacterValue());
		} else {
			descriptor.setEscapeCharacter(escapeCharacter.getCharacter());
		}

		request.setUploadFileHandleId(this.fileHandleId);
		descriptor.setIsFirstLineHeader(view.getIsFristLineHeader());
		request.setDoFullFileScan(this.doFullScan);
		return request;
	}

	@Override
	public void onSeparatorChanged() {
		if (Delimiter.OTHER.equals(view.getSeparator())) {
			view.setOtherSeparatorTextEnabled(true);
		} else {
			view.setOtherSeparatorTextEnabled(false);
			view.clearOtherSeparatorText();
		}
	}

	@Override
	public void onEscapeCharacterChanged() {
		if (EscapeCharacter.OTHER.equals(view.getEscapeCharacter())) {
			view.setOtherEscapeCharacterTextEnabled(true);
		} else {
			view.setOtherEscapeCharacterTextEnabled(false);
			view.clearOtherEscapeCharacterText();
		}
	}


	@Override
	public void onRefreshPreview() {
		handler.invoke();
	}

}
