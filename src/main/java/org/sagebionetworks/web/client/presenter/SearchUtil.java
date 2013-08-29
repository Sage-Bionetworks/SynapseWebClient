package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Search;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.shared.EntityWrapper;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * This logic was removed from the search presenter so we could make a clean SearchPresenterProxy.
 * 
 * @author John
 *
 */
public class SearchUtil {
	
	/**
	 * If this returns a Synapse place then we should redirect to an entity page
	 * @param place
	 * @return
	 */
	public static Synapse willRedirect(Search place) {
		String queryTerm = place.getSearchTerm();
		if (queryTerm == null) queryTerm = "";
		return willRedirect(queryTerm);
	}
	/**
	 * If this returns a Synapse place then we should redirect to an entity page
	 * @param queryTerm
	 * @return
	 */
	public static Synapse willRedirect(String queryTerm) {
		if(queryTerm.startsWith(ClientProperties.SYNAPSE_ID_PREFIX)) {
			String remainder = queryTerm.replaceFirst(ClientProperties.SYNAPSE_ID_PREFIX, "");
			if(remainder.matches("^[0-9]+$")) {
				return new Synapse(queryTerm);
			}
		}
		return null;
	}
	public static void searchForTerm(String queryTerm, final GlobalApplicationState globalApplicationState, SynapseClientAsync synapseClient) {
		final Synapse synapsePlace = willRedirect(queryTerm);
		final Search searchPlace = new Search(queryTerm);
		if (synapsePlace == null) {
			//no potential redirect, go directly to search!
			globalApplicationState.getPlaceChanger().goTo(searchPlace);	
		} else {
			//looks like a redirect.  let's validate before going there.
			synapseClient.getEntity(queryTerm, new AsyncCallback<EntityWrapper>() {
				
				@Override
				public void onSuccess(EntityWrapper result) {
					//any success then go to entity page
					globalApplicationState.getPlaceChanger().goTo(synapsePlace);
				}
				
				@Override
				public void onFailure(Throwable caught) {
					//any failure then go to search
					globalApplicationState.getPlaceChanger().goTo(searchPlace);
				}
			});
		}
	}

}
