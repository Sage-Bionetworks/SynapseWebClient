package org.sagebionetworks.web.client.widget.googlemap;

import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
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

public class GoogleMap implements IsWidget {
	public static boolean isLoaded = false;
	SynapseJSNIUtils utils;
	RequestBuilderWrapper requestBuilder;
	private String jsonURL;
	public static final String S3_PREFIX = "https://s3.amazonaws.com/geoloc.sagebase.org/";
	GoogleMapView view;
	SynapseAlert synAlert;
	
	@Inject
	public GoogleMap(GoogleMapView view, SynapseJSNIUtils utils, RequestBuilderWrapper requestBuilder, SynapseAlert synAlert) {
		this.view = view;
		this.utils = utils;
		this.requestBuilder = requestBuilder;
		this.synAlert = synAlert;
		view.setSynAlert(synAlert.asWidget());
		loadScript();
	}
	
	public void configure(String teamId) {
		this.jsonURL = S3_PREFIX + teamId + ".json";
		initMap();
	}
	
	public void configure() {
		this.jsonURL = S3_PREFIX + "allPoints.json";
		initMap();
	}
	
	private void initMap() {
		view.setLoading(true);
		if (isLoaded && jsonURL != null) {
			requestBuilder.configure(RequestBuilder.GET, jsonURL);
			requestBuilder.setHeader(WebConstants.CONTENT_TYPE, WebConstants.TEXT_PLAIN_CHARSET_UTF8);
			try {
				requestBuilder.sendRequest(null, new RequestCallback() {
					@Override
					public void onResponseReceived(Request request,
							Response response) {
						int statusCode = response.getStatusCode();
						if (statusCode == Response.SC_OK) {
							view.setLoading(false);
							String data = response.getText();
							view.showMap(data);
						} else {
							onError(null, new IllegalArgumentException("Unable to retrieve map data for " + jsonURL + ". Reason: " + response.getStatusText()));
						}
					}

					@Override
					public void onError(Request request, Throwable exception) {
						view.setLoading(false);
						synAlert.handleException(exception);
					}
				});
			} catch (final Exception e) {
				view.setLoading(false);
				synAlert.handleException(e);
			}
		}
	}
	
	private void loadScript() {
		if (!isLoaded) {
			ScriptInjector.fromUrl("https://maps.googleapis.com/maps/api/js").setCallback(
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
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
