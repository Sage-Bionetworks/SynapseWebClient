package org.sagebionetworks.web.client.widget.docker;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ListGroup;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.docker.DockerRepository;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DockerRepoListWidgetViewImpl implements DockerRepoListWidgetView {
	@UiField
	ListGroup dockerList;
	@UiField
	Button addExternalRepo;
	@UiField
	SimplePanel addExternalRepoModalPanel;
	@UiField
	SimplePanel synAlertContainer;
	@UiField
	Div membersContainer;

	Widget widget;
	Presenter presenter;

	public interface Binder extends UiBinder<Widget, DockerRepoListWidgetViewImpl> {}

	@Inject
	public DockerRepoListWidgetViewImpl(Binder binder) {
		this.widget = binder.createAndBindUi(this);
		addExternalRepo.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				presenter.onClickAddExternalRepo();
			}
		});
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void addRepo(final EntityBundle bundle) {
		dockerList.add(new DockerRepoListGroupItem(HeadingSize.H4, (DockerRepository) bundle.getEntity(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onRepoClicked(bundle);
			}
		}));
	}

	@Override
	public void clear() {
		dockerList.clear();
	}

	@Override
	public void setAddExternalRepoButtonVisible(boolean visibile) {
		addExternalRepo.setVisible(visibile);
	}

	@Override
	public void addExternalRepoModal(IsWidget addExternalRepoModel) {
		this.addExternalRepoModalPanel.add(addExternalRepoModel);
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
		membersContainer.add(membersContainerW.asWidget());
	}
}
