package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SearchServiceAsync;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.QueryConstants.WhereOperator;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WhereCondition;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class MyEntitiesBrowser implements MyEntitiesBrowserView.Presenter, SynapseWidgetPresenter {
	
	private MyEntitiesBrowserView view;	
	private SearchServiceAsync searchService;
	private NodeModelCreator nodeModelCreator;
	private AuthenticationController authenticationController;
	private GlobalApplicationState globalApplicationState;
	private HandlerManager handlerManager = new HandlerManager(this);
	private EntityTypeProvider entityTypeProvider;
	private SynapseClientAsync synapseClient;
	private JSONObjectAdapter jsonObjectAdapter;
	private SelectedHandler selectedHandler;
	
	public interface SelectedHandler {
		void onSelection(String selectedEntityId);
	}
	
	@Inject
	public MyEntitiesBrowser(MyEntitiesBrowserView view,
			NodeModelCreator nodeModelCreator,
			AuthenticationController authenticationController,
			EntityTypeProvider entityTypeProvider,
			final GlobalApplicationState globalApplicationState,
			SynapseClientAsync synapseClient,
			JSONObjectAdapter jsonObjectAdapter, 
			SearchServiceAsync searchService) {
		this.view = view;
		this.searchService = searchService;
		this.nodeModelCreator = nodeModelCreator;
		this.authenticationController = authenticationController;
		this.entityTypeProvider = entityTypeProvider;
		this.globalApplicationState = globalApplicationState;
		this.synapseClient = synapseClient;
		this.jsonObjectAdapter = jsonObjectAdapter;
		
		// default selection behavior is to do nothing
		this.selectedHandler = new SelectedHandler() {			
			@Override
			public void onSelection(String selectedEntityId) {								
			}
		};
		
		view.setPresenter(this);
	}	

	@SuppressWarnings("unchecked")
	public void clearState() {
		view.clear();
		// remove handlers
		handlerManager = new HandlerManager(this);		
	}

	/**
	 * Does nothing. Use asWidget(Entity)
	 */
	@Override
	public Widget asWidget() {
		view.setPresenter(this);
		loadUserUpdateable();
		loadFavorites();
		return view.asWidget();
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

	@Override
	public void loadUserUpdateable() {
		//first, load the projects that the user created
		if(authenticationController.isLoggedIn()) {
			view.showLoading();
			List<WhereCondition> where = new ArrayList<WhereCondition>();
			UserSessionData userSessionData = authenticationController.getLoggedInUser();
			where.add(new WhereCondition(WebConstants.ENTITY_CREATEDBYPRINCIPALID_KEY, WhereOperator.EQUALS, userSessionData.getProfile().getOwnerId()));
			searchService.searchEntities("project", where, 1, 1000, null, false, new AsyncCallback<List<String>>() {
				@Override
				public void onSuccess(List<String> result) {
					List<EntityHeader> headers = new ArrayList<EntityHeader>();
					for(String entityHeaderJson : result) {
						try {
							headers.add(nodeModelCreator.createJSONEntity(entityHeaderJson, EntityHeader.class));
						} catch (JSONObjectAdapterException e) {
							onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
						}
					} 
					//show whatever projects that we found (maybe zero)
					view.setUpdatableEntities(headers);

				}
				@Override
				public void onFailure(Throwable caught) {
					//failed to load projects that the user created
					view.setUpdatableEntities(new ArrayList<EntityHeader>());
				}
			});
		}
	}

	public EntityTreeBrowser getEntityTreeBrowser() {
		return view.getEntityTreeBrowser();
	}

	@Override
	public void loadFavorites() {
		synapseClient.getFavorites(Integer.MAX_VALUE, 0, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				try {
					PaginatedResults<EntityHeader> favorites = nodeModelCreator.createPaginatedResults(result, EntityHeader.class);
					globalApplicationState.setFavorites(favorites.getResults());
					view.setFavoriteEntities(favorites.getResults());
				} catch (JSONObjectAdapterException e) {
					onFailure(e);
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(DisplayConstants.ERROR_GENERIC_RELOAD);
			}
		});
	}

	
	/*
	 * Private Methods
	 */
}
