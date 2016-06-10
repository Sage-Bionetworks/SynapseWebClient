package org.sagebionetworks.web.client.widget.entity.tabs;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class DockerTabViewImpl implements DockerTabView {
	@UiField
	SimplePanel dockerBreadcrumbContainer;
	@UiField
	SimplePanel dockerMetadataContainer;
	@UiField
	SimplePanel dockerTitlebarContainer;
	@UiField
	SimplePanel dockerRepoListWidgetContainer;
	@UiField
	SimplePanel dockerRepoWidgetContainer;
	@UiField
	SimplePanel synapseAlertContainer;
	@UiField
	SimplePanel dockerModifiedAndCreatedContainer;

	Presenter presenter;
	Widget widget;
	public interface TabsViewImplUiBinder extends UiBinder<Widget, DockerTabViewImpl> {}

	public DockerTabViewImpl() {
		TabsViewImplUiBinder binder = GWT.create(TabsViewImplUiBinder.class);
		widget = binder.createAndBindUi(this);
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
	public void setTitlebar(Widget widget) {
		dockerTitlebarContainer.add(widget);
	}

	@Override
	public void setDockerRepoList(Widget widget) {
		dockerRepoListWidgetContainer.add(widget);
	}

	@Override
	public void setBreadcrumb(Widget widget) {
		dockerBreadcrumbContainer.add(widget);
	}

	@Override
	public void setEntityMetadata(Widget widget) {
		dockerMetadataContainer.add(widget);
	}

	@Override
	public void setSynapseAlert(Widget widget) {
		synapseAlertContainer.add(widget);
	}

	@Override
	public void setModifiedCreatedBy(IsWidget widget) {
		dockerModifiedAndCreatedContainer.add(widget);
	}

	@Override
	public void setDockerRepoWidget(Widget widget) {
		dockerRepoWidgetContainer.add(widget);
	}
}
