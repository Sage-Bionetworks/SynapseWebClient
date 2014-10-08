package org.sagebionetworks.web.client.widget.table.modal.upload;

import org.gwtbootstrap3.client.ui.TextBox;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
/**
 * UploadCSVPreviewView with zero business logic.
 * 
 * @author John
 *
 */
public class UploadCSVConfigurationViewImpl implements UploadCSVConfigurationView {

	public interface Binder extends UiBinder<Widget, UploadCSVConfigurationViewImpl> {}
	
	@UiField
	TextBox tableNameBox;
	@UiField
	SimplePanel previewPanel;
	@UiField
	SimplePanel trackerPanel;
	
	Widget widget;
	Presenter presenter;
	
	@Inject
	public UploadCSVConfigurationViewImpl(Binder binder){
		this.widget = binder.createAndBindUi(this);
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void setTableName(String fileName) {
		tableNameBox.setText(fileName);
	}

	@Override
	public void setTrackingWidget(Widget tracker) {
		this.trackerPanel.add(tracker);
	}

	@Override
	public void setTrackerVisible(boolean visible) {
		trackerPanel.setVisible(visible);
	}

	@Override
	public void setPreviewVisible(boolean visible) {
		this.previewPanel.setVisible(visible);
	}

	@Override
	public void setPreviewWidget(Widget uploadPreviewWidget) {
		this.previewPanel.add(uploadPreviewWidget);
	}

}
