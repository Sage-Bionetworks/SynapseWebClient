package org.sagebionetworks.web.client.widget.table.modal.download;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.select.client.ui.Select;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * View implementation with zero business logic.
 * 
 * @author jhill
 *
 */
public class DownloadTableQueryModalViewImpl implements DownloadTableQueryModalView {
	
	public interface Binder extends UiBinder<Modal, DownloadTableQueryModalViewImpl> {}
	
	@UiField
	Select fileType;
	@UiField
	CheckBox writeHeader;
	@UiField
	CheckBox includeMetadata;
	@UiField
	SimplePanel trackerPanel;
	@UiField
	Alert alert;
	@UiField
	Button primaryButton;
	
	Modal modal;
	
	@Inject
	public DownloadTableQueryModalViewImpl(Binder binder){
		this.modal = binder.createAndBindUi(this);
		// Set the options
		for(FileType type: FileType.values()){
			Option option = new Option();
			option.setValue(type.getDispalyValue());
			fileType.add(option);
		}
	}

	@Override
	public void setFileType(FileType type) {
		fileType.setValue(type.getDispalyValue());
	}

	@Override
	public FileType getFileType() {
		return FileType.findType(fileType.getValue());
	}

	@Override
	public Widget asWidget() {
		return modal;
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

	@Override
	public void setErrorMessage(String message) {
		alert.setText(message);
	}

	@Override
	public void setErrorMessageVisible(boolean visible) {
		alert.setVisible(visible);
	}

	@Override
	public void show() {
		this.modal.show();
	}

	@Override
	public void setLoading(boolean loading) {
		if(loading){
			this.primaryButton.state().loading();
		}else{
			this.primaryButton.state().reset();
		}
	}

	@Override
	public void setPresenter(final Presenter presenter) {
		this.primaryButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				presenter.onPrimary();
			}
		});
		
	}

	@Override
	public void hide() {
		this.modal.hide();
	}

}
