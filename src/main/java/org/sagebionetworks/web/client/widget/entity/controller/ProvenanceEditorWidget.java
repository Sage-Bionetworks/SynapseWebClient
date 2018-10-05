package org.sagebionetworks.web.client.widget.entity.controller;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.provenance.Activity;
import org.sagebionetworks.repo.model.provenance.Used;
import org.sagebionetworks.repo.model.provenance.UsedEntity;
import org.sagebionetworks.repo.model.provenance.UsedURL;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;

import com.google.gwt.event.shared.EventBus;
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
	Activity activity;
	EntityFinder entityFinder;
	ProvenanceURLDialogWidget urlDialog;
	EventBus eventBus;
	
	@Inject
	public ProvenanceEditorWidget(ProvenanceEditorWidgetView view,
			SynapseClientAsync synapseClient, SynapseAlert synAlert,
			PortalGinInjector ginInjector, EntityFinder entityFinder,
			ProvenanceURLDialogWidget urlDialog,
			EventBus eventBus) {
		this.view = view;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.synAlert = synAlert;
		this.ginInjector = ginInjector;
		this.entityFinder = entityFinder;
		this.urlDialog = urlDialog;
		this.eventBus = eventBus;
		usedProvenanceList = ginInjector.getProvenanceListWidget();
		executedProvenanceList = ginInjector.getProvenanceListWidget();
		usedProvenanceList.setEntityFinder(entityFinder);
		usedProvenanceList.setURLDialog(urlDialog);
		executedProvenanceList.setEntityFinder(entityFinder);
		executedProvenanceList.setURLDialog(urlDialog);
		view.setSynAlertWidget(synAlert);
		view.setUsedProvenanceList(usedProvenanceList);
		view.setExecutedProvenanceList(executedProvenanceList);
		view.setURLDialog(urlDialog);
		view.setPresenter(this);
	}
	
	public void configure(EntityBundle entityBundle) {
		clear();
		Entity entity = entityBundle.getEntity();
		synapseClient.getOrCreateActivityForEntityVersion(entity.getId(), null, new AsyncCallback<Activity>() {
			
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
			
			@Override
			public void onSuccess(Activity result) {
				activity = result;
				view.setName(result.getName());
				view.setDescription(result.getDescription());
				Set<Used> allProvenance = result.getUsed();
				if (allProvenance != null) {
					List<ProvenanceEntry> usedEntries = new LinkedList<ProvenanceEntry>();
					List<ProvenanceEntry> executedEntries = new LinkedList<ProvenanceEntry>();
					for (Used provEntry: allProvenance) {    
						ProvenanceEntry toAdd;
						if (provEntry instanceof UsedEntity) {
							Reference ref = ((UsedEntity)provEntry).getReference();
							toAdd = ginInjector.getEntityRefEntry();
							String entityId = ref.getTargetId();
							Long version = ref.getTargetVersionNumber();
							String versionString = null;
							if (version != null) {
								versionString = version.toString();
							}
							((EntityRefProvEntryView)toAdd).configure(entityId, versionString);
							toAdd.setAnchorTarget(DisplayUtils.getSynapseHistoryToken(entityId, version));
						} else {
							UsedURL usedURL = (UsedURL) provEntry;
							toAdd = ginInjector.getURLEntry();
							String name = usedURL.getName();
							String url = usedURL.getUrl();
							if (name == null || name.trim().isEmpty()) {
								name = usedURL.getUrl();
							}
							((URLProvEntryView)toAdd).configure(name, url);
							toAdd.setAnchorTarget(url);
						}
						
						if (provEntry.getWasExecuted()) {
							executedEntries.add(toAdd);
						} else {
							usedEntries.add(toAdd);
						}
					}
					usedProvenanceList.configure(usedEntries);
					executedProvenanceList.configure(executedEntries);
				}
			}
		});
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	public void show() {
		clear();
		view.show();
	}
	
	public void hide() {
		view.hide();
	}
	
	@Override
	public void clear() {
		usedProvenanceList.clear();
		executedProvenanceList.clear();
		synAlert.clear();
		view.clear();
	}

	@Override
	public void onSave() {
		activity.setName(view.getName());
		activity.setDescription(view.getDescription());
		List<ProvenanceEntry> usedEntries = usedProvenanceList.getEntries();
		List<ProvenanceEntry> executedEntries = executedProvenanceList.getEntries();
		Set<Used> usedSet = new HashSet<Used>();
		usedSet.addAll(provEntryListToUsedSet(usedEntries, false));
		usedSet.addAll(provEntryListToUsedSet(executedEntries, true));
		activity.setUsed(usedSet);
		synapseClient.putActivity(activity, new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}

			@Override
			public void onSuccess(Void result) {
				view.hide();
				eventBus.fireEvent(new EntityUpdatedEvent());
			}
		});
		
	}
	
	private Set<Used> provEntryListToUsedSet(List<ProvenanceEntry> entries, boolean wasExecuted) {
		Set<Used> usedSet = new HashSet<Used>();
		for (ProvenanceEntry used: entries) {
			if (used instanceof URLProvEntryView) {
				URLProvEntryView urlEntry = (URLProvEntryView) used;
				UsedURL activityURL = new UsedURL();
				activityURL.setUrl(urlEntry.getURL());
				activityURL.setName(urlEntry.getTitle());
				activityURL.setWasExecuted(wasExecuted);
				usedSet.add(activityURL);
			} else {
				EntityRefProvEntryView entityEntry = (EntityRefProvEntryView) used;
				UsedEntity activityEntity = new UsedEntity();
				Reference ref = new Reference();
				ref.setTargetId(entityEntry.getEntryId());
				String version = entityEntry.getEntryVersion();
				// Null version is displayed as "Current"
				if (version != null && !version.equals("Current")) {
					ref.setTargetVersionNumber(Long.valueOf(version));
				}
				activityEntity.setReference(ref);
				activityEntity.setWasExecuted(wasExecuted);
				usedSet.add(activityEntity);
			}
		}
		return usedSet;
	}
	
	/*
	 * Testing purposes only
	 */
	public void setActivty(Activity act) {
		this.activity = act;
	}
}
