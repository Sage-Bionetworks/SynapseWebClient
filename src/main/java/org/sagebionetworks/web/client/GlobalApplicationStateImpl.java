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
import org.sagebionetworks.web.client.widget.footer.VersionState;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.UmbrellaException;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class GlobalApplicationStateImpl implements GlobalApplicationState {
	public static final String DEFAULT_REFRESH_PLACE = "!Home:0";
	public static final String UNCAUGHT_JS_EXCEPTION = "Uncaught JS Exception:";
	private PlaceController placeController;
	private CookieProvider cookieProvider;
	private AppPlaceHistoryMapper appPlaceHistoryMapper;
	private SynapseClientAsync synapseClient;
	private PlaceChanger placeChanger;
	private JiraURLHelper jiraUrlHelper;
	private EventBus eventBus;
	private List<EntityHeader> favorites;
	private boolean isEditing;
	private HashMap<String, String> synapseProperties;
	Set<String> wikiBasedEntites;
	private SynapseJSNIUtils synapseJSNIUtils;
	private ClientLogger logger;
	private GlobalApplicationStateView view;
	private String synapseVersion;

	@Inject
	public GlobalApplicationStateImpl(GlobalApplicationStateView view,
			CookieProvider cookieProvider,
			JiraURLHelper jiraUrlHelper, 
			EventBus eventBus, 
			SynapseClientAsync synapseClient, 
			SynapseJSNIUtils synapseJSNIUtils, 
			ClientLogger logger) {
		this.cookieProvider = cookieProvider;
		this.jiraUrlHelper = jiraUrlHelper;
		this.eventBus = eventBus;
		this.synapseClient = synapseClient;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.logger = logger;
		this.view = view;
		isEditing = false;
		initUncaughtExceptionHandler();
	}
	
	public void initUncaughtExceptionHandler() {
		GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {
			public void onUncaughtException(Throwable e) {
				handleUncaughtException(e);
			}
		});
	}
	
	public void handleUncaughtException(Throwable e) {
		try {
			Throwable unwrapped = unwrap(e);
			logger.errorToRepositoryServices(UNCAUGHT_JS_EXCEPTION, unwrapped);
		} catch (Throwable t) {
			synapseJSNIUtils.consoleError("Unable to log uncaught exception to server: " + t.getMessage());
		} finally {
			synapseJSNIUtils.consoleError(UNCAUGHT_JS_EXCEPTION + e.getMessage() + ": " + e.getStackTrace());	
		}
	}
	
	public Throwable unwrap(Throwable e) {
		if (e instanceof UmbrellaException) {
			UmbrellaException ue = (UmbrellaException) e;
			if (ue.getCauses().size() == 1) {
				return unwrap(ue.getCauses().iterator().next());
			}
		}
		return e;
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
	public void clearCurrentPlace() {
		cookieProvider.removeCookie(CookieKeys.CURRENT_PLACE);
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
	public void checkVersionCompatibility(final AsyncCallback<VersionState> callback) {
		synapseClient.getSynapseVersions(new AsyncCallback<String>() {			
			@Override
			public void onSuccess(String versions) {
				boolean isVersionChange = false;
				//synapse version is set on app load
				if(!synapseVersion.equals(versions)) {
					view.showVersionOutOfDateGlobalMessage();
					isVersionChange = true;
				}
				if (callback != null) {
					callback.onSuccess(new VersionState(synapseVersion, isVersionChange));
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				if (callback != null) {
					callback.onFailure(caught);	
				}
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
				initSynapseVersions(c);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				c.invoke();
			}
		});
	}
	
	public void initSynapseVersions(final Callback c) {
		synapseClient.getSynapseVersions(new AsyncCallback<String>() {			
			@Override
			public void onSuccess(String versions) {
				synapseVersion = versions;
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
	
	@Override
	public void pushCurrentPlace(Place targetPlace) {
		//only push this place into the history if it is a place change
		if (targetPlace != null && !(targetPlace.equals(getCurrentPlace()))) {
			setLastPlace(getCurrentPlace());
			setCurrentPlace(targetPlace);
			String token = appPlaceHistoryMapper.getToken(targetPlace);
			synapseJSNIUtils.pushHistoryState(token);
			
			recordPlaceVisit(targetPlace);
		}
	}
	@Override
	public void recordPlaceVisit(Place targetPlace) {
		String token = appPlaceHistoryMapper.getToken(targetPlace);
		synapseJSNIUtils.recordPageVisit(token);
	}
	
	@Override
	public void initOnPopStateHandler() {
		this.synapseJSNIUtils.initOnPopStateHandler();
	}
	
	public String getSynapseVersion() {
		return synapseVersion;
	}
	
	@Override
	public void refreshPage() {
		//get the place associated to the current url
		AppPlaceHistoryMapper appPlaceHistoryMapper = getAppPlaceHistoryMapper();
		String currentUrl = synapseJSNIUtils.getCurrentURL();
		String place = DEFAULT_REFRESH_PLACE;
		int index = currentUrl.indexOf("!");
		if (index > -1) {
			place = currentUrl.substring(index);
		}
		Place currentPlace = appPlaceHistoryMapper.getPlace(place); 
		getPlaceChanger().goTo(currentPlace);
	}
}
