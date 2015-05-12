package org.sagebionetworks.web.client.widget.table.v2.results;

import org.gwtbootstrap3.client.ui.Alert;
import org.sagebionetworks.web.client.widget.asynch.JobTrackingWidget;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * A UiBound query results view with zero business logic.
 * 
 * @author John
 * 
 */
public class TableQueryResultViewImpl implements TableQueryResultView {

	public interface Binder extends UiBinder<Widget, TableQueryResultViewImpl> {
	}

	@UiField
	SimplePanel tablePanel;
	@UiField
	SimplePanel progressPanel;
	@UiField
	SimplePanel rowEditorModalPanel;
	@UiField
	SimplePanel synapseAlertContainer;

	Widget widget;

	Presenter presenter;

	@Inject
	public TableQueryResultViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
	}

	@Override
	public void setPresenter(Presenter presenterin) {
		this.presenter = presenterin;
	}

	@Override
	public void setTableVisible(boolean visible) {
		tablePanel.setVisible(visible);
	}

	@Override
	public void setPageWidget(TablePageWidget pageWidget) {
		tablePanel.add(pageWidget);
	}

	@Override
	public void setErrorVisible(boolean visible) {
		synapseAlertContainer.setVisible(visible);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setEditorWidget(QueryResultEditorWidget queryResultEditor) {
		rowEditorModalPanel.add(queryResultEditor);
	}

	@Override
	public void setProgressWidget(JobTrackingWidget progressWidget) {
		this.progressPanel.add(progressWidget);
	}

	@Override
	public void setProgressWidgetVisible(boolean visible) {
		this.progressPanel.setVisible(visible);
	}
	@Override
	public void setSynapseAlertWidget(Widget w) {
		synapseAlertContainer.setWidget(w);
	}
}
