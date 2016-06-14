package org.sagebionetworks.web.client.widget.docker;

import java.util.Arrays;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.docker.DockerRepository;
import org.sagebionetworks.repo.model.entity.query.Condition;
import org.sagebionetworks.repo.model.entity.query.EntityFieldName;
import org.sagebionetworks.repo.model.entity.query.EntityQuery;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResults;
import org.sagebionetworks.repo.model.entity.query.EntityQueryUtils;
import org.sagebionetworks.repo.model.entity.query.Operator;
import org.sagebionetworks.repo.model.entity.query.Sort;
import org.sagebionetworks.repo.model.entity.query.SortDirection;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.docker.modal.AddExternalRepoModal;
import org.sagebionetworks.web.client.widget.entity.controller.PreflightController;
import org.sagebionetworks.web.client.widget.pagination.PageChangeListener;
import org.sagebionetworks.web.client.widget.pagination.PaginationWidget;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DockerRepoListWidget implements DockerRepoListWidgetView.Presenter, DockerRepoAddedHandler, PageChangeListener {

	public static final Long PAGE_SIZE = 10L;
	public static final Long OFFSET_ZERO = 0L;

	private DockerRepoListWidgetView view;
	private SynapseClientAsync synapseClient;
	private PaginationWidget paginationWidget;
	private AddExternalRepoModal addExternalRepoModal;
	private PreflightController preflightController;
	private EntityBundle projectBundle;
	private EntityQuery query;
	private CallbackP<String> onRepoClickCallback;

	@Inject
	public DockerRepoListWidget(
			DockerRepoListWidgetView view,
			SynapseClientAsync synapseClient,
			PaginationWidget paginationWidget,
			AddExternalRepoModal addExternalRepoModal,
			PreflightController preflightController
			) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.paginationWidget = paginationWidget;
		this.addExternalRepoModal = addExternalRepoModal;
		this.preflightController = preflightController;
		view.setPresenter(this);
		view.addPaginationWidget(paginationWidget);
		view.addExternalRepoModal(addExternalRepoModal.asWidget());
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

	@Override
	public void repoAdded() {
		queryForOnePage(OFFSET_ZERO);
	}

	public void setRepoClickedCallback(CallbackP<String> onRepoClickCallback) {
		this.onRepoClickCallback = onRepoClickCallback;
	}

	/**
	 * Configure this widget before use.
	 * @param projectBundle
	 */
	public void configure(EntityBundle projectBundle) {
		this.projectBundle = projectBundle;
		String projectId = projectBundle.getEntity().getId();
		this.query = createDockerRepoEntityQuery(projectId);
		addExternalRepoModal.configuration(projectId, this);
		queryForOnePage(OFFSET_ZERO);
	}

	/**
	 * Run a query and populate the page with the results.
	 * @param offset The offset used by the query.
	 */
	private void queryForOnePage(final Long offset){
		this.query.setOffset(offset);
		synapseClient.executeEntityQuery(this.query, new AsyncCallback<EntityQueryResults>() {
			
			@Override
			public void onSuccess(EntityQueryResults results) {
				paginationWidget.configure(query.getLimit(), query.getOffset(), results.getTotalEntityCount(), DockerRepoListWidget.this);
				boolean showPagination = results.getTotalEntityCount() > query.getLimit();
				view.showPaginationVisible(showPagination);
				setResults(results);
			}

			@Override
			public void onFailure(Throwable error) {
				view.showErrorMessage(error.getMessage());
			}
		});
	}

	private void setResults(EntityQueryResults results) {
		view.clear();
		view.addRepos(results.getEntities());
	}

	/**
	 * Create a new query.
	 * @param parentId
	 * @return
	 */
	public EntityQuery createDockerRepoEntityQuery(String projectId) {
		EntityQuery newQuery = new EntityQuery();
		Sort sort = new Sort();
		sort.setColumnName(EntityFieldName.createdOn.name());
		sort.setDirection(SortDirection.DESC);
		newQuery.setSort(sort);
		Condition projectCondition = EntityQueryUtils.buildCondition(EntityFieldName.parentId, Operator.EQUALS, projectId);
		Condition typeCondition = EntityQueryUtils.buildCondition(
				EntityFieldName.nodeType, Operator.IN, EntityType.dockerrepo.name());
		newQuery.setConditions(Arrays.asList(projectCondition, typeCondition));
		newQuery.setLimit(PAGE_SIZE);
		newQuery.setOffset(OFFSET_ZERO);
		return newQuery;
	}

	@Override
	public void onPageChange(Long newOffset) {
		queryForOnePage(newOffset);
	}

	@Override
	public void onRepoClicked(String id) {
		if (onRepoClickCallback != null) {
			onRepoClickCallback.invoke(id);
		}
	}
}
