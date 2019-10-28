package org.sagebionetworks.web.client;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import static org.sagebionetworks.web.client.cookie.CookieKeys.SHOW_DATETIME_IN_UTC;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.cache.SessionStorage;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.mvp.AppActivityMapper;
import org.sagebionetworks.web.client.mvp.AppPlaceHistoryMapper;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.footer.VersionState;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.UmbrellaException;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.google.inject.Inject;

public class GlobalApplicationStateImpl implements GlobalApplicationState {
	public static final String RECENTLY_CHECKED_SYNAPSE_VERSION = "org.sagebionetworks.web.client.recently-checked-synapse-version";
	public static final String DEFAULT_REFRESH_PLACE = "!Home:0";
	private PlaceController placeController;
	private CookieProvider cookieProvider;
	private AppPlaceHistoryMapper appPlaceHistoryMapper;
	private StackConfigServiceAsync stackConfigService;
	private PlaceChanger placeChanger;
	
	private EventBus eventBus;
	private List<EntityHeader> favorites;
	private boolean isEditing;
	private SynapseJSNIUtils synapseJSNIUtils;
	private GlobalApplicationStateView view;
	private String synapseVersion;
	private ClientCache localStorage;
	private GWTWrapper gwt;
	private boolean isShowingVersionAlert;
	private DateTimeUtils dateTimeUtils;
	private SynapseJavascriptClient jsClient;
	private SessionStorage sessionStorage;
	private CallbackP<JavaScriptObject> fileListCallback;
	private SynapseProperties synapseProperties;
	boolean isDragDropInitialized = false;
	/**
	 * Last Place in the app
	 */
	public static String LAST_PLACE = "org.sagebionetworks.synapse.place.last.place";
	public static final ArrayList<String> SAFE_TO_IGNORE_ERRORS = new ArrayList<String>();
	static {
		//Benign error thrown by VideoWidget (<video>). ResizeObserver was not able to deliver all observations within a single animation frame.
		SAFE_TO_IGNORE_ERRORS.add("resizeobserver loop limit exceeded");
		//Server response was not json (html-based error page from the web server)
		SAFE_TO_IGNORE_ERRORS.add("error parsing json");
		//not actionable script errors
		SAFE_TO_IGNORE_ERRORS.add("script error. (:0)");
		SAFE_TO_IGNORE_ERRORS.add("unspecified error");
		SAFE_TO_IGNORE_ERRORS.add("unbekannter fehler");
		//DOM has changed such that insert of widget fails
		SAFE_TO_IGNORE_ERRORS.add("the node before which the new node is to be inserted is not a child of this node.");
		SAFE_TO_IGNORE_ERRORS.add("die eigenschaft \"removechild\" eines undefinierten oder nullverweises kann nicht abgerufen werden.");
		
	}
	@Inject
	public GlobalApplicationStateImpl(GlobalApplicationStateView view,
			CookieProvider cookieProvider,
			EventBus eventBus, 
			StackConfigServiceAsync stackConfigService, 
			SynapseJSNIUtils synapseJSNIUtils, 
			ClientCache localStorage, 
			GWTWrapper gwt,
			DateTimeUtils dateTimeUtils,
			SynapseJavascriptClient jsClient,
			SynapseProperties synapseProperties,
			SessionStorage sessionStorage) {
		this.cookieProvider = cookieProvider;
		this.eventBus = eventBus;
		this.stackConfigService = stackConfigService;
		fixServiceEntryPoint(stackConfigService);
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.localStorage = localStorage;
		this.dateTimeUtils = dateTimeUtils;
		this.gwt = gwt;
		this.view = view;
		this.jsClient = jsClient;
		isEditing = false;
		isShowingVersionAlert = false;
		this.synapseProperties = synapseProperties;
		this.sessionStorage = sessionStorage;
		initUncaughtExceptionHandler();
	}
	
	public void initUncaughtExceptionHandler() {
		GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {
			public void onUncaughtException(Throwable e) {
				handleUncaughtException(e);
			}
		});
	}
	
	public static boolean isIgnoredErrorMessage(String error) {
		if (error == null || error.trim().isEmpty()) {
			return false;
		}
		String lowercaseError = error.toLowerCase();
		for (String ignoredError : SAFE_TO_IGNORE_ERRORS) {
			if (lowercaseError.contains(ignoredError)) {
				return true;
			}
		}
		return false;
	}
	
	public void handleUncaughtException(Throwable e) {
		try {
			GWT.debugger();
			if (!isIgnoredErrorMessage(e.getMessage())) {
				jsClient.logError(unwrap(e));
			}
		} catch (Throwable t) {
			synapseJSNIUtils.consoleError("Unable to log uncaught exception to server: " + t.getMessage());
		} finally {
			synapseJSNIUtils.consoleError(e);	
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
	public Place getLastPlace() {
		return getLastPlace(null);
	}
	
	@Override
	public Place getLastPlace(Place defaultPlace) {
		String historyValue = sessionStorage.getItem(GlobalApplicationStateImpl.LAST_PLACE);
		return getPlaceFromHistoryValue(historyValue, fixIfNull(defaultPlace));
	}
	
	@Override
	public void clearLastPlace() {
		sessionStorage.removeItem(GlobalApplicationStateImpl.LAST_PLACE);
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
		sessionStorage.setItem(GlobalApplicationStateImpl.LAST_PLACE, appPlaceHistoryMapper.getToken(lastPlace));
	}

	@Override
	public Place getCurrentPlace() {
		//get the current place based on the current browser window history token
		String token = gwt.getCurrentHistoryToken();
		return appPlaceHistoryMapper.getPlace(token);
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
		
		stackConfigService.getSynapseVersions(new AsyncCallback<String>() {			
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
				view.showGetVersionError(caught.getMessage());
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
	
	public boolean isDragAndDropListenerSet() {
		return fileListCallback != null;
	}
	
	public void onDrop(JavaScriptObject fileList) {
		if (isDragAndDropListenerSet()) {
			fileListCallback.invoke(fileList);
		}
	}
	
	@Override
	public void initializeDropZone() {
		if (!isDragDropInitialized) {
			isDragDropInitialized = true;
			Element dropZoneElement = RootPanel.get("dropzone").getElement();
			Element rootPanelElement = RootPanel.get("rootPanel").getElement();
			_initializeDragDrop(this, dropZoneElement, rootPanelElement);
		}
	}
	
	private final static native void _initializeDragDrop(
			GlobalApplicationStateImpl globalAppState,
			Element dropZone,
			Element rootPanel
			) /*-{
		try {
			function showDropZone() {
				dropZone.style.display = "block";
			}
			
			function hideDropZone() {
				dropZone.style.display = "none";
			}
			
			$wnd.addEventListener('dragenter', function(e) {
				if (globalAppState.@org.sagebionetworks.web.client.GlobalApplicationStateImpl::isDragAndDropListenerSet()()) {
					showDropZone();
				}
			});
			
			function allowDrag(e) {
				e.dataTransfer.dropEffect = 'copy';
				e.preventDefault();
			}
	
			function handleDrop(e) {
				e.preventDefault();
				hideDropZone();
				globalAppState.@org.sagebionetworks.web.client.GlobalApplicationStateImpl::onDrop(Lcom/google/gwt/core/client/JavaScriptObject;)(e.dataTransfer.files);
			}
	
			dropZone.addEventListener('dragenter', allowDrag);
			dropZone.addEventListener('dragover', allowDrag);
	
			dropZone.addEventListener('drop', handleDrop);
			
			//if files are dropped into the root panel, then ignore the event (do not open file contents if user does not have the upload dialog open).
			rootPanel.addEventListener('drop', function(e) {
				e.preventDefault();
			});
			rootPanel.addEventListener('dragenter', allowDrag);
			rootPanel.addEventListener('dragover', allowDrag);
		} catch (err) {
			console.error(err);
		}
	}-*/;
	
	@Override
	public void setDropZoneHandler(CallbackP<JavaScriptObject> fileListCallback) {
		this.fileListCallback = fileListCallback;
	}
	
	@Override
	public void clearDropZoneHandler() {
		fileListCallback = null;
	}
	
	@Override
	public void init(final Callback finalCallback) {
		synapseProperties.initSynapseProperties(() -> {
			initStep2(finalCallback);
		});
	}
	
	private void initStep2(Callback finalCallback) {
		view.initGlobalViewProperties();
		String showInUTC = cookieProvider.getCookie(SHOW_DATETIME_IN_UTC);
		if (showInUTC != null) {
			setShowUTCTime(Boolean.parseBoolean(showInUTC));
		}
		finalCallback.invoke();
	}
	@Override
	public void back() {
		view.back();
	}
}
