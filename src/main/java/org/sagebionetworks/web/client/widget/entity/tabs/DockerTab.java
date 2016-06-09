package org.sagebionetworks.web.client.widget.entity.tabs;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.breadcrumb.Breadcrumb;
import org.sagebionetworks.web.client.widget.docker.DockerListWidget;
import org.sagebionetworks.web.client.widget.entity.EntityMetadata;
import org.sagebionetworks.web.client.widget.entity.ModifiedCreatedByWidget;
import org.sagebionetworks.web.client.widget.entity.controller.StuAlert;
import org.sagebionetworks.web.client.widget.entity.file.BasicTitleBar;

import com.google.inject.Inject;

public class DockerTab implements DockerTabView.Presenter{
	private static final String DOCKER_TAB_TITLE = "Docker";

	Tab tab;
	DockerTabView view;
	BasicTitleBar dockerTitleBar;
	DockerListWidget dockerListWidget;
	Breadcrumb breadcrumb;
	EntityMetadata metadata;
	ModifiedCreatedByWidget modifiedCreatedBy;
	StuAlert synAlert;

	Entity entity;
	EntityBundle projectBundle;
	Throwable projectBundleLoadError;
	String projectEntityId;
	String areaToken;
	EntityUpdatedHandler handler;
	CallbackP<Boolean> showProjectInfoCallack;

	@Inject
	public DockerTab(
			DockerTabView view,
			Tab tab,
			BasicTitleBar dockerTitleBar,
			DockerListWidget dockerListWidget,
			Breadcrumb breadcrumb,
			EntityMetadata metadata,
			ModifiedCreatedByWidget modifiedCreatedBy,
			StuAlert synAlert
			) {
		this.view = view;
		this.tab = tab;
		this.dockerTitleBar = dockerTitleBar;
		this.dockerListWidget = dockerListWidget;
		this.breadcrumb = breadcrumb;
		this.metadata = metadata;
		this.modifiedCreatedBy = modifiedCreatedBy;
		this.synAlert = synAlert;
		tab.configure(DOCKER_TAB_TITLE, view.asWidget());
		view.setPresenter(this);
		view.setBreadcrumb(breadcrumb.asWidget());
		view.setDockerList(dockerListWidget.asWidget());
		view.setTitlebar(dockerTitleBar.asWidget());
		view.setEntityMetadata(metadata.asWidget());
		view.setSynapseAlert(synAlert.asWidget());
		view.setModifiedCreatedBy(modifiedCreatedBy);
	}

	public void setTabClickedCallback(CallbackP<Tab> onClickCallback) {
		tab.addTabClickedCallback(onClickCallback);
	}

	public void setShowProjectInfoCallback(CallbackP<Boolean> callback) {
		showProjectInfoCallack = callback;
		tab.addTabClickedCallback(new CallbackP<Tab>() {
			@Override
			public void invoke(Tab param) {
				boolean isProject = entity instanceof Project;
				showProjectInfoCallack.invoke(isProject);
			}
		});
	}

	public void configure(Entity entity, EntityUpdatedHandler handler, String areaToken) {
		this.entity = entity;
		this.areaToken = areaToken;
		this.handler = handler;
		metadata.setEntityUpdatedHandler(handler);
		synAlert.clear();
		//if (entity instanceof DockerRepository) {
			//configure based on project
			showProjectLevelUI();
		//} else {
		//	getTargetBundleAndDisplay(entity.getId());
		//}
	}

	private void getTargetBundleAndDisplay(String id) {
		// TODO Auto-generated method stub
		
	}

	private void showProjectLevelUI() {
		// TODO Auto-generated method stub
		
	}

	public Tab asTab(){
		return tab;
	}

	public void setProject(String projectEntityId, EntityBundle projectBundle, Throwable projectBundleLoadError) {
		this.projectEntityId = projectEntityId;
		this.projectBundle = projectBundle;
		this.projectBundleLoadError = projectBundleLoadError;
	}
}
