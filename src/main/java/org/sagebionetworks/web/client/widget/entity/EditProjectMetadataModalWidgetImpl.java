package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.web.client.StringUtils;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.EntityTypeUtils;
import org.sagebionetworks.repo.model.Project;

public class EditProjectMetadataModalWidgetImpl implements EditProjectMetadataModalView.Presenter, EditProjectMetadataModalWidget {
	EditProjectMetadataModalView view;
	SynapseClientAsync synapseClient;
	Project project;
	String startingName, startingAlias;
	Callback handler;
	
	@Inject
	public EditProjectMetadataModalWidgetImpl(EditProjectMetadataModalView view,
			SynapseClientAsync synapseClient) {
		super();
		this.view = view;
		this.synapseClient = synapseClient;
		this.view.setPresenter(this);
	}
	
	private void updateProject(final String name, final String alias) {
		view.setLoading(true);
		project.setName(name);
		project.setAlias(alias);
		
		synapseClient.updateEntity(project, new AsyncCallback<Entity>() {
			@Override
			public void onSuccess(Entity result) {
				view.hide();
				handler.invoke();
			}
			@Override
			public void onFailure(Throwable caught) {
				// put the starting values back
				project.setName(startingName);
				project.setAlias(startingAlias);
				view.showError(caught.getMessage());
				view.setLoading(false);
			}
		});
	}

	@Override
	public void onPrimary() {
		String name = StringUtils.trimWithEmptyAsNull(view.getEntityName());
		String alias = view.getAlias();
		if(name == null){
			view.showError(RenameEntityModalWidgetImpl.NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER);
		}else if(this.startingName.equals(name) && this.startingAlias.equals(alias)){
			// just hide the view if the name and alias have not changed.
			view.hide();
		}else{
			updateProject(name, alias);
		}
	}
	

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void configure(Project project, Callback handler) {
		this.handler = handler;
		this.project = project;
		this.startingName = project.getName();
		this.startingAlias = project.getAlias();
		this.view.clear();
		this.view.configure(startingName, startingAlias);
		this.view.show();
	}


}
