package org.sagebionetworks.web.client.widget.docker;

import org.gwtbootstrap3.client.ui.ListGroup;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.repo.model.docker.DockerRepository;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.LoadingSpinner;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DockerRepoListWidgetViewImpl implements DockerRepoListWidgetView {
	@UiField
	ListGroup dockerList;
	@UiField
	SimplePanel synAlertContainer;
	@UiField
	Div membersContainer;
	@UiField
	LoadingSpinner loadingUI;
	@UiField
	Span emptyUI;

	CallbackP<String> entityClickedHandler;
	Widget widget;
	public interface Binder extends UiBinder<Widget, DockerRepoListWidgetViewImpl> {}

	@Inject
	public DockerRepoListWidgetViewImpl(Binder binder) {
		this.widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void addRepo(DockerRepository entity) {
		emptyUI.setVisible(false);
		dockerList.add(new DockerRepoListGroupItem(HeadingSize.H4, entity, entityClickedHandler));
	}

	@Override
	public void clear() {
		dockerList.clear();
		emptyUI.setVisible(true);
	}

	@Override
	public void setSynAlert(Widget widget){
		synAlertContainer.add(widget);
	}

	@Override
	public void setSynAlertVisible(boolean visible) {
		synAlertContainer.setVisible(visible);
	}

	@Override
	public void setMembersContainer(LoadMoreWidgetContainer membersContainerW) {
		membersContainer.clear();
		membersContainer.add(membersContainerW.asWidget());
	}
	@Override
	public void setEntityClickedHandler(CallbackP<String> entityClickedHandler) {
		this.entityClickedHandler = entityClickedHandler;
	}
	@Override
	public void setLoadingVisible(boolean visible) {
		loadingUI.setVisible(visible);
	}
}
