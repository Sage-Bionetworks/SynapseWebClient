package org.sagebionetworks.web.client.widget.table.modal.download;

import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.Radio;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CreateDownloadPageViewImpl implements CreateDownloadPageView {

	public interface Binder extends UiBinder<Widget, CreateDownloadPageViewImpl> {
	}

	@UiField
	Radio commaRadio;
	@UiField
	Radio tabRadio;
	@UiField
	CheckBox writeHeader;
	@UiField
	CheckBox includeMetadata;
	@UiField
	SimplePanel trackerPanel;

	Widget widget;

	@Inject
	public CreateDownloadPageViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
	}

	@Override
	public void setFileType(FileType type) {
		if (FileType.CSV.equals(type)) {
			commaRadio.setValue(true);
		} else {
			tabRadio.setValue(true);
		}
	}

	@Override
	public FileType getFileType() {
		if (commaRadio.getValue()) {
			return FileType.CSV;
		} else {
			return FileType.TSV;
		}
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setIncludeHeaders(boolean include) {
		writeHeader.setValue(include);
	}

	@Override
	public boolean getIncludeHeaders() {
		return writeHeader.getValue();
	}

	@Override
	public void setIncludeRowMetadata(boolean include) {
		this.includeMetadata.setValue(include);
	}

	@Override
	public boolean getIncludeRowMetadata() {
		return includeMetadata.getValue();
	}

	@Override
	public void addTrackerWidget(IsWidget trackerWidget) {
		this.trackerPanel.add(trackerWidget);
	}

	@Override
	public void setTrackerVisible(boolean visible) {
		this.trackerPanel.setVisible(visible);
	}

}
