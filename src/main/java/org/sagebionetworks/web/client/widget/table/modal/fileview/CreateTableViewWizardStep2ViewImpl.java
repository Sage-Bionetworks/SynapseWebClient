package org.sagebionetworks.web.client.widget.table.modal.fileview;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CreateTableViewWizardStep2ViewImpl implements CreateTableViewWizardStep2View {

	public interface Binder extends UiBinder<Widget, CreateTableViewWizardStep2ViewImpl> {
	}

	@UiField
	SimplePanel editorContainer;
	@UiField
	SimplePanel jobTrackerContainer;

	Widget widget;

	@Inject
	public CreateTableViewWizardStep2ViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setEditor(IsWidget editorWidget) {
		editorContainer.clear();
		editorContainer.setWidget(editorWidget);
	}

	@Override
	public void setJobTracker(IsWidget jobTracker) {
		jobTrackerContainer.clear();
		jobTrackerContainer.setWidget(jobTracker);
	}

	@Override
	public void setJobTrackerVisible(boolean visible) {
		jobTrackerContainer.setVisible(visible);
	}
}
