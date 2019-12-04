package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.ArrayList;
import java.util.List;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.ProjectHeader;
import org.sagebionetworks.repo.model.ProjectHeaderList;
import org.sagebionetworks.repo.model.ProjectListSortColumn;
import org.sagebionetworks.repo.model.ProjectListType;
import org.sagebionetworks.repo.model.entity.query.SortDirection;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundleRequest;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class MyEntitiesBrowser implements MyEntitiesBrowserView.Presenter, SynapseWidgetPresenter {

	public static final int PROJECT_LIMIT = 20;
	private MyEntitiesBrowserView view;
	private AuthenticationController authenticationController;
	private GlobalApplicationState globalApplicationState;
	private SynapseJavascriptClient jsClient;
	private SelectedHandler selectedHandler;
	private Place cachedPlace;
	private String cachedUserId;
	String nextPageToken = null;

	public interface SelectedHandler {
		void onSelection(String selectedEntityId);
	}

	@Inject
	public MyEntitiesBrowser(MyEntitiesBrowserView view, AuthenticationController authenticationController, final GlobalApplicationState globalApplicationState, JSONObjectAdapter jsonObjectAdapter, SynapseJavascriptClient jsClient) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.jsClient = jsClient;
		// default selection behavior is to do nothing
		this.selectedHandler = new SelectedHandler() {
			@Override
			public void onSelection(String selectedEntityId) {}
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
		return view.asWidget();
	}

	public void refresh() {
		// do not reload if the session is unchanged, and the context (project) is unchanged.
		if (!isSameContext()) {
			// reset user updatable entities
			view.getEntityTreeBrowser().clear();
			view.setIsMoreUpdatableEntities(true);
			nextPageToken = null;
			loadCurrentContext();
			loadMoreUserUpdateable();
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

	public void clearCurrentContent() {
		cachedPlace = null;
		cachedUserId = null;
	}

	public Place getCachedCurrentPlace() {
		return cachedPlace;
	}

	public String getCachedUserId() {
		return cachedUserId;
	}

	/**
	 * Define custom handling for when an entity is clicked
	 * 
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
		// get the entity path, and ask for each entity to add to the tree
		Place currentPlace = globalApplicationState.getCurrentPlace();
		boolean isSynapsePlace = currentPlace instanceof Synapse;
		view.setCurrentContextTabVisible(isSynapsePlace);
		if (isSynapsePlace) {
			String entityId = ((Synapse) currentPlace).getEntityId();
			EntityBundleRequest bundleRequest = new EntityBundleRequest();
			bundleRequest.setIncludeEntityPath(true);
			jsClient.getEntityBundle(entityId, bundleRequest, new AsyncCallback<EntityBundle>() {
				@Override
				public void onFailure(Throwable caught) {
					view.showErrorMessage(caught.getMessage());
				}

				public void onSuccess(EntityBundle result) {
					EntityPath path = result.getPath();
					List<EntityHeader> pathHeaders = path.getPath();
					// remove the high level root, so that the first item in the list is the Project
					List<EntityHeader> projectHeader = new ArrayList<EntityHeader>();
					if (pathHeaders.size() > 1) {
						projectHeader.add(pathHeaders.get(1));
					}
					// add to the current context tree, and show all children of this container (or siblings if leaf)
					view.getCurrentContextTreeBrowser().configure(projectHeader);
				};
			});
		}
	}

	@Override
	public void loadMoreUserUpdateable() {
		if (authenticationController.isLoggedIn()) {
			jsClient.getMyProjects(ProjectListType.CREATED, PROJECT_LIMIT, nextPageToken, ProjectListSortColumn.PROJECT_NAME, SortDirection.ASC, new AsyncCallback<ProjectHeaderList>() {
				@Override
				public void onSuccess(ProjectHeaderList projectHeaders) {
					List<EntityHeader> headers = new ArrayList<EntityHeader>();
					for (ProjectHeader result : projectHeaders.getResults()) {
						EntityHeader h = new EntityHeader();
						h.setType(Project.class.getName());
						h.setId(result.getId());
						h.setName(result.getName());
						headers.add(h);
					} ;
					view.addUpdatableEntities(headers);
					nextPageToken = projectHeaders.getNextPageToken();
					view.setIsMoreUpdatableEntities(nextPageToken != null);
				}

				@Override
				public void onFailure(Throwable caught) {
					view.showErrorMessage(caught.getMessage());
				}
			});
		}
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
		EntityBrowserUtils.loadFavorites(jsClient, globalApplicationState, new AsyncCallback<List<EntityHeader>>() {
			@Override
			public void onSuccess(List<EntityHeader> result) {
				view.setFavoriteEntities(result);
			}

			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		});
	}

	public void setEntityFilter(EntityFilter filter) {
		getEntityTreeBrowser().setEntityFilter(filter);
		getFavoritesTreeBrowser().setEntityFilter(filter);
		view.getCurrentContextTreeBrowser().setEntityFilter(filter);
		clearCurrentContent();
		refresh();
	}

	public EntityFilter getEntityFilter() {
		return getEntityTreeBrowser().getEntityFilter();
	}

	public String getNextPageToken() {
		return nextPageToken;
	}
}
