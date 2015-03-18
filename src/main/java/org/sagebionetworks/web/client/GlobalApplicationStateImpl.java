package org.sagebionetworks.web.client;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.mvp.AppActivityMapper;
import org.sagebionetworks.web.client.mvp.AppPlaceHistoryMapper;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class GlobalApplicationStateImpl implements GlobalApplicationState {


	private PlaceController placeController;
	private CookieProvider cookieProvider;
	private AppPlaceHistoryMapper appPlaceHistoryMapper;
	private SynapseClientAsync synapseClient;
	private PlaceChanger placeChanger;
	private JiraURLHelper jiraUrlHelper;
	private EventBus eventBus;
	private List<EntityHeader> favorites;
	private String synapseVersion;
	private boolean isEditing;
	private HashMap<String, String> synapseProperties;
	Set<String> wikiBasedEntites;
	private SynapseJSNIUtils synapseJSNIUtils;
	
	@Inject
	public GlobalApplicationStateImpl(CookieProvider cookieProvider, JiraURLHelper jiraUrlHelper, EventBus eventBus, SynapseClientAsync synapseClient) {
	public GlobalApplicationStateImpl(CookieProvider cookieProvider, JiraURLHelper jiraUrlHelper, EventBus eventBus, SynapseClientAsync synapseClient, SynapseJSNIUtils synapseJSNIUtils) {
		this.cookieProvider = cookieProvider;
		this.jiraUrlHelper = jiraUrlHelper;
		this.eventBus = eventBus;
		this.synapseClient = synapseClient;
		this.synapseJSNIUtils = synapseJSNIUtils;
		isEditing = false;
	}
	
	@Override
	public PlaceChanger getPlaceChanger() {
		if(placeChanger == null) {
			placeChanger = new PlaceChanger() {			
				@Override
				public void goTo(Place place) {
					// If we are not already on this page, go there.
					if(!placeController.getWhere().equals(place)){
						placeController.goTo(place);
					}else{
						// We are already on this page but we want to force it to reload.
						eventBus.fireEvent(new PlaceChangeEvent(place));
					}
				}
			};
		}
		return placeChanger;
	}

	@Override
	public void setPlaceController(PlaceController placeController) {
		this.placeController = placeController;
	}
	
	@Override
	public JiraURLHelper getJiraURLHelper() {
		return jiraUrlHelper;
	}

	@Override
	public Place getLastPlace() {
		return getLastPlace(null);
	}
	
	@Override
	public Place getLastPlace(Place defaultPlace) {
		String historyValue = cookieProvider.getCookie(CookieKeys.LAST_PLACE);
		return getPlaceFromHistoryValue(historyValue, fixIfNull(defaultPlace));
	}
	
	@Override
	public void clearLastPlace() {
		cookieProvider.removeCookie(CookieKeys.LAST_PLACE);
	}
	
	@Override
	public void gotoLastPlace() {
		gotoLastPlace(null);
	}

	@Override
	public void gotoLastPlace(Place defaultPlace) {
		getPlaceChanger().goTo(getLastPlace(defaultPlace));
	}
	
	private Place fixIfNull(Place defaultPlace) {
		if (defaultPlace == null) return AppActivityMapper.getDefaultPlace();
		else return defaultPlace;
	}

	@Override
	public void setLastPlace(Place lastPlace) {
		Date expires = new Date(System.currentTimeMillis() + (1000*60*60*2)); // store for 2 hours (we don't want to lose this state while a user registers for Synapse)
		cookieProvider.setCookie(CookieKeys.LAST_PLACE, appPlaceHistoryMapper.getToken(lastPlace), expires);
	}

	@Override
	public Place getCurrentPlace() {
		String historyValue = cookieProvider.getCookie(CookieKeys.CURRENT_PLACE);
		return getPlaceFromHistoryValue(historyValue, AppActivityMapper.getDefaultPlace());		
	}

	@Override
	public void setCurrentPlace(Place currentPlace) {		
		Date expires = new Date(System.currentTimeMillis() + 300000); // store for 5 minutes
		cookieProvider.setCookie(CookieKeys.CURRENT_PLACE, appPlaceHistoryMapper.getToken(currentPlace), expires);
	}

	@Override
	public void setAppPlaceHistoryMapper(AppPlaceHistoryMapper appPlaceHistoryMapper) {
		this.appPlaceHistoryMapper = appPlaceHistoryMapper;
	}

	@Override
	public AppPlaceHistoryMapper getAppPlaceHistoryMapper() {
		return appPlaceHistoryMapper;
	}

	/*
	 * Private Methods
	 */
	private Place getPlaceFromHistoryValue(String historyValue, Place defaultPlace) {
		if(historyValue != null) {
			Place place = appPlaceHistoryMapper.getPlace(historyValue);
			return place;
		} else return defaultPlace;
	}

	@Override
	public List<EntityHeader> getFavorites() {
		return favorites;
	}

	@Override
	public void setFavorites(List<EntityHeader> favorites) {
		this.favorites = favorites;
	}

	@Override
	public void checkVersionCompatibility(final SynapseView view) {
		synapseClient.getSynapseVersions(new AsyncCallback<String>() {			
			@Override
			public void onSuccess(String versions) {
				if(synapseVersion == null) {
					synapseVersion = versions;
				} else {
					if(!synapseVersion.equals(versions)) {
						view.showErrorMessage(DisplayConstants.NEW_VERSION_INSTRUCTIONS);
					}
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
			}
		});
	}

	@Override
	public boolean isEditing() {
		return isEditing;
	}
	
	@Override
	public void setIsEditing(boolean isEditing) {
		this.isEditing = isEditing;
	}
	
	@Override
	public void initSynapseProperties(final Callback c) {
		synapseClient.getSynapseProperties(new AsyncCallback<HashMap<String, String>>() {			
			@Override
			public void onSuccess(HashMap<String, String> properties) {
				synapseProperties = properties;
				initWikiEntities(properties);
				c.invoke();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				c.invoke();
			}
		});
	}
	
	@Override
	public String getSynapseProperty(String key) {
		if (synapseProperties != null)
			return synapseProperties.get(key);
		else 
			return null;
	}

	@Override
	public boolean isWikiBasedEntity(String entityId) {
		if(wikiBasedEntites == null){
			return false;
		}else{
			return wikiBasedEntites.contains(entityId);
	}
	}
	
	/**
	 * Setup the wiki based entities.
	 * @param properties
	 */
	private void initWikiEntities(HashMap<String, String> properties) {
		wikiBasedEntites = new HashSet<String>();
		wikiBasedEntites.add(properties.get(WebConstants.GETTING_STARTED_GUIDE_ENTITY_ID_PROPERTY));
		wikiBasedEntites.add(properties.get(WebConstants.CREATE_PROJECT_ENTITY_ID_PROPERTY));
		wikiBasedEntites.add(properties.get(WebConstants.R_CLIENT_ENTITY_ID_PROPERTY));
		wikiBasedEntites.add(properties.get(WebConstants.PYTHON_CLIENT_ENTITY_ID_PROPERTY));
		wikiBasedEntites.add(properties.get(WebConstants.FORMATTING_GUIDE_ENTITY_ID_PROPERTY));
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sagebionetworks.web.client.GlobalApplicationState#replaceCurrentPlace(com.google.gwt.place.shared.Place)
	 */
	@Override
	public void replaceCurrentPlace(Place currentPlace) {
		setCurrentPlace(currentPlace);
		String token = appPlaceHistoryMapper.getToken(currentPlace);
		this.synapseJSNIUtils.replaceHistoryState(token);
	}
}
