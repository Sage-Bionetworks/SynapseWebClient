package org.sagebionetworks.web.client.widget.docker;

import java.util.ArrayList;
import java.util.List;
import org.sagebionetworks.repo.model.EntityChildrenRequest;
import org.sagebionetworks.repo.model.EntityChildrenResponse;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.docker.DockerRepository;
import org.sagebionetworks.repo.model.entity.Direction;
import org.sagebionetworks.repo.model.entity.SortBy;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import com.google.gwt.http.client.Request;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DockerRepoListWidget {

	private DockerRepoListWidgetView view;
	private SynapseAlert synAlert;
	private EntityChildrenRequest query;
	private LoadMoreWidgetContainer membersContainer;
	private SynapseJavascriptClient jsClient;
	private Request currentRequest = null;

	@Inject
	public DockerRepoListWidget(DockerRepoListWidgetView view, LoadMoreWidgetContainer membersContainer, SynapseAlert synAlert, SynapseJavascriptClient jsClient) {
		this.view = view;
		this.synAlert = synAlert;
		this.membersContainer = membersContainer;
		this.jsClient = jsClient;
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

	/**
	 * Configure this widget before use.
	 * 
	 * @param projectBundle
	 */
	public void configure(String projectId) {
		if (currentRequest != null) {
			currentRequest.cancel();
		}
		this.query = createDockerRepoEntityQuery(projectId);
		view.clear();
		view.setLoadingVisible(false);
		query.setNextPageToken(null);
		loadMore();
	}

	public void loadMore() {
		synAlert.clear();
		currentRequest = jsClient.getEntityChildren(query, new AsyncCallback<EntityChildrenResponse>() {
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
		for (EntityHeader header : results) {
			view.addRepo(header);
			jsClient.getEntityBundleFromCache(header.getId(), new AsyncCallback<EntityBundle>() {
				@Override
				public void onSuccess(EntityBundle result) {
					view.setDockerRepository((DockerRepository) result.getEntity());
				};

				@Override
				public void onFailure(Throwable caught) {
					synAlert.handleException(caught);
				}
			});
		}
	}

	/**
	 * Create a new query.
	 * 
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

	public void setEntityClickedHandler(CallbackP<String> entityClickedHandler) {
		view.setEntityClickedHandler(entityId -> {
			view.clear();
			view.setLoadingVisible(true);
			entityClickedHandler.invoke(entityId);
		});
	}
}
