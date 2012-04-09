package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.search.Hit;
import org.sagebionetworks.repo.model.search.SearchResults;
import org.sagebionetworks.repo.model.search.query.KeyValue;
import org.sagebionetworks.repo.model.search.query.SearchQuery;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SearchServiceAsync;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.services.NodeServiceAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.QueryConstants.WhereOperator;
import org.sagebionetworks.web.shared.WhereCondition;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.users.UserData;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class MyEntitiesBrowser implements MyEntitiesBrowserView.Presenter, SynapseWidgetPresenter {
	
	private MyEntitiesBrowserView view;
	private NodeServiceAsync nodeService;
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
			NodeServiceAsync nodeService, NodeModelCreator nodeModelCreator,
			AuthenticationController authenticationController,
			EntityTypeProvider entityTypeProvider,
			final GlobalApplicationState globalApplicationState,
			SynapseClientAsync synapseClient,
			JSONObjectAdapter jsonObjectAdapter, 
			SearchServiceAsync searchService) {
		this.view = view;
		this.nodeService = nodeService;
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
		return view.asWidget();
	}

	@Override
	public void loadUserUpdateable() {
		if(!authenticationController.isLoggedIn()) return;		
		UserData user = authenticationController.getLoggedInUser();		
		SearchQuery query = createUpdateQuery(user.getEmail());
		
		view.showLoading();
		JSONObjectAdapter adapter = jsonObjectAdapter.createNew();
		try {
			query.writeToJSONObject(adapter);
			synapseClient.search(adapter.toJSONString(), new AsyncCallback<EntityWrapper>() {			
				@Override
				public void onSuccess(EntityWrapper result) {
					SearchResults results = new SearchResults();		
					try {
						results = nodeModelCreator.createEntity(result, SearchResults.class);
						// convert results to EntityHeaders
						List<EntityHeader> eheaders = new ArrayList<EntityHeader>();
						for(Hit hit : results.getHits()) {
							EntityHeader eh = new EntityHeader();
							eh.setId(hit.getId());
							eh.setName(hit.getName());
							eh.setType("project");
							eheaders.add(eh);
						}
						
						
						if(eheaders.size() == 0) {
							EntityHeader eh = new EntityHeader();
							eh.setId("9");
							eh.setName("Nine");
							eh.setType("project");
							eheaders.add(eh);
							
						}
						
						view.setUpdatableEntities(eheaders);
					} catch (RestServiceException e) {
						onFailure(e);
					}																	
				}
				
				@Override
				public void onFailure(Throwable caught) {
					if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.getLoggedInUser())) {					
						view.showErrorMessage(DisplayConstants.ERROR_GENERIC_RELOAD);
					} 						
				}
			});
		} catch (JSONObjectAdapterException e) {
			view.showErrorMessage(DisplayConstants.ERROR_GENERIC);
		}

	}

	private SearchQuery createUpdateQuery(String username) {
		SearchQuery query = new SearchQuery();
		
		// BQ
		List<KeyValue> bq = new ArrayList<KeyValue>();
		KeyValue kv; 
		kv = new KeyValue();
		//kv.setKey("update_acl");
		kv.setKey("acl");
		kv.setValue(username);
		bq.add(kv);
		
		kv = new KeyValue();
		kv.setKey("node_type");
		kv.setValue("project");
		bq.add(kv);
		query.setBooleanQuery(bq);
		
		// fields
		query.setReturnFields(Arrays.asList(new String[] { "id", "name" }));
		
		return query;
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
	public void createdOnlyFilter() {
		if(authenticationController.isLoggedIn()) {
			view.showLoading();
			List<WhereCondition> where = new ArrayList<WhereCondition>();
			where.add(new WhereCondition(DisplayUtils.ENTITY_CREATEDBY_KEY, WhereOperator.EQUALS, authenticationController.getLoggedInUser().getEmail()));
			searchService.searchEntities("project", where, 1, 1000, null, false, new AsyncCallback<List<String>>() {
				@Override
				public void onSuccess(List<String> result) {
					List<EntityHeader> headers = new ArrayList<EntityHeader>();
					for(String entityHeaderJson : result) {
						try {
							headers.add(nodeModelCreator.createEntity(entityHeaderJson, EntityHeader.class));
						} catch (RestServiceException e) {
							onFailure(e);
						}
					}
					// send to view
					view.setCreatedEntities(headers);
				}
				@Override
				public void onFailure(Throwable caught) {
					DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.getLoggedInUser());				
				}
			});
		}
	}

	public EntityTreeBrowser getEntityTreeBrowser() {
		return view.getEntityTreeBrowser();
	}

	
	/*
	 * Private Methods
	 */
}
