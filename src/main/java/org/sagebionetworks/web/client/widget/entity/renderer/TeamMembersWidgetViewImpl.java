package org.sagebionetworks.web.client.widget.entity.renderer;

import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.view.bootstrap.table.Table;
import org.sagebionetworks.web.client.widget.LoadingSpinner;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TeamMembersWidgetViewImpl implements TeamMembersWidgetView {
	@UiField
	Table table;
	@UiField
	Div paginationWidgetContainer;
	@UiField
	Div synAlertContainer;
	@UiField
	LoadingSpinner loadingUI;

	private Widget widget;

	public interface Binder extends UiBinder<Widget, TeamMembersWidgetViewImpl> {
	}

	@Inject
	public TeamMembersWidgetViewImpl(Binder binder) {
		this.widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void addRow(IsWidget w) {
		table.add(w);
	}

	@Override
	public void clearRows() {
		// remove all widgets except for the first one (the table header row)
		for (int i = table.getWidgetCount() - 1; i > 0; i--) {
			table.remove(i);
		}
	}

	@Override
	public void setPaginationWidget(IsWidget w) {
		paginationWidgetContainer.add(w);
	}

	@Override
	public void setSynapseAlert(IsWidget w) {
		synAlertContainer.add(w);
	}

	@Override
	public void setLoadingVisible(boolean visible) {
		loadingUI.setVisible(visible);
		paginationWidgetContainer.setVisible(!visible);
	}
}
