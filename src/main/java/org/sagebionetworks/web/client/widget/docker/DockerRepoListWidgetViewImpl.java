package org.sagebionetworks.web.client.widget.docker;

import org.gwtbootstrap3.client.ui.ListGroup;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.repo.model.docker.DockerRepository;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;

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

	Widget widget;
	PlaceChanger placeChanger;
	public interface Binder extends UiBinder<Widget, DockerRepoListWidgetViewImpl> {}

	@Inject
	public DockerRepoListWidgetViewImpl(Binder binder, GlobalApplicationState globalAppState) {
		this.widget = binder.createAndBindUi(this);
		placeChanger = globalAppState.getPlaceChanger();
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void addRepo(DockerRepository entity) {
		dockerList.add(new DockerRepoListGroupItem(HeadingSize.H4, entity, placeChanger));
	}

	@Override
	public void clear() {
		dockerList.clear();
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
}
