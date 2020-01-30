package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.Map;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CytoscapeWidget implements CytoscapeView.Presenter, WidgetRendererPresenter {

	private CytoscapeView view;
	private Map<String, String> descriptor;
	AuthenticationController authenticationController;
	RequestBuilderWrapper requestBuilder;
	SynapseAlert synAlert;
	SynapseJSNIUtils synapseJsniUtils;
	String entityId, styleEntityId, height;
	String mainFileContents, styleFileContents;
	public static final String DEFAULT_HEIGHT = "500";

	@Inject
	public CytoscapeWidget(CytoscapeView view, AuthenticationController authenticationController, RequestBuilderWrapper requestBuilder, SynapseAlert synAlert, SynapseJSNIUtils synapseJsniUtils) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.requestBuilder = requestBuilder;
		this.synAlert = synAlert;
		this.synapseJsniUtils = synapseJsniUtils;
		view.setSynAlert(synAlert.asWidget());
		view.setPresenter(this);
	}

	@Override
	public void configure(final WikiPageKey wikiKey, final Map<String, String> widgetDescriptor, Callback widgetRefreshRequired, Long wikiVersionInView) {
		view.setGraphVisible(false);
		this.descriptor = widgetDescriptor;
		entityId = descriptor.get(WidgetConstants.SYNAPSE_ID_KEY);
		styleEntityId = descriptor.get(WidgetConstants.STYLE_SYNAPSE_ID_KEY);
		height = DEFAULT_HEIGHT;
		if (descriptor.containsKey(WidgetConstants.HEIGHT_KEY)) {
			height = descriptor.get(WidgetConstants.HEIGHT_KEY);
		}
		mainFileContents = null;
		styleFileContents = null;
		getMainFileContents();
	}

	public void getMainFileContents() {
		CallbackP<String> callback = new CallbackP<String>() {
			public void invoke(String fileContents) {
				mainFileContents = fileContents;
				getStyleFileContents();
			};
		};
		getFileContents(entityId, callback);
	}

	public void getStyleFileContents() {
		if (DisplayUtils.isDefined(styleEntityId)) {
			CallbackP<String> callback = new CallbackP<String>() {
				public void invoke(String fileContents) {
					styleFileContents = fileContents;
					showGraph();
				};
			};
			getFileContents(styleEntityId, callback);
		} else {
			// no style file defined, we're ready to show the graph
			showGraph();
		}
	}

	public void showGraph() {
		view.setGraphVisible(true);
		view.configure(mainFileContents, styleFileContents, height);
	}

	public void getFileContents(final String entityId, final CallbackP<String> fileContentCallback) {
		Long version = null;
		synAlert.clear();
		String url = DisplayUtils.createFileEntityUrl(synapseJsniUtils.getBaseFileHandleUrl(), entityId, version, false, true);
		requestBuilder.configure(RequestBuilder.GET, url);
		try {
			requestBuilder.sendRequest(null, new RequestCallback() {
				public void onError(final Request request, final Throwable e) {
					synAlert.handleException(e);
				}

				public void onResponseReceived(final Request request, final Response response) {
					// add the response text
					int statusCode = response.getStatusCode();
					if (statusCode == Response.SC_OK) {
						String responseText = response.getText();
						if (responseText != null && responseText.length() > 0) {
							fileContentCallback.invoke(responseText);
						} else {
							onError(null, new IllegalArgumentException("Unable to retrieve Cytoscape JS data file entity " + entityId));
						}
					}
				}
			});
		} catch (final Exception e) {
			synAlert.handleException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public void clearState() {}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	/*
	 * Private Methods
	 */
}
