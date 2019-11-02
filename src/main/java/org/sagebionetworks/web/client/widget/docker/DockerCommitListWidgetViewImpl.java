package org.sagebionetworks.web.client.widget.docker;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DockerCommitListWidgetViewImpl implements DockerCommitListWidgetView {
	public interface Binder extends UiBinder<Widget, DockerCommitListWidgetViewImpl> {
	}

	@UiField
	SimplePanel dockerCommitList;
	@UiField
	SimplePanel synAlertContainer;

	Widget widget;
	Presenter presenter;

	@Inject
	public DockerCommitListWidgetViewImpl(Binder binder) {
		this.widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setCommitsContainer(IsWidget widget) {
		dockerCommitList.clear();
		dockerCommitList.add(widget);
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void setSynAlert(Widget widget) {
		synAlertContainer.setWidget(widget);
	}
}
