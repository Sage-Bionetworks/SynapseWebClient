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
	 * Set the activity mapper.
	 * @param mapper
	 */
	public void setActivityMapper(ActivityMapper mapper);

	/**
	 * Holds the last visited place
	 * @return
	 */
	public Place getLastPlace();
	
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
}
