package org.sagebionetworks.web.client.presenter;

import com.google.gwt.place.shared.Place;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.place.PeopleSearch;
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
   *
   * @param place
   * @return
   */
  public static Place willRedirect(Search place) {
    String queryTerm = place.getSearchTerm();
    if (queryTerm == null) queryTerm = "";
    return willRedirect(queryTerm);
  }

  /**
   * If this returns a Synapse place then we should redirect to an entity page
   *
   * @param queryTerm
   * @return
   */
  public static Place willRedirect(String queryTerm) {
    if (queryTerm.startsWith(ClientProperties.SYNAPSE_ID_PREFIX)) {
      String remainder = queryTerm.replaceFirst(
        ClientProperties.SYNAPSE_ID_PREFIX,
        ""
      );
      if (remainder.matches("^[0-9]+$")) {
        return new Synapse(queryTerm);
      }
    } else if (queryTerm.charAt(0) == '@') {
      return new PeopleSearch(queryTerm.substring(1));
    }

    return null;
  }

  public static void searchForTerm(
    String queryTerm,
    final GlobalApplicationState globalApplicationState
  ) {
    final Place place = willRedirect(queryTerm);
    final Search searchPlace = new Search(queryTerm);
    if (place == null) {
      // no potential redirect, go directly to search!
      globalApplicationState.getPlaceChanger().goTo(searchPlace);
    } else {
      globalApplicationState.getPlaceChanger().goTo(place);
    }
  }
}
