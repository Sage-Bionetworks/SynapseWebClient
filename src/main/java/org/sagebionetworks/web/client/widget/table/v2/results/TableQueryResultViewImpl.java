package org.sagebionetworks.web.client.widget.table.v2.results;

import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.asynch.JobTrackingWidget;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
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
	@UiField
	Div scrollTarget;
	@UiField
	Div facetsWidgetPanel;
	@UiField
	Div totalVisibleResultsPanel;
	@UiField
	Heading resultCountHeader;

	Widget widget;

	Presenter presenter;
	SynapseJSNIUtils jsniUtils;

	@Inject
	public TableQueryResultViewImpl(Binder binder, SynapseJSNIUtils jsniUtils) {
		widget = binder.createAndBindUi(this);
		this.jsniUtils = jsniUtils;
		facetsWidgetPanel.addStyleName("pull-left-unless-xs margin-right-10 padding-10");
	}

	@Override
	public void setPresenter(Presenter presenterin) {
		this.presenter = presenterin;
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
		progressPanel.clear();
		progressPanel.add(progressWidget);
	}

	@Override
	public void setProgressWidgetVisible(boolean visible) {
		this.progressPanel.setVisible(visible);
	}

	@Override
	public void setSynapseAlertWidget(Widget w) {
		synapseAlertContainer.setWidget(w);
	}

	@Override
	public void scrollTableIntoView() {
		jsniUtils.scrollIntoView(scrollTarget.getElement());
	}

	@Override
	public void setResultCount(Long resultCount) {
		if (resultCount == null || new Long(0).equals(resultCount)) {
			resultCountHeader.setText("No Results");
		} else if (new Long(1).equals(resultCount)) {
			resultCountHeader.setText("1 Result");
		} else {
			resultCountHeader.setText(NumberFormat.getDecimalFormat().format(resultCount) + " Results");
		}
	}

	@Override
	public void setResultCountVisible(boolean visible) {
		resultCountHeader.setVisible(visible);
	}

	@Override
	public void setFacetsWidget(IsWidget w) {
		facetsWidgetPanel.clear();
		facetsWidgetPanel.add(w);
	}

	@Override
	public void setFacetsVisible(boolean visible) {
		facetsWidgetPanel.setVisible(visible);
	}

	@Override
	public void setTotalVisibleResultsPanel(IsWidget w) {
		totalVisibleResultsPanel.clear();
		totalVisibleResultsPanel.add(w);
	}
}
