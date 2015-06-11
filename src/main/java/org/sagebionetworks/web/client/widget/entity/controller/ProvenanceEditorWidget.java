package org.sagebionetworks.web.client.widget.entity.controller;

import java.util.Set;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.provenance.Activity;
import org.sagebionetworks.repo.model.provenance.Used;
import org.sagebionetworks.repo.model.provenance.UsedEntity;
import org.sagebionetworks.repo.model.provenance.UsedURL;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProvenanceEditorWidget implements ProvenanceEditorWidgetView.Presenter {

	ProvenanceEditorWidgetView view;
	SynapseClientAsync synapseClient;
	SynapseAlert synAlert;
	PortalGinInjector ginInjector;
	ProvenanceListWidget usedProvenanceList;
	ProvenanceListWidget executedProvenanceList;

	
	@Inject
	public ProvenanceEditorWidget(ProvenanceEditorWidgetView view,
			SynapseClientAsync synapseClient, SynapseAlert synAlert,
			PortalGinInjector ginInjector) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.synAlert = synAlert;
		usedProvenanceList = ginInjector.getProvenanceListWidget();
		executedProvenanceList = ginInjector.getProvenanceListWidget();
		view.setSynAlertWidget(synAlert.asWidget());
		view.setUsedProvenanceList(usedProvenanceList.asWidget());
		view.setExecutedProvenanceList(executedProvenanceList.asWidget());
	}
	
	@Override
	public void configure(EntityBundle entityBundle) {
		
		Entity entity = entityBundle.getEntity();
		synapseClient.getActivityForEntity(entity.getId(), new AsyncCallback<Activity>() {
			
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
			
			@Override
			public void onSuccess(Activity result) {
				view.setName(result.getName());
				view.setDescription(result.getDescription());
				Set<Used> usedSet = result.getUsed();
				for (Used used: usedSet) {
					if (used.getWasExecuted()) {
						if (used instanceof UsedEntity) {
							usedProvenanceList.loadEntityRow(((UsedEntity)used).getReference().getTargetId());
						} else {
							UsedURL usedURL = (UsedURL) used;
							usedProvenanceList.loadURLRow(usedURL.getName(), (usedURL).getUrl());
						}
					} else {
						if (used instanceof UsedEntity) {
							executedProvenanceList.loadEntityRow(((UsedEntity)used).getReference().getTargetId());
						} else {
							UsedURL usedURL = (UsedURL) used;
							executedProvenanceList.loadURLRow(usedURL.getName(), (usedURL).getUrl());
						}
					}
				}
			}
		});
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	public void setVisible(boolean isVisible) {
		view.setVisible(isVisible);
	}
}
