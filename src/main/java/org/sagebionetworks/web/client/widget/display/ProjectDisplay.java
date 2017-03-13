package org.sagebionetworks.web.client.widget.display;

import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cache.SessionStorage;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.ProjectDisplayBundle;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProjectDisplay implements ProjectDisplayView.Presenter, IsWidget {
	ProjectDisplayView view;
	SynapseClientAsync synapseClient;
	SynapseAlert synAlert;
	SessionStorage storage;
	GlobalApplicationState globalAppState;
	
	private String projectId;
	private String userId;
	private String tag;
	private ProjectDisplayBundle result;
	
	public static final String WIKI = "Wiki";
	public static final String FILES = "Files";
	public static final String TABLES = "Tables";
	public static final String CHALLENGE = "Challenge";
	public static final String DISCUSSION = "Discussion";
	public static final String DOCKER = "Docker";
	
	@Inject
	public ProjectDisplay(ProjectDisplayView view,
			SynapseClientAsync synapseClient, 
			SynapseAlert synAlert,
			SessionStorage storage, 
			GlobalApplicationState globalAppState) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.synAlert = synAlert;
		this.storage = storage;
		this.globalAppState = globalAppState;
		view.setSynAlertWidget(synAlert);
		view.setPresenter(this);
	}

	public void configure(String projectId, String userId) {
		this.projectId = projectId;
		this.userId = userId;
		this.tag = this.userId + "_" + this.projectId + "_";
	}
	
	public Widget asWidget() {
		return view.asWidget();
	}
	
	public void show() {
		synapseClient.getCountsForTabs(projectId, new AsyncCallback<ProjectDisplayBundle>() {
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}

			@Override
			public void onSuccess(ProjectDisplayBundle result) {
				ProjectDisplay.this.result = result;
				boolean wiki = Boolean.parseBoolean(storage.getItem(tag + WIKI)) || result.wikiHasContent();
				view.setWiki(wiki);
				boolean files = Boolean.parseBoolean(storage.getItem(tag + FILES)) || result.filesHasContent();
				view.setFiles(files);
				boolean tables = Boolean.parseBoolean(storage.getItem(tag + TABLES)) || result.tablesHasContent();
				view.setTables(tables);
				boolean challenge = Boolean.parseBoolean(storage.getItem(tag + CHALLENGE)) || result.challengeHasContent();
				view.setChallenge(challenge);
				boolean discussion = Boolean.parseBoolean(storage.getItem(tag + DISCUSSION)) || result.discussionHasContent();
				view.setDiscussion(discussion);
				boolean docker = Boolean.parseBoolean(storage.getItem(tag + DOCKER)) || result.dockerHasContent();
				view.setDocker(docker);
				view.show();
			}
			
		});
		
	}
	
	public void hide() {
		view.hide();
		globalAppState.getPlaceChanger().goTo(new Synapse(projectId));
	}
	
	public void clear() {
		synAlert.clear();
		view.clear();
	}

	@Override
	public void onSave() {
		synAlert.clear();
		
		//validate that at least one is selected
		if (! (view.getWiki() || view.getFiles() || view.getTables() || view.getChallenge() || view.getDiscussion() || view.getDocker())) {
			synAlert.showError("Please select at least one feature.");
			return;
		}
		
		try {
			updateCache(view.getWiki(), result.wikiHasContent(), WIKI);
			updateCache(view.getFiles(), result.filesHasContent(), FILES);
			updateCache(view.getTables(), result.tablesHasContent(), TABLES);
			updateCache(view.getChallenge(), result.challengeHasContent(), CHALLENGE);
			updateCache(view.getDiscussion(), result.discussionHasContent(), DISCUSSION);
			updateCache(view.getDocker(), result.dockerHasContent(), DOCKER);
		} catch (IllegalArgumentException e) {
			synAlert.showError(e.getMessage());
			return;
		}
		hide();
	}
	
	private void updateCache(boolean isChecked, boolean hasContent, String key) {
		if (isChecked && !hasContent) {
			storage.setItem(tag + key, "true");
		} else if (!isChecked && hasContent) {
			throw new IllegalArgumentException(key + " tab contains content. Content must be deleted to hide tab.");
		} else { //user doesn't want it, or they do but it already has content and doesn't belong in cache
			storage.removeItem(tag + key);
		}
	}
	
	@Override
	public void cancel() {
		synAlert.clear();
		hide();
	}

}
