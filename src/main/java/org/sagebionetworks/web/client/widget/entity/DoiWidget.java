package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.doi.Doi;
import org.sagebionetworks.repo.model.doi.DoiStatus;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.StackConfigServiceAsync;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.DoiWidgetView.Presenter;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DoiWidget implements Presenter {

	public static final String DOI = "doi:";
	public static final int REFRESH_TIME = 13 * 1000; //13 seconds
	private DoiWidgetView view;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	private StackConfigServiceAsync stackConfigService;
	
	private Timer timer = null;
	
	Doi doi;
	String entityId;
	Long versionNumber;
	boolean canEdit;
	
	@Inject
	public DoiWidget(DoiWidgetView view,
			SynapseClientAsync synapseClient,
			NodeModelCreator nodeModelCreator,
			GlobalApplicationState globalApplicationState, 
			StackConfigServiceAsync stackConfigService) {
		this.view = view;
		this.view.setPresenter(this);
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.stackConfigService = stackConfigService;
	}
	
	public void configure(String entityId, boolean canEdit, Long versionNumber) {
		this.entityId = entityId;
		this.versionNumber = versionNumber;
		this.canEdit = canEdit;
		configureDoi();
	}
	
	public Widget asWidget() {
		return view.asWidget();
	}

	public void configureDoi() {
		view.clear();
		//get this entity's Doi (if it has one)
		doi = null;
		timer = null;
		synapseClient.getEntityDoi(entityId, versionNumber, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				//construct the DOI
				try {
					doi = nodeModelCreator.createJSONEntity(result, Doi.class);
					view.showDoi(doi.getDoiStatus());
					if ((doi.getDoiStatus() == DoiStatus.IN_PROCESS) && timer == null) {
						timer = new Timer() {
							public void run() {
								configureDoi();
							};
						};
						//schedule a timer to update the DOI status later
						timer.schedule(REFRESH_TIME);
					};
				} catch (JSONObjectAdapterException e) {							
					onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof NotFoundException) {
					if (canEdit)
						view.showCreateDoi();
				} else {
					view.showErrorMessage(caught.getMessage());
				}
			}
		});
	}

	@Override
	public void createDoi() {
		synapseClient.createDoi(entityId, versionNumber, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void v) {
				view.showInfo(DisplayConstants.DOI_REQUEST_SENT_TITLE, DisplayConstants.DOI_REQUEST_SENT_MESSAGE);
				configureDoi();
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		});
	}
	
	@Override
	public void getDoiPrefix(AsyncCallback<String> callback) {
		stackConfigService.getDoiPrefix(callback);
	}
	
	@Override
	public String getDoiHtml(String prefix, boolean isReady) {
		String html = "";
		if (prefix != null && prefix.length() > 0) {
			String versionString = "";
			if (versionNumber != null) {
				versionString = "." + versionNumber;
			}
			
			String fullDoi = prefix + entityId + versionString;
			String doiName = prefix.substring(DOI.length()) + entityId + versionString;
			if (isReady)
				html = getDoiLink(fullDoi, doiName);
			else
				html = getDoiSpan(fullDoi);
		}
		return html;
	}
	
	public static String getDoiLink(String fullDoi, String doiName){
		return "<a target=\"_blank\" class=\"link\" href=\"http://dx.doi.org/" +
				doiName + "\">" + fullDoi +"</a>";
	}
	
	public static String getDoiSpan(String fullDoi){
		return "<span>" + fullDoi +"</span>";
	}

	
	public void clear() {
		view.clear();
	}

}
