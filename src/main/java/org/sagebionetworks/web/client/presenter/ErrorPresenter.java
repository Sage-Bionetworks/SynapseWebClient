package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.repo.model.LogEntry;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.view.ErrorView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class ErrorPresenter extends AbstractActivity implements ErrorView.Presenter, Presenter<org.sagebionetworks.web.client.place.ErrorPlace> {

	private ErrorView view;
	private SynapseClientAsync synapseClient;
	private SynapseAlert synAlert;
	private SynapseJSNIUtils jsniUtils;
	
	@Inject
	public ErrorPresenter(ErrorView view, 
			SynapseClientAsync synapseClient, 
			SynapseAlert synAlert,
			SynapseJSNIUtils jsniUtils) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.synAlert = synAlert;
		this.jsniUtils = jsniUtils;
		view.setPresenter(this);
		view.setSynAlertWidget(synAlert.asWidget());
	} 

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		panel.setWidget(view.asWidget());
	}
	
	@Override
	public void setPlace(final org.sagebionetworks.web.client.place.ErrorPlace place) {
		String token = place.toToken();
		showLogEntry(token);
	}
	
	public void showLogEntry(String encodedLogEntry) {
		view.clear();
		view.setPresenter(this);
		synAlert.clear();
		//decode log entry
		synapseClient.hexDecodeLogEntry(encodedLogEntry, new AsyncCallback<LogEntry>() {
			@Override
			public void onSuccess(LogEntry result) {
				view.setEntry(result);
				jsniUtils.consoleError(result.getLabel());
				jsniUtils.consoleError(result.getMessage());
				jsniUtils.consoleError(result.getStacktrace());
				if (!synAlert.isUserLoggedIn()) {
					synAlert.showLogin();
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	}
	
}
