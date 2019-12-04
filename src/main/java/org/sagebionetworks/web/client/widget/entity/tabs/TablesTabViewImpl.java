package org.sagebionetworks.web.client.widget.entity.tabs;

import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.html.Div;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TablesTabViewImpl implements TablesTabView {

	@UiField
	SimplePanel tableTitlebarContainer;

	// tables
	@UiField
	SimplePanel tableBreadcrumbContainer;
	@UiField
	SimplePanel tableMetadataContainer;
	@UiField
	SimplePanel tableActionMenuContainer;
	@UiField
	SimplePanel tableWidgetContainer;
	@UiField
	SimplePanel tableModifiedAndCreatedContainer;
	@UiField
	SimplePanel tableListWidgetContainer;
	@UiField
	SimplePanel synapseAlertContainer;
	@UiField
	Column provenanceContainer;
	@UiField
	Div provenanceContainerHighlightBox;

	public interface TabsViewImplUiBinder extends UiBinder<Widget, TablesTabViewImpl> {
	}

	Widget widget;

	@Inject
	public TablesTabViewImpl(TabsViewImplUiBinder binder) {
		widget = binder.createAndBindUi(this);
		initClickHandlers();
	}

	private void initClickHandlers() {}

	@Override
	public void setBreadcrumb(Widget w) {
		tableBreadcrumbContainer.setWidget(w);
	}

	@Override
	public void setTableList(Widget w) {
		tableListWidgetContainer.setWidget(w);
	}

	@Override
	public void setTitlebar(Widget w) {
		tableTitlebarContainer.setWidget(w);
	}

	@Override
	public void setEntityMetadata(Widget w) {
		tableMetadataContainer.setWidget(w);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setTableEntityWidget(Widget w) {
		tableWidgetContainer.setWidget(w);
	}

	@Override
	public void clearTableEntityWidget() {
		tableWidgetContainer.clear();
	}

	@Override
	public void setActionMenu(Widget w) {
		tableActionMenuContainer.setWidget(w);
	}

	@Override
	public void clearActionMenuContainer() {
		tableActionMenuContainer.clear();
	}

	@Override
	public void setSynapseAlert(Widget w) {
		synapseAlertContainer.setWidget(w);
	}

	@Override
	public void setBreadcrumbVisible(boolean visible) {
		tableBreadcrumbContainer.setVisible(visible);
	}

	@Override
	public void setEntityMetadataVisible(boolean visible) {
		tableMetadataContainer.setVisible(visible);
	}

	@Override
	public void setTableListVisible(boolean visible) {
		tableListWidgetContainer.setVisible(visible);
	}

	@Override
	public void setTitlebarVisible(boolean visible) {
		tableTitlebarContainer.setVisible(visible);
	}

	@Override
	public void setModifiedCreatedBy(IsWidget modifiedCreatedBy) {
		tableModifiedAndCreatedContainer.setWidget(modifiedCreatedBy);
	}

	@Override
	public void setTableUIVisible(boolean visible) {
		provenanceContainer.setVisible(visible);
	}

	@Override
	public void setProvenance(IsWidget w) {
		provenanceContainerHighlightBox.clear();
		provenanceContainerHighlightBox.add(w);
	}
}

