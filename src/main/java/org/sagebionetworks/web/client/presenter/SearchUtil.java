package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.place.Search;
import org.sagebionetworks.web.client.place.Synapse;

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
		if(queryTerm.startsWith(DisplayUtils.SYNAPSE_ID_PREFIX)) {
			String remainder = queryTerm.replaceFirst(DisplayUtils.SYNAPSE_ID_PREFIX, "");
			if(remainder.matches("^[0-9]+$")) {
				return new Synapse(queryTerm);
			}
		}
		return null;
	}
	
	public static String getSearchHistoryToken(String searchQuery) {
		Search place = new Search(searchQuery);
		return "#!" + getSearchPlaceString(Search.class) + ":" + place.toToken();
	}
	
	public static String getSearchHistoryToken(String searchQuery, Long start) {
		Search place = new Search(searchQuery, start);
		return "#!" + getSearchPlaceString(Search.class) + ":" + place.toToken();
	}
	
	
	private static String getSearchPlaceString(Class<Search> place) {
		String fullPlaceName = place.getName();		
		fullPlaceName = fullPlaceName.replaceAll(".+\\.", "");
		return fullPlaceName;
	}


}
