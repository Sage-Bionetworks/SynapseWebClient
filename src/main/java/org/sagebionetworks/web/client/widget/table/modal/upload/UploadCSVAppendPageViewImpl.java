package org.sagebionetworks.web.client.widget.table.modal.upload;

import org.gwtbootstrap3.extras.bootbox.client.Bootbox;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Implementation with no business logic.
 * 
 * @author John
 *
 */
public class UploadCSVAppendPageViewImpl implements UploadCSVAppendPageView {

	public interface Binder extends UiBinder<Widget, UploadCSVAppendPageViewImpl> {
	}

	@UiField
	SimplePanel trackerPanel;

	Widget widget;

	@Inject
	public UploadCSVAppendPageViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void addJobTrackingWidget(IsWidget jobTrackingWidget) {
		this.trackerPanel.add(jobTrackingWidget);
	}

	@Override
	public void setTrackingWidgetVisible(boolean visible) {
		this.trackerPanel.setVisible(visible);
	}

	@Override
	public void showErrorDialog(String message) {
		Bootbox.alert(message);
	}
}
