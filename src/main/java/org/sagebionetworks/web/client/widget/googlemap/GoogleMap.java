package org.sagebionetworks.web.client.widget.googlemap;

import java.util.ArrayList;
import java.util.List;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadHelper;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class GoogleMap implements IsWidget, GoogleMapView.Presenter {
	public static boolean isLoaded = false;
	SynapseJSNIUtils utils;
	SynapseJavascriptClient jsClient;
	private String jsonURL;
	public static final String S3_PREFIX = "https://s3.amazonaws.com/geoloc.sagebase.org/";
	public static final String ALL_POINTS_URL = S3_PREFIX + "allPoints.json";
	public static final String GOOGLE_MAP_URL = S3_PREFIX + "googlemap.txt";
	GoogleMapView view;
	SynapseAlert synAlert;
	PortalGinInjector ginInjector;
	LazyLoadHelper lazyLoadHelper;

	@Inject
	public GoogleMap(GoogleMapView view, SynapseJSNIUtils utils, SynapseJavascriptClient jsClient, SynapseAlert synAlert, PortalGinInjector ginInjector, LazyLoadHelper lazyLoadHelper) {
		this.view = view;
		this.utils = utils;
		this.jsClient = jsClient;
		this.synAlert = synAlert;
		this.ginInjector = ginInjector;
		this.lazyLoadHelper = lazyLoadHelper;
		lazyLoadHelper.configure(new org.sagebionetworks.web.client.utils.Callback() {
			@Override
			public void invoke() {
				loadData();
			}
		}, view);
		view.setSynAlert(synAlert.asWidget());
		view.setPresenter(this);
	}

	public void configure(String teamId) {
		this.jsonURL = S3_PREFIX + teamId + ".json";
		lazyLoadHelper.setIsConfigured();
	}

	public void configure() {
		this.jsonURL = ALL_POINTS_URL;
		lazyLoadHelper.setIsConfigured();
	}

	public void initMap() {
		if (isLoaded && jsonURL != null) {
			getFileContents(jsonURL, new CallbackP<String>() {
				@Override
				public void invoke(String data) {
					view.showMap(data);
				}
			});
		}
	}

	public void getFileContents(final String url, final CallbackP<String> c) {
		boolean forceAnonymous = true;
		jsClient.doGetString(url, forceAnonymous, new AsyncCallback<String>() {
			@Override
			public void onFailure(Throwable ex) {
				synAlert.handleException(ex);
			}

			public void onSuccess(String result) {
				c.invoke(result);
			};
		});
	}

	public static void initGoogleLibrary(SynapseJavascriptClient jsClient, AsyncCallback<Void> callback) {
		if (!isLoaded) {
			boolean forceAnonymous = true;
			jsClient.doGetString(GOOGLE_MAP_URL, forceAnonymous, new AsyncCallback<String>() {
				@Override
				public void onFailure(Throwable ex) {
					if (callback != null) {
						callback.onFailure(ex);
					}
				}

				public void onSuccess(String key) {
					ScriptInjector.fromUrl("https://maps.googleapis.com/maps/api/js?key=" + key + "&libraries=places").setCallback(new Callback<Void, Exception>() {
						@Override
						public void onSuccess(Void result) {
							isLoaded = true;
							if (callback != null) {
								callback.onSuccess(null);
							}
						}

						@Override
						public void onFailure(Exception reason) {
							if (callback != null) {
								callback.onFailure(reason);
							}
						}
					}).inject();
				};
			});
		} else {
			if (callback != null) {
				callback.onSuccess(null);
			}
		}
	}

	private void loadData() {
		initGoogleLibrary(jsClient, new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}

			@Override
			public void onSuccess(Void result) {
				initMap();
			}
		});
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void markerClicked(String location, List<String> userIds) {
		// create user badges
		List<Widget> userBadges = new ArrayList<Widget>();
		for (String userId : userIds) {
			UserBadge userBadge = ginInjector.getUserBadgeWidget();
			userBadge.setOpenInNewWindow();
			userBadge.configure(userId);
			userBadges.add(userBadge.asWidget());
		}
		view.showUsers(location, userBadges);
	}

	public void setHeight(String height) {
		view.setHeight(height);
	}

	public void setVisible(boolean visible) {
		view.setVisible(visible);
	}
}
