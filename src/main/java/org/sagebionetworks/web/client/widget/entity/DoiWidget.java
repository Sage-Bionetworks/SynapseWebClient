package org.sagebionetworks.web.client.widget.entity;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

import org.sagebionetworks.repo.model.doi.Doi;
import org.sagebionetworks.repo.model.doi.DoiStatus;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.StackConfigServiceAsync;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DoiWidget implements IsWidget {

	public static final int REFRESH_TIME = 13 * 1000; //5 seconds
	private DoiWidgetView view;
	private StackConfigServiceAsync stackConfigService;
	GlobalApplicationState globalApplicationState;
	AuthenticationController authenticationController;
	SynapseClientAsync synapseClient;
	
	private Timer timer = null;
	
	Doi doi;
	String entityId;
	Long versionNumber;
	boolean canEdit;
	SynapseAlert synAlert;
	
	@Inject
	public DoiWidget(DoiWidgetView view,
			GlobalApplicationState globalApplicationState, 
			StackConfigServiceAsync stackConfigService,
			AuthenticationController authenticationController,
			SynapseClientAsync synapseClient,
			SynapseAlert synAlert) {
		this.view = view;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.stackConfigService = stackConfigService;
		fixServiceEntryPoint(stackConfigService);
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.synAlert = synAlert;
		view.setSynAlert(synAlert);
	}
	
	public Widget asWidget() {
		return view.asWidget();
	}
	
	public void configure(Doi newDoi, final String entityId) {
		clear();
		timer = null;
		if (newDoi != null) {
			this.doi = newDoi;
			this.entityId = entityId;
			this.versionNumber = newDoi.getObjectVersion();
			final DoiStatus doiStatus = newDoi.getDoiStatus();
			if (doiStatus == DoiStatus.ERROR) {
				view.showDoiError();
			} else if (doiStatus == DoiStatus.IN_PROCESS) {
				view.showDoiInProgress();
			} else if (doiStatus == DoiStatus.CREATED || doiStatus == DoiStatus.READY) {
				synAlert.clear();
				getDoiPrefix(new AsyncCallback<String>() {
					@Override
					public void onSuccess(String prefix) {
						view.showDoiCreated(getDoi(prefix, doiStatus == DoiStatus.READY));
					}
					@Override
					public void onFailure(Throwable caught) {
						synAlert.handleException(caught);
					}
				});
			}		
			if (doiStatus == DoiStatus.IN_PROCESS) {
				timer = new Timer() {
					public void run() {
						getEntityDoi(entityId, versionNumber);
					};
				};
				//schedule a timer to update the DOI status later
				timer.schedule(REFRESH_TIME);
			};
		}
	}

	public void getEntityDoi(final String entityId, Long versionNumber) {
		synAlert.clear();
		synapseClient.getEntityDoi(entityId, versionNumber, new AsyncCallback<Doi>() {
			@Override
			public void onSuccess(Doi result) {
				configure(result, entityId);
			}
			@Override
			public void onFailure(Throwable caught) {
				if (!(caught instanceof NotFoundException)) {
					synAlert.handleException(caught);
				}
			}
		});
	}

	public void getDoiPrefix(AsyncCallback<String> callback) {
		stackConfigService.getDoiPrefix(callback);
	}
	
	public String getDoi(String prefix, boolean isReady) {
		String doi = "";
		if (prefix != null && prefix.length() > 0) {
			String versionString = "";
			if (versionNumber != null) {
				versionString = "." + versionNumber;
			}
			
			doi = prefix + entityId + versionString;
		}
		return doi;
	}
	
	public void clear() {
		view.setVisible(false);
		view.clear();
	}

}
