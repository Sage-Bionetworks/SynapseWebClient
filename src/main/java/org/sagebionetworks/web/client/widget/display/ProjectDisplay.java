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
	EntityUpdatedHandler entityUpdatedHandler;
	EntityBundle entityBundle;
	CookieProvider cookies;
	SessionStorage storage;
	
	private Entity entity;
	private String userId;
	private String tag;
	private Callback callback;
	private ProjectDisplayBundle result;
	
	public static final String WIKI = "wiki";
	public static final String FILES = "files";
	public static final String TABLES = "tables";
	public static final String CHALLENGE = "challenge";
	public static final String DISCUSSION = "discussion";
	public static final String DOCKER = "docker";
	
	@Inject
	public ProjectDisplay(ProjectDisplayView view,
			SynapseClientAsync synapseClient, 
			SynapseAlert synAlert, 
			CookieProvider cookies,
			SessionStorage storage) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.synAlert = synAlert;
		this.cookies = cookies;
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
		if (view.getWiki() && !result.wikiHasContent()) {
			storage.setItem(tag + WIKI, "true");
		} else if (!view.getWiki() && result.wikiHasContent()) {
			synAlert.showError("Error: Wiki contains content. Content must be deleted to hide tab.");
			return;
		} else { //user doesn't want it, or they do but it already has content and doesn't belong in cache
			storage.removeItem(tag + WIKI);
		}
		if (view.getFiles() && !result.filesHasContent()) {
			storage.setItem(tag + FILES, "true");
		} else if (!view.getFiles() && result.filesHasContent()) {
			synAlert.showError("Error: Files contains content. Content must be deleted to hide tab.");
			return;
		} else { //user doesn't want it, or they do but it already has content and doesn't belong in cache
			storage.removeItem(tag + FILES);
		}
		if (view.getTables() && !result.tablesHasContent()) {
			storage.setItem(tag + TABLES, "true");
		} else if (!view.getTables() && result.tablesHasContent()) {
			synAlert.showError("Error: Tables contains content. Content must be deleted to hide tab.");
			return;
		} else { //user doesn't want it, or they do but it already has content and doesn't belong in cache
			storage.removeItem(tag + TABLES);
		}
		if (view.getChallenge() && !result.challengeHasContent()) {
			storage.setItem(tag + CHALLENGE, "true");
		} else if (!view.getChallenge() && result.challengeHasContent()) {
			synAlert.showError("Error: Challenge contains content. Content must be deleted to hide tab.");
			return;
		} else { //user doesn't want it, or they do but it already has content and doesn't belong in cache
			storage.removeItem(tag + CHALLENGE);
		}
		if (view.getDiscussion() && !result.discussionHasContent()) {
			storage.setItem(tag + DISCUSSION, "true");
		} else if (!view.getDiscussion() && result.discussionHasContent()) {
			synAlert.showError("Error: Discussion contains content. Content must be deleted to hide tab.");
			return;
		} else { //user doesn't want it, or they do but it already has content and doesn't belong in cache
			storage.removeItem(tag + DISCUSSION);
		}
		if (view.getDocker() && !result.dockerHasContent()) {
			storage.setItem(tag + DOCKER, "true");
		} else if (!view.getDocker() && result.dockerHasContent()) {
			synAlert.showError("Error: Docker contains content. Content must be deleted to hide tab.");
			return;
		} else { //user doesn't want it, or they do but it already has content and doesn't belong in cache
			storage.removeItem(tag + DOCKER);
		}
		hide();
	}
	
	@Override
	public void cancel() {
		synAlert.clear();
		hide();
	}

}
