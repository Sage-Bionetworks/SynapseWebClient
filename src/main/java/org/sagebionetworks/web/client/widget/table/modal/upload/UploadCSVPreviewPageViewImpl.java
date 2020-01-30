package org.sagebionetworks.web.client.widget.table.modal.upload;

import org.gwtbootstrap3.client.ui.Button;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * UploadCSVPreviewView with zero business logic.
 * 
 * @author John
 *
 */
public class UploadCSVPreviewPageViewImpl implements UploadCSVPreviewPageView {

	public interface Binder extends UiBinder<Widget, UploadCSVPreviewPageViewImpl> {
	}

	@UiField
	SimplePanel previewPanel;
	@UiField
	SimplePanel trackerPanel;
	@UiField
	SimplePanel optionsPanel;
	@UiField
	Button optionsButton;
	Widget widget;
	Presenter presenter;

	@Inject
	public UploadCSVPreviewPageViewImpl(Binder binder) {
		this.widget = binder.createAndBindUi(this);
		optionsButton.addClickHandler(event -> {
			// toggle visibility
			optionsPanel.setVisible(!optionsPanel.isVisible());
		});
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
	public void setTrackingWidget(IsWidget tracker) {
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
	public void setPreviewWidget(IsWidget uploadPreviewWidget) {
		this.previewPanel.add(uploadPreviewWidget);
	}

	@Override
	public void setCSVOptionsWidget(IsWidget asWidget) {
		this.optionsPanel.add(asWidget);
	}

}
