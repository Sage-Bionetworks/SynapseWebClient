package org.sagebionetworks.web.client;

import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.mvp.AppPlaceHistoryMapper;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;

import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;

public interface GlobalApplicationState {

	
	/**
	 * Gets the place changer for the application
	 * @return
	 */	
	public PlaceChanger getPlaceChanger();
	
	/**
	 * Sets the place controller (should only be used in the onModuleLoad() method of Portal) 
	 * @param placeController
	 */
	public void setPlaceController(PlaceController placeController);
	

	/**
	 * Gets the Jira url helper for the application
	 * @return
	 */	
	public JiraURLHelper getJiraURLHelper();
	
	/**
	 * Holds the last visited place
	 * @return
	 */
	public Place getLastPlace();
	
	
	/**
	 * Go to the last visited place. 
	 */
	public void gotoLastPlace();
	/**
	 * Go to the last visited place.  Return to the given default place if no last place is available. 
	 * @param defaultPlace
	 */
	public void gotoLastPlace(Place defaultPlace);
	
	/**
	 * Holds the last visited place.  Return the given default place if no last place is available.
	 * @return
	 */
	public Place getLastPlace(Place defaultPlace);
	
	/**
	 * Sets the last visited place (should only used in the AppActivityMapper) 
	 * @param lastPlace
	 */
	public void setLastPlace(Place lastPlace);
	
	/**
	 * Holds the current place
	 * @return
	 */
	public Place getCurrentPlace();
	
	/**
	 * Sets the last visited place (should only used in the AppActivityMapper) 
	 * @param lastPlace
	 */
	public void setCurrentPlace(Place currentPlace);
	
	/**
	 * This can be used to change the URL in the browser without adding new history or reloading the page.
	 * Instead of adding history it will rewrite the current history.
	 * 
	 * @param currentPlace
	 */
	public void replaceCurrentPlace(Place currentPlace);
	
	/**
	 * Sets the App Place History Mapper
	 * @param appPlaceHistoryMapper
	 */
	public void setAppPlaceHistoryMapper(AppPlaceHistoryMapper appPlaceHistoryMapper);
	
	/**
	 * Gets the App Place History Mapper
	 * @return AppPlaceHistoryMapper
	 */
	public AppPlaceHistoryMapper getAppPlaceHistoryMapper();
	
	public List<EntityHeader> getFavorites();
	
	public void setFavorites(List<EntityHeader> favorites);
	
	public void checkVersionCompatibility(SynapseView view);
	public boolean isEditing();
	public void setIsEditing(boolean isEditing);
	
	/**
	 * As app loads, initialize Synapse properties in the GlobalApplicationState so that they can be used client-side.
	 */
	void initSynapseProperties(Callback c);
	
	String getSynapseProperty(String key);
	
	void clearLastPlace();
}
