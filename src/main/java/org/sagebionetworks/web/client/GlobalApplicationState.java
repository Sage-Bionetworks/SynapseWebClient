package org.sagebionetworks.web.client;

import java.util.List;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.mvp.AppPlaceHistoryMapper;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.footer.VersionState;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface GlobalApplicationState {

	public void init(final Callback c);

	/**
	 * Gets the place changer for the application
	 * 
	 * @return
	 */
	public PlaceChanger getPlaceChanger();

	/**
	 * Sets the place controller (should only be used in the onModuleLoad() method of Portal)
	 * 
	 * @param placeController
	 */
	public void setPlaceController(PlaceController placeController);

	/**
	 * Holds the last visited place
	 * 
	 * @return
	 */
	public Place getLastPlace();


	/**
	 * Go to the last visited place.
	 */
	public void gotoLastPlace();

	/**
	 * Go to the last visited place. Return to the given default place if no last place is available.
	 * 
	 * @param defaultPlace
	 */
	public void gotoLastPlace(Place defaultPlace);

	/**
	 * Holds the last visited place. Return the given default place if no last place is available.
	 * 
	 * @return
	 */
	public Place getLastPlace(Place defaultPlace);

	/**
	 * Sets the last visited place (should only used in the AppActivityMapper)
	 * 
	 * @param lastPlace
	 */
	public void setLastPlace(Place lastPlace);

	/**
	 * Holds the current place
	 * 
	 * @return
	 */
	public Place getCurrentPlace();

	/**
	 * Sets the App Place History Mapper
	 * 
	 * @param appPlaceHistoryMapper
	 */
	public void setAppPlaceHistoryMapper(AppPlaceHistoryMapper appPlaceHistoryMapper);

	/**
	 * Gets the App Place History Mapper
	 * 
	 * @return AppPlaceHistoryMapper
	 */
	public AppPlaceHistoryMapper getAppPlaceHistoryMapper();

	public List<EntityHeader> getFavorites();

	public void setFavorites(List<EntityHeader> favorites);

	public void checkVersionCompatibility(AsyncCallback<VersionState> callback);

	public boolean isEditing();

	public void setIsEditing(boolean isEditing);

	void clearLastPlace();

	/**
	 * This method can be used to change the URL in the browser without reloading the page.
	 * 
	 * @param currentPlace
	 */
	public void pushCurrentPlace(Place targetPlace);

	/**
	 * This method initializes the handler for on pop state event.
	 */
	public void initOnPopStateHandler();

	public void refreshPage();

	public void recordPlaceVisit(Place targetPlace);

	void replaceCurrentPlace(Place targetPlace);

	void setShowUTCTime(boolean showUTC);

	boolean isShowingUTCTime();

	void initializeDropZone();

	void setDropZoneHandler(CallbackP<JavaScriptObject> fileListCallback);

	void clearDropZoneHandler();

	/**
	 * Programmatic equivalent to the user pressing the browser's 'back' button.
	 */
	void back();
	
	boolean isShowingVersionAlert();
}
