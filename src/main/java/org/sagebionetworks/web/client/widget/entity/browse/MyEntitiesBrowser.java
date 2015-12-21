package org.sagebionetworks.web.client.widget.entity.browse;

import static org.sagebionetworks.repo.model.EntityBundle.ENTITY_PATH;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.entity.query.Condition;
import org.sagebionetworks.repo.model.entity.query.EntityFieldName;
import org.sagebionetworks.repo.model.entity.query.EntityQuery;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResult;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResults;
import org.sagebionetworks.repo.model.entity.query.EntityQueryUtils;
import org.sagebionetworks.repo.model.entity.query.Operator;
import org.sagebionetworks.repo.model.entity.query.Sort;
import org.sagebionetworks.repo.model.entity.query.SortDirection;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class MyEntitiesBrowser implements MyEntitiesBrowserView.Presenter, SynapseWidgetPresenter {
	
	public static final Long ZERO_OFFSET = 0L;
	public static final Long PROJECT_LIMIT = 1000L;
	private MyEntitiesBrowserView view;	
	private AuthenticationController authenticationController;
	private GlobalApplicationState globalApplicationState;
	private SynapseClientAsync synapseClient;
	private SelectedHandler selectedHandler;
	AdapterFactory adapterFactory;
	private Place cachedPlace;
	private String cachedUserId;
	
	public interface SelectedHandler {
		void onSelection(String selectedEntityId);
	}
	
	@Inject
	public MyEntitiesBrowser(MyEntitiesBrowserView view,
			AuthenticationController authenticationController,
			final GlobalApplicationState globalApplicationState,
			SynapseClientAsync synapseClient,
			JSONObjectAdapter jsonObjectAdapter, 
			AdapterFactory adapterFactory) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.synapseClient = synapseClient;
		this.adapterFactory = adapterFactory;
		
		// default selection behavior is to do nothing
		this.selectedHandler = new SelectedHandler() {			
			@Override
			public void onSelection(String selectedEntityId) {								
			}
		};
		
		view.setPresenter(this);
	}	

	public void clearState() {
		if (isSameContext()) {
			view.clearSelection();
		} else {
			view.clear();
		}
	}

	@Override
	public Widget asWidget() {
		view.setPresenter(this);
		refresh();
		return view.asWidget();
	}
	
	public void refresh() {
		//do not reload if the session is unchanged, and the context (project) is unchanged.
		if (!isSameContext()) {
			loadCurrentContext();
			loadUserUpdateable();
			loadFavorites();
			updateContext();
		}
	}

	public boolean isSameContext() {
		if (globalApplicationState.getCurrentPlace() == null || authenticationController.getCurrentUserPrincipalId() == null) {
			return false;
		}
		return globalApplicationState.getCurrentPlace().equals(cachedPlace) && authenticationController.getCurrentUserPrincipalId().equals(cachedUserId);
	}
	public void updateContext() {
		cachedPlace = globalApplicationState.getCurrentPlace();
		cachedUserId = authenticationController.getCurrentUserPrincipalId();
	}
	
	public Place getCachedCurrentPlace() {
		return cachedPlace;
	}
	public String getCachedUserId() {
		return cachedUserId;
	}
	
	/**
	 * Define custom handling for when an entity is clicked
	 * @param handler
	 */
	public void setEntitySelectedHandler(SelectedHandler handler) {
		selectedHandler = handler;
	}

	@Override
	public void entitySelected(String selectedEntityId) {
		selectedHandler.onSelection(selectedEntityId);
	}

	public void loadCurrentContext() {
		view.getCurrentContextTreeBrowser().clear();
		//get the entity path, and ask for each entity to add to the tree
		Place currentPlace = globalApplicationState.getCurrentPlace();
		boolean isSynapsePlace = currentPlace instanceof Synapse;
		view.setCurrentContextTabVisible(isSynapsePlace);
		if (isSynapsePlace) {
			String entityId = ((Synapse) currentPlace).getEntityId();
			int mask = ENTITY_PATH;
			synapseClient.getEntityBundle(entityId, mask, new AsyncCallback<EntityBundle>() {
				@Override
				public void onFailure(Throwable caught) {
					view.showErrorMessage(caught.getMessage());
				}
				public void onSuccess(EntityBundle result) {
					EntityPath path = result.getPath();
					List<EntityHeader> pathHeaders = path.getPath();
					//remove the high level root, so that the first item in the list is the Project
					if (pathHeaders.size() > 0) {
						pathHeaders.remove(0);	
					}
					//add to the current context tree, and show all children of this container (or siblings if leaf)
					view.getCurrentContextTreeBrowser().configureWithPath(pathHeaders);
				};
			});
		}
	}
	@Override
	public void loadUserUpdateable() {
		view.getEntityTreeBrowser().clear();
		if (authenticationController.isLoggedIn()) {
			synapseClient.executeEntityQuery(createMyProjectQuery(), new AsyncCallback<EntityQueryResults>() {
				@Override
				public void onSuccess(EntityQueryResults results) {
					List<EntityHeader> headers = new ArrayList<EntityHeader>();
					for (EntityQueryResult result : results.getEntities()) {
						EntityHeader h = new EntityHeader();
						h.setType(EntityType.project.name());
						h.setId(result.getId());
						h.setName(result.getName());
						headers.add(h);
					}
					
					view.setUpdatableEntities(headers);
				}
				@Override
				public void onFailure(Throwable caught) {
					view.showErrorMessage(caught.getMessage());
				}
			});
		}
	}
	
	public EntityQuery createMyProjectQuery() {
		EntityQuery newQuery = new EntityQuery();
		Sort sort = new Sort();
		sort.setColumnName(EntityFieldName.name.name());
		sort.setDirection(SortDirection.ASC);
		newQuery.setSort(sort);
		Condition condition = EntityQueryUtils.buildCondition(
				EntityFieldName.createdByPrincipalId, Operator.EQUALS, authenticationController.getCurrentUserPrincipalId());
		newQuery.setConditions(Arrays.asList(condition));
		newQuery.setFilterByType(org.sagebionetworks.repo.model.EntityType.project);
		newQuery.setLimit(PROJECT_LIMIT);
		newQuery.setOffset(ZERO_OFFSET);
		return newQuery;
	}

	public EntityTreeBrowser getEntityTreeBrowser() {
		return view.getEntityTreeBrowser();
	}
	
	public EntityTreeBrowser getFavoritesTreeBrowser() {
		return view.getFavoritesTreeBrowser();
	}

	@Override
	public void loadFavorites() {
		view.getFavoritesTreeBrowser().clear();
		EntityBrowserUtils.loadFavorites(synapseClient, adapterFactory, globalApplicationState, new AsyncCallback<List<EntityHeader>>() {
			@Override
			public void onSuccess(List<EntityHeader> result) {
				view.setFavoriteEntities(result);
			}
			@Override
			public void onFailure(Throwable caught) {
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view))
					view.showErrorMessage(DisplayConstants.ERROR_GENERIC_RELOAD);
			}
		});
	}

	
	/*
	 * Private Methods
	 */
}
