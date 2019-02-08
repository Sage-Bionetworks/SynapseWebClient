package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Search;
import org.sagebionetworks.web.client.place.Synapse;

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
	public static void searchForTerm(String queryTerm, final GlobalApplicationState globalApplicationState) {
		final Synapse synapsePlace = willRedirect(queryTerm);
		final Search searchPlace = new Search(queryTerm);
		if (synapsePlace == null) {
			//no potential redirect, go directly to search!
			globalApplicationState.getPlaceChanger().goTo(searchPlace);	
		} else {
			globalApplicationState.getPlaceChanger().goTo(synapsePlace);
			
		}
	}

}
