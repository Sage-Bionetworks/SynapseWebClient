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
				boolean wiki = Boolean.parseBoolean(storage.getItem(tag + "wiki")) || result.wikiHasContent();
				view.setWiki(wiki);
				boolean files = Boolean.parseBoolean(storage.getItem(tag + "files")) || result.filesHasContent();
				view.setFiles(files);
				boolean tables = Boolean.parseBoolean(storage.getItem(tag + "tables")) || result.tablesHasContent();
				view.setTables(tables);
				boolean discussion = Boolean.parseBoolean(storage.getItem(tag + "discussion")) || result.discussionHasContent();
				view.setDiscussion(discussion);
				boolean docker = Boolean.parseBoolean(storage.getItem(tag + "docker")) || result.dockerHasContent();
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
			storage.setItem(tag + "wiki", "true");
		} else if (!view.getWiki() && result.wikiHasContent()) {
			synAlert.showError("Error: Wiki contains content. Content must be deleted to hide tab.");
			return;
		} else { //user doesn't want it, or they do but it already has content and doesn't belong in cache
			storage.removeItem(tag + "wiki");
		}
		if (view.getFiles() && !result.filesHasContent()) {
			storage.setItem(tag + "files", "true");
		} else if (!view.getFiles() && result.filesHasContent()) {
			synAlert.showError("Error: Files contains content. Content must be deleted to hide tab.");
			return;
		} else { //user doesn't want it, or they do but it already has content and doesn't belong in cache
			storage.removeItem(tag + "files");
		}
		if (view.getTables() && !result.tablesHasContent()) {
			storage.setItem(tag + "tables", "true");
		} else if (!view.getTables() && result.tablesHasContent()) {
			synAlert.showError("Error: Tables contains content. Content must be deleted to hide tab.");
			return;
		} else { //user doesn't want it, or they do but it already has content and doesn't belong in cache
			storage.removeItem(tag + "tables");
		}
		if (view.getChallenge() && !result.challengeHasContent()) {
			storage.setItem(tag + "challenge", "true");
		} else if (!view.getChallenge() && result.challengeHasContent()) {
			synAlert.showError("Error: Challenge contains content. Content must be deleted to hide tab.");
			return;
		} else { //user doesn't want it, or they do but it already has content and doesn't belong in cache
			storage.removeItem(tag + "challenge");
		}
		if (view.getDiscussion() && !result.discussionHasContent()) {
			storage.setItem(tag + "discussion", "true");
		} else if (!view.getDiscussion() && result.discussionHasContent()) {
			synAlert.showError("Error: Discussion contains content. Content must be deleted to hide tab.");
			return;
		} else { //user doesn't want it, or they do but it already has content and doesn't belong in cache
			storage.removeItem(tag + "discussion");
		}
		if (view.getDocker() && !result.dockerHasContent()) {
			storage.setItem(tag + "docker", "true");
		} else if (!view.getDocker() && result.dockerHasContent()) {
			synAlert.showError("Error: Docker contains content. Content must be deleted to hide tab.");
			return;
		} else { //user doesn't want it, or they do but it already has content and doesn't belong in cache
			storage.removeItem(tag + "docker");
		}
		hide();
	}
	
	@Override
	public void cancel() {
		synAlert.clear();
		hide();
	}

}
