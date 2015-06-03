package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.HashMap;
import java.util.Map;

import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class RestServiceButtonWidget implements RestServiceButtonWidgetView.Presenter, WidgetRendererPresenter {
	
	private RestServiceButtonWidgetView view;
	private Map<String,String> descriptor;
	SynapseClientAsync synapseClient;
	SynapseJSNIUtils synapseJsniUtils;
	String method, uri, requestJson;
	Map<String, String> paramsMap;
	SynapseAlert alert;
	@Inject
	public RestServiceButtonWidget( 
			RestServiceButtonWidgetView view,
			SynapseClientAsync synapseClient,
			SynapseAlert alert,
			SynapseJSNIUtils synapseJsniUtils
			) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.alert = alert;
		this.synapseJsniUtils = synapseJsniUtils;
		view.setPresenter(this);
		view.setSynapseAlert(alert.asWidget());
	}
	
	@Override
	public void configure(final WikiPageKey wikiKey, final Map<String, String> widgetDescriptor, Callback widgetRefreshRequired, Long wikiVersionInView) {
		this.descriptor = widgetDescriptor;
		paramsMap = new HashMap<String, String>();
		uri = descriptor.get(WidgetConstants.URI_KEY);
		method = descriptor.get(WidgetConstants.METHOD_KEY);
		requestJson = descriptor.get(WidgetConstants.REQUEST_JSON_KEY);
		String buttonText = descriptor.get(WidgetConstants.TEXT_KEY);
		ButtonType buttonType = ButtonType.DEFAULT;
		if (descriptor.containsKey(WidgetConstants.BUTTON_TYPE_KEY)){
			buttonType = ButtonType.valueOf(descriptor.get(WidgetConstants.BUTTON_TYPE_KEY));
		}
		
		view.configure(buttonText, buttonType);
		descriptor = widgetDescriptor;
	}
	
	@Override
	public void onClick() {
		String methodStringLowercase = method.toLowerCase().trim();
		if ("get".equals(methodStringLowercase)) {
			executeGet();
		} else if ("put".equals(methodStringLowercase)) {
			executePut();
		} else if ("delete".equals(methodStringLowercase)) {
			executeDelete();
		} else if ("post".equals(methodStringLowercase)) {
			executePost();
		}
	}
	
	public void executeGet() {
		synapseClient.getFromRepo(uri, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				synapseJsniUtils.consoleLog(result);
				view.showSuccessMessage();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				alert.handleException(caught);
			}
		});
	}
	
	public void executePut() {
		synapseClient.putToRepo(uri, requestJson, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				synapseJsniUtils.consoleLog(result);
				view.showSuccessMessage();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				alert.handleException(caught);
			}
		});
	}
	
	public void executeDelete() {
		synapseClient.deleteFromRepo(uri, paramsMap, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				view.showSuccessMessage();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				alert.handleException(caught);
			}
		});
	}
	
	public void executePost() {
		synapseClient.postToRepo(uri, requestJson, paramsMap, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				synapseJsniUtils.consoleLog(result);
				view.showSuccessMessage();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				alert.handleException(caught);
			}
		});
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
