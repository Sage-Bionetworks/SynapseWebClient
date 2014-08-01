package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SearchServiceAsync;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class MyEntitiesBrowser implements MyEntitiesBrowserView.Presenter, SynapseWidgetPresenter {
	
	private MyEntitiesBrowserView view;	
	private SearchServiceAsync searchService;
	private AuthenticationController authenticationController;
	private GlobalApplicationState globalApplicationState;
	private HandlerManager handlerManager = new HandlerManager(this);
	private SynapseClientAsync synapseClient;
	private SelectedHandler selectedHandler;
	AdapterFactory adapterFactory;
	
	public interface SelectedHandler {
		void onSelection(String selectedEntityId);
	}
	
	@Inject
	public MyEntitiesBrowser(MyEntitiesBrowserView view,
			AuthenticationController authenticationController,
			final GlobalApplicationState globalApplicationState,
			SynapseClientAsync synapseClient,
			JSONObjectAdapter jsonObjectAdapter, 
			SearchServiceAsync searchService,
			AdapterFactory adapterFactory) {
		this.view = view;
		this.searchService = searchService;
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
		view.showLoading();
		view.getEntityTreeBrowser().clear();
		EntityBrowserUtils.loadUserUpdateable(searchService, adapterFactory, globalApplicationState, authenticationController, new AsyncCallback<List<EntityHeader>>() {
			@Override
			public void onSuccess(List<EntityHeader> result) {
				view.setUpdatableEntities(result);
			}
			@Override
			public void onFailure(Throwable caught) {
				view.setUpdatableEntities(new ArrayList<EntityHeader>());
			}
		});
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
