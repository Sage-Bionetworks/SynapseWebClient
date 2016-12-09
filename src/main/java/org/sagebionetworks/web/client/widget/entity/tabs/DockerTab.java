package org.sagebionetworks.web.client.widget.entity.tabs;

import static org.sagebionetworks.repo.model.EntityBundle.ACCESS_REQUIREMENTS;
import static org.sagebionetworks.repo.model.EntityBundle.ANNOTATIONS;
import static org.sagebionetworks.repo.model.EntityBundle.DOI;
import static org.sagebionetworks.repo.model.EntityBundle.ENTITY;
import static org.sagebionetworks.repo.model.EntityBundle.ENTITY_PATH;
import static org.sagebionetworks.repo.model.EntityBundle.PERMISSIONS;
import static org.sagebionetworks.repo.model.EntityBundle.UNMET_ACCESS_REQUIREMENTS;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.docker.DockerRepository;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.EntityTypeUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.breadcrumb.Breadcrumb;
import org.sagebionetworks.web.client.widget.breadcrumb.LinkData;
import org.sagebionetworks.web.client.widget.docker.DockerRepoListWidget;
import org.sagebionetworks.web.client.widget.docker.DockerRepoWidget;
import org.sagebionetworks.web.client.widget.entity.controller.StuAlert;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class DockerTab implements DockerTabView.Presenter{
	private static final String DOCKER_TAB_TITLE = "Docker";

	Tab tab;
	DockerTabView view;
	DockerRepoListWidget dockerRepoListWidget;
	Breadcrumb breadcrumb;
	PortalGinInjector ginInjector;
	SynapseClientAsync synapseClient;
	StuAlert synAlert;

	Entity entity;
	EntityBundle projectBundle;
	Throwable projectBundleLoadError;
	String projectEntityId;
	String areaToken;
	EntityUpdatedHandler handler;
	CallbackP<Boolean> showProjectInfoCallack;

	@Inject
	public DockerTab(Tab tab, PortalGinInjector ginInjector) {
		this.tab = tab;
		this.ginInjector = ginInjector;
		tab.configure(DOCKER_TAB_TITLE + "&nbsp;" + DisplayConstants.BETA_BADGE_HTML, "A [Docker](https://www.docker.com/what-docker) container is a convenient way to bundle up code and dependencies into a lightweight virtual machine to support reusable and reproducible analysis.", WebConstants.DOCS_URL + "docker.html");

		// Necessary for "beta" badge.  Remove when bringing out of beta.
		tab.addTabListItemStyle("min-width-150");
	}

	public void lazyInject() {
		if (view == null) {
			this.view = ginInjector.getDockerTabView();
			this.dockerRepoListWidget = ginInjector.getDockerRepoListWidget();
			this.breadcrumb = ginInjector.getBreadcrumb();
			this.synapseClient = ginInjector.getSynapseClientAsync();
			this.synAlert = ginInjector.getStuAlert();
			view.setPresenter(this);
			view.setBreadcrumb(breadcrumb.asWidget());
			view.setDockerRepoList(dockerRepoListWidget.asWidget());
			view.setSynapseAlert(synAlert.asWidget());
			tab.setContent(view.asWidget());
			initClickHandler();
		}
	}
	private void initClickHandler() {
		breadcrumb.setLinkClickedHandler(new CallbackP<Place>() {
			public void invoke(Place place) {
				//if this is the project id, then just reconfigure from the project bundle
				Synapse synapse = (Synapse)place;
				String entityId = synapse.getEntityId();
				if (entityId.equals(projectEntityId)) {
					showProjectLevelUI();
					tab.showTab();
				} else {
					getTargetBundleAndDisplay(entityId);
				}
			};
		});

		dockerRepoListWidget.setRepoClickedCallback(new CallbackP<EntityBundle>() {
			@Override
			public void invoke(EntityBundle bundle) {
				areaToken = null;
				tab.setEntityNameAndPlace(bundle.getEntity().getName(), new Synapse(bundle.getEntity().getId(), null, null, null));
				setTargetBundle(bundle);
				tab.showTab();
			}
		});
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
		lazyInject();
		this.entity = entity;
		this.areaToken = areaToken;
		this.handler = handler;
		synAlert.clear();
		if (entity instanceof DockerRepository) {
			getTargetBundleAndDisplay(entity.getId());
		} else {
			showProjectLevelUI();
		}
	}

	private void showProjectLevelUI() {
		String title = projectEntityId;
		if (projectBundle != null) {
			title = projectBundle.getEntity().getName();
			setTargetBundle(projectBundle);	
		} else {
			showError(projectBundleLoadError);
		}
		tab.setEntityNameAndPlace(title, new Synapse(projectEntityId, null, EntityArea.DOCKER, null));
	}

	public void resetView() {
		synAlert.clear();
		view.setBreadcrumbVisible(false);
		view.setDockerRepoListVisible(false);
		view.clearDockerRepoWidget();
		view.setDockerRepoWidgetVisible(false);
		showProjectInfoCallack.invoke(false);
	}
	
	public void showError(Throwable error) {
		resetView();
		synAlert.handleException(error);
	}

	private void setTargetBundle(EntityBundle bundle) {
		this.entity = bundle.getEntity();
		boolean isRepo = entity instanceof DockerRepository;
		boolean isProject = entity instanceof Project;
		view.setBreadcrumbVisible(isRepo);
		view.setDockerRepoListVisible(isProject);
		view.setDockerRepoWidgetVisible(isRepo);
		showProjectInfoCallack.invoke(isProject);
		view.clearDockerRepoWidget();
		if (isRepo) {
			List<LinkData> links = new ArrayList<LinkData>();
			Place projectPlace = new Synapse(projectEntityId);
			String projectName = bundle.getPath().getPath().get(1).getName();
			links.add(new LinkData(projectName, EntityTypeUtils.getIconTypeForEntityClassName(Project.class.getName()), projectPlace));
			breadcrumb.configure(links, ((DockerRepository)entity).getRepositoryName());
			DockerRepoWidget repoWidget = ginInjector.createNewDockerRepoWidget();
			view.setDockerRepoWidget(repoWidget.asWidget());
			repoWidget.configure(bundle, handler);
		} else if (isProject) {
			areaToken = null;
			dockerRepoListWidget.configure(bundle);
		}
	}

	private void getTargetBundleAndDisplay(final String entityId) {
		synAlert.clear();
		view.clearDockerRepoWidget();
		int mask = ENTITY | ANNOTATIONS | PERMISSIONS | ENTITY_PATH | ACCESS_REQUIREMENTS | UNMET_ACCESS_REQUIREMENTS | DOI;
		AsyncCallback<EntityBundle> callback = new AsyncCallback<EntityBundle>() {
			@Override
			public void onSuccess(EntityBundle bundle) {
				tab.setEntityNameAndPlace(bundle.getEntity().getName(), new Synapse(entityId, null, null, null));
				setTargetBundle(bundle);
				tab.showTab();
			}

			@Override
			public void onFailure(Throwable caught) {
				tab.setEntityNameAndPlace(entityId, new Synapse(entityId, null, null, null));
				showError(caught);
				tab.showTab();
			}
		};

		synapseClient.getEntityBundle(entityId, mask, callback);
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
