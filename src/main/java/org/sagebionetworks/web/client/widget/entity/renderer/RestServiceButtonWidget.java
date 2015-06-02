package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.Map;

import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.UrlCache;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.shared.Endpoint;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.jsonp.client.JsonpRequestBuilder;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class RestServiceButtonWidget implements ButtonLinkWidgetView.Presenter, WidgetRendererPresenter {
	
	private ButtonLinkWidgetView view;
	private Map<String,String> descriptor;
	SynapseClientAsync synapseClient;
	String method, uri;
	Endpoint endpoint;
	@Inject
	public RestServiceButtonWidget( 
			ButtonLinkWidgetView view,
			SynapseClientAsync synapseClient,
			GWTWrapper gwt) {
		this.view = view;
		this.synapseClient = synapseClient;
		view.setPresenter(this);
	}
	
	@Override
	public void configure(final WikiPageKey wikiKey, final Map<String, String> widgetDescriptor, Callback widgetRefreshRequired, Long wikiVersionInView) {
		this.descriptor = widgetDescriptor;
		uri = descriptor.get(WidgetConstants.URI_KEY);
		method = descriptor.get(WidgetConstants.METHOD_KEY);
		endpoint = Endpoint.valueOf(descriptor.get(WidgetConstants.ENDPOINT_KEY));
		String buttonText = descriptor.get(WidgetConstants.TEXT_KEY);
		ButtonType buttonType = ButtonType.DEFAULT;
		if (descriptor.containsKey(WidgetConstants.BUTTON_TYPE_KEY)){
			buttonType = ButtonType.valueOf(descriptor.get(WidgetConstants.BUTTON_TYPE_KEY));
		}
		
//		view.configure(buttonText, buttonType);
		descriptor = widgetDescriptor;
	}
	
	public void doRequest() {
		String methodStringLowercase = method.toLowerCase().trim();
		if ("get".equals(methodStringLowercase)) {
			
		} else if ("put".equals(methodStringLowercase)) {
			
		} else if ("delete".equals(methodStringLowercase)) {
			
		} else if ("post".equals(methodStringLowercase)) {
			
		}
	}
	
	public void executeGet() {
		
	}
	
	public void executePut() {
		
	}
	
	public void executeDelete() {
		
	}
	
	public void executePost() {
		
	}
	@SuppressWarnings("unchecked")
	public void clearState() {
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

		/*
	 * Private Methods
	 */
}
