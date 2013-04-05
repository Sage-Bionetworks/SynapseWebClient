package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.repo.model.doi.Doi;
import org.sagebionetworks.repo.model.doi.DoiStatus;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.DoiWidgetView.Presenter;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DoiWidget implements Presenter {

	private DoiWidgetView view;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	private JSONObjectAdapter jsonObjectAdapter;
	
	Doi doi;
	String entityId;
	Long versionNumber;
	
	@Inject
	public DoiWidget(DoiWidgetView view,
			SynapseClientAsync synapseClient,
			NodeModelCreator nodeModelCreator,
			JSONObjectAdapter jsonObjectAdapter,
			GlobalApplicationState globalApplicationState) {
		this.view = view;
		this.view.setPresenter(this);
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.jsonObjectAdapter = jsonObjectAdapter;
	}
	
	public void configure(String entityId, Long versionNumber) {
		this.entityId = entityId;
		this.versionNumber = versionNumber;
		configureDoi();
	}
	
	public Widget asWidget() {
		return view.asWidget();
	}

	public void configureDoi() {
		//get this entity's Doi (if it has one)
		doi = null;
		synapseClient.getEntityDoi(entityId, versionNumber, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				if(result == null || result.length() == 0) {
					view.showCreateDoi();
				} 
				else {
					//construct the DOI
					try {
						doi = nodeModelCreator.createJSONEntity(result, Doi.class);
						view.showDoi(doi);
					} catch (JSONObjectAdapterException e) {							
						onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
					}

					
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
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
}
