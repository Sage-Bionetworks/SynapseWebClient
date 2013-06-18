package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SearchServiceAsync;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.QueryConstants.WhereOperator;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WhereCondition;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class EntityBrowserUtils {

	public static void loadUserUpdateable(SearchServiceAsync searchService,
			final NodeModelCreator nodeModelCreator,
			final GlobalApplicationState globalApplicationState,
			final AuthenticationController authenticationController,			
			final AsyncCallback<List<EntityHeader>> callback) {
		//first, load the projects that the user created
		if(authenticationController.isLoggedIn()) {
			
			List<WhereCondition> where = new ArrayList<WhereCondition>();
			where.add(new WhereCondition(WebConstants.ENTITY_CREATEDBYPRINCIPALID_KEY, WhereOperator.EQUALS, authenticationController.getCurrentUserPrincipalId()));
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
			final NodeModelCreator nodeModelCreator,
			final GlobalApplicationState globalApplicationState,
			final AsyncCallback<List<EntityHeader>> callback) {
		synapseClient.getFavorites(Integer.MAX_VALUE, 0, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				try {
					PaginatedResults<EntityHeader> favorites = nodeModelCreator.createPaginatedResults(result, EntityHeader.class);
					globalApplicationState.setFavorites(favorites.getResults());
					callback.onSuccess(favorites.getResults());
				} catch (JSONObjectAdapterException e) {
					onFailure(e);
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

}
