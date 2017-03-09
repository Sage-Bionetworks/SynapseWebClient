package org.sagebionetworks.web.client.widget.display;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cache.SessionStorage;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.ProjectDisplayBundle;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProjectDisplay implements ProjectDisplayView.Presenter {
	ProjectDisplayView view;
	SynapseClientAsync synapseClient;
	SynapseAlert synAlert;
	SessionStorage storage;
	
	private Entity entity;
	private String userId;
	private String tag;
	private Callback callback;
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
			SessionStorage storage) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.synAlert = synAlert;
		this.storage = storage;
		view.setSynAlertWidget(synAlert);
		view.setPresenter(this);
	}

	@Override
	public void configure(Entity entity, String userId, Callback callback) {
		this.entity = entity;
		this.userId = userId;
		this.callback = callback;
		this.tag = this.userId + "_" + this.entity.getId() + "_";
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	public void show() {
		//show loading gif
		synapseClient.getCountsForTabs(entity, new AsyncCallback<ProjectDisplayBundle>() {

			@Override
			public void onFailure(Throwable caught) {
				//hide loading gif
				view.clear();
				view.show();
			}

			@Override
			public void onSuccess(ProjectDisplayBundle result) {
				//hide loading gif
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
		callback.invoke();
	}
	
	@Override
	public void clear() {
		synAlert.clear();
		view.clear();
	}

	@Override
	public void onSave() {
		synAlert.clear();
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
