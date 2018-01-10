package org.sagebionetworks.web.client;

import static org.sagebionetworks.web.client.cookie.CookieKeys.CURRENT_PLACE;
import static org.sagebionetworks.web.client.cookie.CookieKeys.LAST_PLACE;
import static org.sagebionetworks.web.client.cookie.CookieKeys.SHOW_DATETIME_IN_UTC;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.mvp.AppActivityMapper;
import org.sagebionetworks.web.client.mvp.AppPlaceHistoryMapper;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.client.widget.footer.VersionState;
import org.sagebionetworks.web.shared.PublicPrincipalIds;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.UmbrellaException;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.google.inject.Inject;

public class GlobalApplicationStateImpl implements GlobalApplicationState {
	public static final String RECENTLY_CHECKED_SYNAPSE_VERSION = "org.sagebionetworks.web.client.recently-checked-synapse-version";
	public static final String PROPERTIES_LOADED_KEY = "org.sagebionetworks.web.client.properties-loaded";
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
	Set<String> wikiBasedEntites;
	private SynapseJSNIUtils synapseJSNIUtils;
	private GlobalApplicationStateView view;
	private String synapseVersion;
	private ClientCache localStorage;
	private GWTWrapper gwt;
	private boolean isShowingVersionAlert;
	private DateTimeUtils dateTimeUtils;
	private PublicPrincipalIds publicPrincipalIds;
	private SynapseJavascriptClient jsClient;
	
	@Inject
	public GlobalApplicationStateImpl(GlobalApplicationStateView view,
			CookieProvider cookieProvider,
			JiraURLHelper jiraUrlHelper, 
			EventBus eventBus, 
			SynapseClientAsync synapseClient, 
			SynapseJSNIUtils synapseJSNIUtils, 
			ClientCache localStorage, 
			GWTWrapper gwt,
			DateTimeUtils dateTimeUtils,
			SynapseJavascriptClient jsClient) {
		this.cookieProvider = cookieProvider;
		this.jiraUrlHelper = jiraUrlHelper;
		this.eventBus = eventBus;
		this.synapseClient = synapseClient;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.localStorage = localStorage;
		this.dateTimeUtils = dateTimeUtils;
		this.gwt = gwt;
		this.view = view;
		this.jsClient = jsClient;
		isEditing = false;
		isShowingVersionAlert = false;
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
			GWT.debugger();
			jsClient.logError(UNCAUGHT_JS_EXCEPTION, unwrap(e));
		} catch (Throwable t) {
			synapseJSNIUtils.consoleError("Unable to log uncaught exception to server: " + t.getMessage());
		} finally {
			synapseJSNIUtils.consoleError(UNCAUGHT_JS_EXCEPTION + e.getMessage() + ": " + e.getStackTrace());	
		}
	}
	
	public static Throwable unwrap(Throwable e) {
		if (e instanceof UmbrellaException) {
			UmbrellaException ue = (UmbrellaException) e;
			if (ue.getCauses().size() > 0) {
				return unwrap(ue.getCauses().iterator().next());
			}
		} else if (e instanceof com.google.web.bindery.event.shared.UmbrellaException) {
			com.google.web.bindery.event.shared.UmbrellaException ue = (com.google.web.bindery.event.shared.UmbrellaException)e;
			if (ue.getCauses().size() > 0) {
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
						try {
							placeController.goTo(place);
						} catch (Exception e) {
							synapseJSNIUtils.consoleError(e.getMessage());
						}
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
		String historyValue = cookieProvider.getCookie(LAST_PLACE);
		return getPlaceFromHistoryValue(historyValue, fixIfNull(defaultPlace));
	}
	
	@Override
	public void clearLastPlace() {
		cookieProvider.removeCookie(LAST_PLACE);
	}
	@Override
	public void clearCurrentPlace() {
		cookieProvider.removeCookie(CURRENT_PLACE);
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
		cookieProvider.setCookie(LAST_PLACE, appPlaceHistoryMapper.getToken(lastPlace), expires);
	}

	@Override
	public Place getCurrentPlace() {
		String historyValue = cookieProvider.getCookie(CURRENT_PLACE);
		return getPlaceFromHistoryValue(historyValue, AppActivityMapper.getDefaultPlace());		
	}

	@Override
	public void setCurrentPlace(Place currentPlace) {		
		Date expires = new Date(System.currentTimeMillis() + 300000); // store for 5 minutes
		cookieProvider.setCookie(CURRENT_PLACE, appPlaceHistoryMapper.getToken(currentPlace), expires);
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
		//have we checked recently?
		String cachedVersion = localStorage.get(RECENTLY_CHECKED_SYNAPSE_VERSION);
		if (synapseVersion != null && cachedVersion != null) {
			if (callback != null) {
				callback.onSuccess(new VersionState(synapseVersion, false));
			}
			return;
		}
		// don't check for the next minute
		localStorage.put(RECENTLY_CHECKED_SYNAPSE_VERSION, Boolean.TRUE.toString(), new Date(System.currentTimeMillis() + 1000*60).getTime());
		
		synapseClient.getSynapseVersions(new AsyncCallback<String>() {			
			@Override
			public void onSuccess(String versions) {
				if (synapseVersion == null) {
					synapseVersion = versions;
				}
				boolean isVersionChange = false;
				//synapse version is set on app load
				if(!synapseVersion.equals(versions)) {
					if (!isShowingVersionAlert) {
						view.showVersionOutOfDateGlobalMessage();
						isShowingVersionAlert = true;
					}
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
		String isLoaded = localStorage.get(PROPERTIES_LOADED_KEY);
		if (isLoaded != null) {
			// we have properties locally, defer updating properties from server
			gwt.scheduleDeferred(new Callback() {
				@Override
				public void invoke() {
					initSynapsePropertiesFromServer();
				}
			});
		} else {
			initSynapsePropertiesFromServer();
		}
		initWikiEntitiesAndVersions(c);
		view.initGlobalViewProperties();
		String showInUTC = cookieProvider.getCookie(SHOW_DATETIME_IN_UTC);
		if (showInUTC != null) {
			setShowUTCTime(Boolean.parseBoolean(showInUTC));
		}
	}
	
	public void initSynapsePropertiesFromServer() {
		synapseClient.getSynapseProperties(new AsyncCallback<HashMap<String, String>>() {			
			@Override
			public void onSuccess(HashMap<String, String> properties) {
				for (String key : properties.keySet()) {
					localStorage.put(key, properties.get(key), DateTimeUtilsImpl.getYearFromNow().getTime());
				}
				localStorage.put(PROPERTIES_LOADED_KEY, Boolean.TRUE.toString(), DateTimeUtilsImpl.getWeekFromNow().getTime());
			}
			
			@Override
			public void onFailure(Throwable caught) {
				synapseJSNIUtils.consoleError(caught.getMessage());
			}
		});
	}
	
	public void initWikiEntitiesAndVersions(Callback c) {
		initWikiEntities();
		initSynapseVersions(c);
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
		return localStorage.get(key);
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
	private void initWikiEntities() {
		wikiBasedEntites = new HashSet<String>();
		wikiBasedEntites.add(localStorage.get(WebConstants.GETTING_STARTED_GUIDE_ENTITY_ID_PROPERTY));
		wikiBasedEntites.add(localStorage.get(WebConstants.CREATE_PROJECT_ENTITY_ID_PROPERTY));
		wikiBasedEntites.add(localStorage.get(WebConstants.R_CLIENT_ENTITY_ID_PROPERTY));
		wikiBasedEntites.add(localStorage.get(WebConstants.PYTHON_CLIENT_ENTITY_ID_PROPERTY));
		wikiBasedEntites.add(localStorage.get(WebConstants.FORMATTING_GUIDE_ENTITY_ID_PROPERTY));
	}
	
	@Override
	public void pushCurrentPlace(Place targetPlace) {
		//only push this place into the history if it is a place change
		setCurrentPlaceInHistory(targetPlace, true);
	}
	
	@Override
	public void replaceCurrentPlace(Place targetPlace) {
		setCurrentPlaceInHistory(targetPlace, false);
	}
	
	
	private void setCurrentPlaceInHistory(Place targetPlace, boolean pushState) {
		//only push this place into the history if it is a place change
		try {
			if (targetPlace != null && !(targetPlace.equals(getCurrentPlace()))) {
				setLastPlace(getCurrentPlace());
				setCurrentPlace(targetPlace);
				String token = appPlaceHistoryMapper.getToken(targetPlace);
				if (pushState) {
					gwt.newItem(token, false);
				} else {
					gwt.replaceItem(token, false);
				}
				
				recordPlaceVisit(targetPlace);
			}	
		} catch(Throwable t) {
			synapseJSNIUtils.consoleError(t.getMessage());
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
	
	@Override
	public void setShowUTCTime(boolean showUTC) {
		Date yearFromNow = new Date();
		CalendarUtil.addMonthsToDate(yearFromNow, 12);
		cookieProvider.setCookie(SHOW_DATETIME_IN_UTC, Boolean.toString(showUTC), yearFromNow);
		dateTimeUtils.setShowUTCTime(showUTC);
	}
	
	@Override
	public boolean isShowingUTCTime() {
		return dateTimeUtils.isShowingUTCTime();
	}
	
	private static Integer timezoneOffsetMs = null;
	/**
	 * 
	 * @return the time difference between UTC time and local time, in milliseconds
	 */
	public static Integer getTimezoneOffsetMs() {
		if (timezoneOffsetMs == null) {
			timezoneOffsetMs = new Date().getTimezoneOffset() * 60 * 1000;
		}
		return timezoneOffsetMs;
	}
	
	@Override
	public PublicPrincipalIds getPublicPrincipalIds() {
		if (publicPrincipalIds == null) {
			publicPrincipalIds = new PublicPrincipalIds();
			publicPrincipalIds.setPublicAclPrincipalId(Long.parseLong(getSynapseProperty(WebConstants.PUBLIC_ACL_PRINCIPAL_ID)));
			publicPrincipalIds.setAnonymousUserId(Long.parseLong(getSynapseProperty(WebConstants.ANONYMOUS_USER_PRINCIPAL_ID)));
			publicPrincipalIds.setAuthenticatedAclPrincipalId(Long.parseLong(getSynapseProperty(WebConstants.AUTHENTICATED_ACL_PRINCIPAL_ID)));	
		}
		return publicPrincipalIds;
	}
}
