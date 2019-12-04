package org.sagebionetworks.web.client.widget.entity.tabs;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class DockerTabViewImpl implements DockerTabView {
	@UiField
	SimplePanel dockerBreadcrumbContainer;
	@UiField
	SimplePanel dockerRepoListWidgetContainer;
	@UiField
	SimplePanel dockerRepoWidgetContainer;
	@UiField
	SimplePanel synapseAlertContainer;

	Presenter presenter;
	Widget widget;

	public interface TabsViewImplUiBinder extends UiBinder<Widget, DockerTabViewImpl> {
	}

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
	public void setDockerRepoList(Widget widget) {
		dockerRepoListWidgetContainer.setWidget(widget);
	}

	@Override
	public void setBreadcrumb(Widget widget) {
		dockerBreadcrumbContainer.setWidget(widget);
	}

	@Override
	public void setSynapseAlert(Widget widget) {
		synapseAlertContainer.setWidget(widget);
	}

	@Override
	public void setDockerRepoWidget(Widget widget) {
		dockerRepoWidgetContainer.setWidget(widget);
	}

	@Override
	public void setBreadcrumbVisible(boolean visible) {
		dockerBreadcrumbContainer.setVisible(visible);
	}

	@Override
	public void setDockerRepoListVisible(boolean visible) {
		dockerRepoListWidgetContainer.setVisible(visible);
	}

	@Override
	public void setDockerRepoUIVisible(boolean visible) {
		dockerRepoWidgetContainer.setVisible(visible);
	}

	@Override
	public void clearDockerRepoWidget() {
		dockerRepoWidgetContainer.clear();
	}
}
