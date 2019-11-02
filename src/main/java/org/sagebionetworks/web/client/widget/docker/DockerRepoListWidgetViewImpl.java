package org.sagebionetworks.web.client.widget.docker;

import java.util.HashMap;
import java.util.Map;
import org.gwtbootstrap3.client.ui.ListGroup;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.repo.model.EntityHeader;
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
	Map<String, DockerRepoListGroupItem> id2RepoListGroupItem = new HashMap<>();

	public interface Binder extends UiBinder<Widget, DockerRepoListWidgetViewImpl> {
	}

	@Inject
	public DockerRepoListWidgetViewImpl(Binder binder) {
		this.widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void addRepo(EntityHeader entityHeader) {
		emptyUI.setVisible(false);
		DockerRepoListGroupItem groupItem = new DockerRepoListGroupItem(HeadingSize.H4, entityHeader, entityClickedHandler);
		id2RepoListGroupItem.put(entityHeader.getId(), groupItem);
		dockerList.add(groupItem);
	}

	@Override
	public void setDockerRepository(DockerRepository entity) {
		DockerRepoListGroupItem groupItem = id2RepoListGroupItem.get(entity.getId());
		if (groupItem != null) {
			groupItem.setDockerRepositoryName(entity.getRepositoryName());
		}
	}

	@Override
	public void clear() {
		dockerList.clear();
		emptyUI.setVisible(true);
		id2RepoListGroupItem.clear();
	}

	@Override
	public void setSynAlert(Widget widget) {
		synAlertContainer.setWidget(widget);
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
