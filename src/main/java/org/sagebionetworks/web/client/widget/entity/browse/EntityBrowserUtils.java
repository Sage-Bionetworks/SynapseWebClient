package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SearchServiceAsync;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.shared.QueryConstants.WhereOperator;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WhereCondition;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class EntityBrowserUtils {

	public static void loadUserUpdateable(SearchServiceAsync searchService,
			final AdapterFactory adapterFactory,
			final GlobalApplicationState globalApplicationState,
			final AuthenticationController authenticationController,			
			final AsyncCallback<List<EntityHeader>> callback) {
		if(authenticationController.isLoggedIn()) {
			loadUserUpdateable(authenticationController.getCurrentUserPrincipalId(), searchService, adapterFactory, globalApplicationState, callback);
		}
	}
	
	public static void loadUserUpdateable(String userId,
			SearchServiceAsync searchService,
			final AdapterFactory adapterFactory,
			final GlobalApplicationState globalApplicationState,
			final AsyncCallback<List<EntityHeader>> callback) {
		//first, load the projects that the user created
		if(userId!=null) {
			List<WhereCondition> where = new ArrayList<WhereCondition>();
			where.add(new WhereCondition(WebConstants.ENTITY_CREATEDBYPRINCIPALID_KEY, WhereOperator.EQUALS, userId));
			searchService.searchEntities("project", where, 1, 1000, null, false, new AsyncCallback<List<String>>() {
				@Override
				public void onSuccess(List<String> result) {
					List<EntityHeader> headers = new ArrayList<EntityHeader>();
					for(String entityHeaderJson : result) {
						try {
							headers.add(new EntityHeader(adapterFactory.createNew(entityHeaderJson)));
						} catch (JSONObjectAdapterException e) {
							onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
						}
					} 
					//show whatever projects that we found (maybe zero)
					callback.onSuccess(headers);
				}
				@Override
				public void onFailure(Throwable caught) {
					//failed to load projects that the user created
					callback.onFailure(caught);
				}
			});
		}
	}
	
	public static void loadFavorites(SynapseClientAsync synapseClient,
			final AdapterFactory adapterFactory,
			final GlobalApplicationState globalApplicationState,
			final AsyncCallback<List<EntityHeader>> callback) {
		synapseClient.getFavorites(new AsyncCallback<List<EntityHeader>>() {
			@Override
			public void onSuccess(List<EntityHeader> favorites) {
				//show whatever projects that we found (maybe zero)
				globalApplicationState.setFavorites(favorites);
				
				callback.onSuccess(favorites);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	public static void sortEntityHeadersByName(List<EntityHeader> list) {
		Collections.sort(list, new Comparator<EntityHeader>() {
	        @Override
	        public int compare(EntityHeader o1, EntityHeader o2) {
	        	return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
	        }
		});
	}
	
}
