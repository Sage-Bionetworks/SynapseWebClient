package org.sagebionetworks.web.client;
import org.sagebionetworks.web.client.mvp.AppActivityMapper;
import org.sagebionetworks.web.client.mvp.AppPlaceHistoryMapper;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;

import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Portal implements EntryPoint {
	
	//If there's a failure to load the code from the server, how long (in ms) should we wait before trying again...
	public static final int CODE_LOAD_DELAY = 5000;
	//  We are using gin to create all of our objects
	private static final PortalGinInjector ginjector = GWT.create(PortalGinInjector.class);
	private SimplePanel appWidget = new SimplePanel();
	public final static native void _consoleError(String message) /*-{
		console.error(message);
	}-*/;

	public static PortalGinInjector getInjector() {
		return ginjector;
	}
	
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		zeroOpacity(RootPanel.get("headerPanel"), RootPanel.get("rootPanel"));
		//we might need to reload using the new token scheme (required for SEO)
		String initToken = History.getToken();
		if (initToken.length() > 0 && !initToken.startsWith("!")) {
			String fullUrl = Window.Location.getHref();
			fullUrl = fullUrl.replace("#"+initToken, "#!"+initToken);
			Window.Location.replace(fullUrl);
			Window.Location.reload();
		} else {
			// This is a split point where the browser can download the first large code file.
			GWT.runAsync(new RunAsyncCallback() {
				@Override
				public void onFailure(Throwable reason) {
					//SWC-2444: if there is a problem getting the code, try to reload the app after some time
					_consoleError(reason.getMessage());
					reloadApp(CODE_LOAD_DELAY);
				}

				@Override
				public void onSuccess() {
					try {
						// load the previous session, if there is one
						ginjector.getAuthenticationController().reloadUserSessionData(() -> {
							// make sure jsni utils code is available to the client
							ginjector.getSynapseJSNIUtils();
							EventBus eventBus = ginjector.getEventBus();
							PlaceController placeController = new PlaceController(eventBus);

							// Start ActivityManager for the main widget with our ActivityMapper
							AppActivityMapper activityMapper = new AppActivityMapper(ginjector, new SynapseJSNIUtilsImpl(), null);
							ActivityManager activityManager = new ActivityManager(activityMapper, eventBus);
							activityManager.setDisplay(appWidget);
							
							// All pages get added to the root panel
							appWidget.addStyleName("rootPanel");

							// Start PlaceHistoryHandler with our PlaceHistoryMapper
							AppPlaceHistoryMapper historyMapper = GWT.create(AppPlaceHistoryMapper.class);
							final PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper);		
							historyHandler.register(placeController, eventBus, AppActivityMapper.getDefaultPlace());						
							Header header = ginjector.getHeader();
							RootPanel.get("headerPanel").add(header);
							Footer footer = ginjector.getFooter();
							RootPanel.get("footerPanel").add(footer);
							
							RootPanel.get("rootPanel").add(appWidget);
							RootPanel.get("initialLoadingUI").setVisible(false);
							fullOpacity(RootPanel.get("headerPanel"), RootPanel.get("rootPanel"));
							final GlobalApplicationState globalApplicationState = ginjector.getGlobalApplicationState();
							globalApplicationState.setPlaceController(placeController);
							globalApplicationState.setAppPlaceHistoryMapper(historyMapper);
							globalApplicationState.init(new Callback() {
								
								@Override
								public void invoke() {
									//listen for window close (or navigating away)
									registerWindowClosingHandler(globalApplicationState);
									registerOnPopStateHandler(globalApplicationState);
									
									// start version timer
									ginjector.getVersionTimer().start();
									// start timer to check for user session state change (session expired, or user explicitly logged out)
									ginjector.getSessionDetector().start();
									
									// Goes to place represented on URL or default place
									historyHandler.handleCurrentHistory();
									delayLoadOfZxcvbn();
									globalApplicationState.initializeDropZone();
								}
							});
						});
					} catch (Throwable e) {
						onFailure(e);
					}
				}
			});
		}
	}
	
	public static void zeroOpacity(RootPanel...panels) {
		for (RootPanel panel : panels) {
			panel.removeStyleName("fullOpacity");
			panel.addStyleName("zeroOpacity");
		}
	}
	
	public static void fullOpacity(RootPanel...panels) {
		for (RootPanel panel : panels) {
			panel.removeStyleName("zeroOpacity");
			panel.addStyleName("fullOpacity");
		}
	}
	
	public void delayLoadOfZxcvbn() {
		Timer timer = new Timer() { 
		    public void run() {
		    	ScriptInjector.fromUrl("//cdnjs.cloudflare.com/ajax/libs/zxcvbn/4.4.2/zxcvbn.js")
		    		.setWindow(ScriptInjector.TOP_WINDOW)
		    		.inject();
		    }
		};
		timer.schedule(10000);
	}
	
	public void reloadApp(int delay) {
		Timer timer = new Timer() { 
		    public void run() { 
		    	Window.Location.reload();
		    } 
		};
		timer.schedule(delay);
	}

	
	private void registerWindowClosingHandler(final GlobalApplicationState globalApplicationState) {
		Window.addWindowClosingHandler(new Window.ClosingHandler() {
		      public void onWindowClosing(Window.ClosingEvent closingEvent) {
		    	  if (globalApplicationState.isEditing())
		    		  closingEvent.setMessage(DisplayConstants.CLOSE_PORTAL_CONFIRMATION_MESSAGE);
		      }
		    });
	}

	private void registerOnPopStateHandler(final GlobalApplicationState globalApplicationState) {
		globalApplicationState.initOnPopStateHandler();
	}
}
