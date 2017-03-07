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
	private Callback callback;
	
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
		this.callback = callback;
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
			}

			@Override
			public void onSuccess(ProjectDisplayBundle result) {
				//hide loading gif
				boolean wiki = Boolean.parseBoolean(storage.getItem("wiki")) || result.wikiHasContent();
				view.setWiki(wiki);
				boolean files = Boolean.parseBoolean(storage.getItem("files")) || result.filesHasContent();
				view.setFiles(files);
				boolean tables = Boolean.parseBoolean(storage.getItem("tables")) || result.tablesHasContent();
				view.setTables(tables);
				boolean discussion = Boolean.parseBoolean(storage.getItem("discussion")) || result.discussionHasContent();
				view.setDiscussion(discussion);
				boolean docker = Boolean.parseBoolean(storage.getItem("docker")) || result.dockerHasContent();
				view.setDocker(docker);
			}
			
		});
		view.show();
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
		hide();
	}

}
