package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.NrgrSynapseGlue;
import org.sagebionetworks.web.client.view.NrgrSynapseGlueView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class NrgrSynapseGluePresenter extends AbstractActivity implements NrgrSynapseGlueView.Presenter,Presenter<NrgrSynapseGlue> {

	private NrgrSynapseGlueView view;
	private PopupUtilsView popupUtils;
	private SynapseAlert synAlert;
	private SynapseJavascriptClient jsClient;

	@Inject
	public NrgrSynapseGluePresenter(NrgrSynapseGlueView view, PopupUtilsView popupUtils, SynapseAlert synAlert, SynapseJavascriptClient jsClient) {
		this.view = view;
		this.popupUtils = popupUtils;
		this.synAlert = synAlert;
		this.jsClient = jsClient;
		view.setPresenter(this);
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		panel.setWidget(view.asWidget());
	}

	@Override
	public void setPlace(NrgrSynapseGlue place) {
		String token = place.toToken();
		view.refreshHeader();
		if (token != null && !token.equalsIgnoreCase(ClientProperties.DEFAULT_PLACE_TOKEN)) {
			view.setNrgrToken(token);	
		}		
	}
	@Override
	public void onSubmitToken() {
		// call the service (see IT-834)
		String token = view.getNrgrToken();
		synAlert.clear();
		jsClient.submitNRGRDataAccessToken(token, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String responseMessage) {
				// on 201, pop up the message (note that this might be an error or success message, so do not clear the text area)
				popupUtils.showInfoDialog("", responseMessage, null);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// on error, show in synAlert
				synAlert.handleException(caught);
			}
		});
	}
}
