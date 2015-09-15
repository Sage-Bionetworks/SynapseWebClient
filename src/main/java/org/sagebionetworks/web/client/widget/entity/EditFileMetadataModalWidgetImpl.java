package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.web.client.StringUtils;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EditFileMetadataModalWidgetImpl implements EditFileMetadataModalView.Presenter, EditFileMetadataModalWidget {
	public static final String FILE_NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER = "File name must include at least one character.";
	public static final String CURRENT_VERSION_ONLY_MESSAGE = "Metadata can only be modified on the most current version of the file.";
	
	EditFileMetadataModalView view;
	SynapseClientAsync synapseClient;
	FileHandle fileHandle;
	FileEntity fileEntity;
	String startingName, startingFileName;
	Callback handler;
	
	@Inject
	public EditFileMetadataModalWidgetImpl(EditFileMetadataModalView view,
			SynapseClientAsync synapseClient) {
		super();
		this.view = view;
		this.synapseClient = synapseClient;
		this.view.setPresenter(this);
	}
	
	
	/**
	 * Update entity with a new name.
	 * @param name
	 */
	private void updateFileEntity(final String name, final String newFileName) {
		view.setLoading(true);
		fileEntity.setName(name);
		final String revertOverrideFilename = fileEntity.getFileNameOverride();
		//only set the fileEntity file name override if it differs from the starting filename
		if (!startingFileName.equals(newFileName)) {
			fileEntity.setFileNameOverride(newFileName);
		}
		synapseClient.updateEntity(fileEntity, new AsyncCallback<Entity>() {
			@Override
			public void onSuccess(Entity result) {
				view.hide();
				handler.invoke();
			}
			@Override
			public void onFailure(Throwable caught) {
				// put the name back.
				fileEntity.setName(startingName);
				fileEntity.setFileNameOverride(revertOverrideFilename);
				view.showError(caught.getMessage());
				view.setLoading(false);
			}
		});
	}

	@Override
	public void onPrimary() {
		String name = StringUtils.trimWithEmptyAsNull(view.getEntityName());
		String fileName = StringUtils.trimWithEmptyAsNull(view.getFileName());
		if(name == null){
			view.showError(RenameEntityModalWidgetImpl.NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER);
		} else if(fileName == null){
			view.showError(FILE_NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER);
		}else if(this.startingName.equals(name) && this.startingFileName.equals(fileName)){
			// just hide the view if nothing has changed
			view.hide();
		}else{
			updateFileEntity(name, fileName);
		}
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void configure(final FileEntity fileEntity, final String fileName, final Callback handler) {
		this.handler = handler;
		this.fileEntity = fileEntity;
		this.startingName = fileEntity.getName();
		this.startingFileName = fileName;
		view.clear();
		view.configure(startingName, startingFileName);
		view.show();
	}

}
