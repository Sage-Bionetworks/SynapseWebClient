package org.sagebionetworks.web.client.widget.googlemap;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadHelper;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class GoogleMap implements IsWidget, GoogleMapView.Presenter {
	public static boolean isLoaded = false;
	SynapseJSNIUtils utils;
	RequestBuilderWrapper requestBuilder;
	private String jsonURL;
	public static final String S3_PREFIX = "https://s3.amazonaws.com/geoloc.sagebase.org/";
	GoogleMapView view;
	SynapseAlert synAlert;
	PortalGinInjector ginInjector;
	LazyLoadHelper lazyLoadHelper;
	@Inject
	public GoogleMap(GoogleMapView view, 
			SynapseJSNIUtils utils, 
			RequestBuilderWrapper requestBuilder, 
			SynapseAlert synAlert, 
			PortalGinInjector ginInjector, 
			LazyLoadHelper lazyLoadHelper) {
		this.view = view;
		this.utils = utils;
		this.requestBuilder = requestBuilder;
		this.synAlert = synAlert;
		this.ginInjector = ginInjector;
		this.lazyLoadHelper = lazyLoadHelper;
		lazyLoadHelper.configure(new org.sagebionetworks.web.client.utils.Callback() {
			@Override
			public void invoke() {
				initMap();
			}
		}, view);
		view.setSynAlert(synAlert.asWidget());
		view.setPresenter(this);
		loadScript();
	}
	
	public void configure(String teamId) {
		this.jsonURL = S3_PREFIX + teamId + ".json";
		lazyLoadHelper.setIsConfigured();
	}
	
	public void configure() {
		this.jsonURL = S3_PREFIX + "allPoints.json";
		lazyLoadHelper.setIsConfigured();
	}
	
	private void initMap() {
		if (isLoaded && jsonURL != null) {
			getFileContents(jsonURL, new CallbackP<String>() {
				@Override
				public void invoke(String data) {
					view.showMap(data);
				}
			});
		}
	}
	
	public void getFileContents(String url, final CallbackP<String> c) {
		requestBuilder.configure(RequestBuilder.GET, url);
		requestBuilder.setHeader(WebConstants.CONTENT_TYPE, WebConstants.TEXT_PLAIN_CHARSET_UTF8);
		try {
			requestBuilder.sendRequest(null, new RequestCallback() {
				@Override
				public void onResponseReceived(Request request,
						Response response) {
					int statusCode = response.getStatusCode();
					if (statusCode == Response.SC_OK) {
						c.invoke(response.getText());
					} else {
						onError(null, new IllegalArgumentException("Unable to retrieve map data for " + jsonURL + ". Reason: " + response.getStatusText()));
					}
				}

				@Override
				public void onError(Request request, Throwable exception) {
					synAlert.handleException(exception);
				}
			});
		} catch (final Exception e) {
			synAlert.handleException(e);
		}
	}
	
	private void loadScript() {
		if (!isLoaded) {
			getFileContents(S3_PREFIX + "googlemap.txt", new CallbackP<String>() {
				@Override
				public void invoke(String key) {
					ScriptInjector.fromUrl("https://maps.googleapis.com/maps/api/js?key=" + key).setCallback(
						     new Callback<Void, Exception>() {
								@Override
								public void onSuccess(Void result) {
									isLoaded = true;
									utils.consoleLog("Loaded Google Maps API");
									initMap();
								}
								
								@Override
								public void onFailure(Exception reason) {
									synAlert.handleException(reason);
								}
							}).inject();
				}
			});
		}
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	@Override
	public void markerClicked(String location, List<String> userIds) {
		//create user badges
		List<Widget> userBadges = new ArrayList<Widget>();
		for (String userId : userIds) {
			UserBadge userBadge = ginInjector.getUserBadgeWidget();
			userBadge.configure(userId);
			userBadges.add(userBadge.asWidget());
		}
		view.showUsers(location, userBadges);
	}
	
	public void setHeight(String height) {
		view.setHeight(height);
	}
}
