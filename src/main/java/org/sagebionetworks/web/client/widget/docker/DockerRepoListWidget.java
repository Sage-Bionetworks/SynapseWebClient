package org.sagebionetworks.web.client.widget.docker;

import static org.sagebionetworks.repo.model.EntityBundle.ANNOTATIONS;
import static org.sagebionetworks.repo.model.EntityBundle.BENEFACTOR_ACL;
import static org.sagebionetworks.repo.model.EntityBundle.DOI;
import static org.sagebionetworks.repo.model.EntityBundle.ENTITY;
import static org.sagebionetworks.repo.model.EntityBundle.ENTITY_PATH;
import static org.sagebionetworks.repo.model.EntityBundle.PERMISSIONS;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityChildrenRequest;
import org.sagebionetworks.repo.model.EntityChildrenResponse;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.docker.DockerRepository;
import org.sagebionetworks.repo.model.entity.Direction;
import org.sagebionetworks.repo.model.entity.SortBy;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.SynapseJavascriptFactory.OBJECT_TYPE;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.docker.modal.AddExternalRepoModal;
import org.sagebionetworks.web.client.widget.entity.controller.PreflightController;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DockerRepoListWidget implements DockerRepoListWidgetView.Presenter {

	private DockerRepoListWidgetView view;
	private AddExternalRepoModal addExternalRepoModal;
	private PreflightController preflightController;
	private SynapseAlert synAlert;
	private EntityBundle projectBundle;
	private EntityChildrenRequest query;
	private LoadMoreWidgetContainer membersContainer;
	private SynapseJavascriptClient jsClient;

	@Inject
	public DockerRepoListWidget(
			DockerRepoListWidgetView view,
			AddExternalRepoModal addExternalRepoModal,
			PreflightController preflightController,
			LoadMoreWidgetContainer membersContainer,
			SynapseAlert synAlert,
			SynapseJavascriptClient jsClient
			) {
		this.view = view;
		this.addExternalRepoModal = addExternalRepoModal;
		this.preflightController = preflightController;
		this.synAlert = synAlert;
		this.membersContainer = membersContainer;
		this.jsClient = jsClient;
		view.setPresenter(this);
		view.addExternalRepoModal(addExternalRepoModal.asWidget());
		view.setSynAlert(synAlert.asWidget());
		view.setMembersContainer(membersContainer);
		membersContainer.configure(new Callback() {
			@Override
			public void invoke() {
				loadMore();
			}
		});
	}

	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void onClickAddExternalRepo() {
		preflightController.checkCreateEntity(projectBundle, DockerRepository.class.getName(), new Callback() {
			@Override
			public void invoke() {
				addExternalRepoModal.show();
			}
		});
	}

	/**
	 * Configure this widget before use.
	 * @param projectBundle
	 */
	public void configure(EntityBundle projectBundle) {
		this.projectBundle = projectBundle;
		String projectId = projectBundle.getEntity().getId();
		this.query = createDockerRepoEntityQuery(projectId);
		view.setAddExternalRepoButtonVisible(projectBundle.getPermissions().getCanAddChild());
		addExternalRepoModal.configuration(projectId, new Callback(){		
			
			@Override		
			public void invoke() {		
				performInitialLoad();
			}		
		});
		performInitialLoad();
	}
	
	private void performInitialLoad() {
		view.clear();
		query.setNextPageToken(null);
		loadMore();
	}

	public void loadMore() {
		synAlert.clear();
		jsClient.getEntityChildren(query, new AsyncCallback<EntityChildrenResponse>() {
			public void onSuccess(EntityChildrenResponse results) {
				query.setNextPageToken(results.getNextPageToken());
				membersContainer.setIsMore(results.getNextPageToken() != null);
				setResults(results.getPage());
			};
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
			
		});
	}
	
	private void setResults(List<EntityHeader> results) {
		synAlert.clear();
		for (EntityHeader header: results) {
			jsClient.getEntityByID(header.getId(), OBJECT_TYPE.DockerRepository, new AsyncCallback<Entity>(){
				@Override
				public void onSuccess(Entity dockerRepository) {
					view.addRepo((DockerRepository)dockerRepository);
				}
				@Override
				public void onFailure(Throwable error) {
					synAlert.handleException(error);
				}
			});
		}
	}

	/**
	 * Create a new query.
	 * @param parentId
	 * @return
	 */
	public EntityChildrenRequest createDockerRepoEntityQuery(String projectId) {
		EntityChildrenRequest newQuery = new EntityChildrenRequest();
		newQuery.setParentId(projectId);
		newQuery.setSortBy(SortBy.CREATED_ON);
		newQuery.setSortDirection(Direction.DESC);
		List<EntityType> types = new ArrayList<EntityType>();
		types.add(EntityType.dockerrepo);
		newQuery.setIncludeTypes(types);
		return newQuery;
	}
}
