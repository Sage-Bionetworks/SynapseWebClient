package org.sagebionetworks.web.client.widget.entity.controller;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.provenance.Activity;
import org.sagebionetworks.repo.model.provenance.Used;
import org.sagebionetworks.repo.model.provenance.UsedEntity;
import org.sagebionetworks.repo.model.provenance.UsedURL;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProvenanceEditorWidget implements ProvenanceEditorWidgetView.Presenter {

	ProvenanceEditorWidgetView view;
	SynapseJavascriptClient jsClient;
	SynapseAlert synAlert;
	PortalGinInjector ginInjector;
	ProvenanceListWidget usedProvenanceList;
	ProvenanceListWidget executedProvenanceList;
	Activity activity;
	EntityFinder entityFinder;
	ProvenanceURLDialogWidget urlDialog;
	EventBus eventBus;
	Entity entity;
	boolean isNewActivity;

	@Inject
	public ProvenanceEditorWidget(ProvenanceEditorWidgetView view, SynapseJavascriptClient jsClient, SynapseAlert synAlert, PortalGinInjector ginInjector, EntityFinder entityFinder, ProvenanceURLDialogWidget urlDialog, EventBus eventBus) {
		this.view = view;
		this.jsClient = jsClient;
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
		entity = entityBundle.getEntity();
		isNewActivity = false;
		jsClient.getActivityForEntityVersion(entity.getId(), null, new AsyncCallback<Activity>() {

			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof NotFoundException) {
					isNewActivity = true;
					activity = new Activity();
					usedProvenanceList.configure(new LinkedList<ProvenanceEntry>());
					executedProvenanceList.configure(new LinkedList<ProvenanceEntry>());
				} else {
					synAlert.handleException(caught);
				}
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
					for (Used provEntry : allProvenance) {
						ProvenanceEntry toAdd;
						if (provEntry instanceof UsedEntity) {
							Reference ref = ((UsedEntity) provEntry).getReference();
							toAdd = ginInjector.getEntityRefEntry();
							String entityId = ref.getTargetId();
							Long version = ref.getTargetVersionNumber();
							String versionString = null;
							if (version != null) {
								versionString = version.toString();
							}
							((EntityRefProvEntryView) toAdd).configure(entityId, versionString);
							toAdd.setAnchorTarget(DisplayUtils.getSynapseHistoryToken(entityId, version));
						} else {
							UsedURL usedURL = (UsedURL) provEntry;
							toAdd = ginInjector.getURLEntry();
							String name = usedURL.getName();
							String url = usedURL.getUrl();
							if (name == null || name.trim().isEmpty()) {
								name = usedURL.getUrl();
							}
							((URLProvEntryView) toAdd).configure(name, url);
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

		if (isNewActivity) {
			// create new activity, and link to entity!
			jsClient.createActivityAndLinkToEntity(activity, entity, new AsyncCallback<Entity>() {
				@Override
				public void onFailure(Throwable caught) {
					synAlert.handleException(caught);
				}

				@Override
				public void onSuccess(Entity result) {
					view.hide();
					eventBus.fireEvent(new EntityUpdatedEvent());
				}
			});
		} else {
			// otherwise, just update the existing activity
			jsClient.updateActivity(activity, new AsyncCallback<Activity>() {
				@Override
				public void onFailure(Throwable caught) {
					synAlert.handleException(caught);
				}

				@Override
				public void onSuccess(Activity result) {
					view.hide();
					eventBus.fireEvent(new EntityUpdatedEvent());
				}
			});
		}
	}

	private Set<Used> provEntryListToUsedSet(List<ProvenanceEntry> entries, boolean wasExecuted) {
		Set<Used> usedSet = new HashSet<Used>();
		for (ProvenanceEntry used : entries) {
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
	public void setActivity(Activity act) {
		this.activity = act;
	}
}
