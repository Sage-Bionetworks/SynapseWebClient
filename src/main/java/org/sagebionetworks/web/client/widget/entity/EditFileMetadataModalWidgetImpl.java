package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.web.client.StringUtils;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EditFileMetadataModalWidgetImpl implements EditFileMetadataModalView.Presenter, EditFileMetadataModalWidget {
	public static final String FILE_NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER = "File name must include at least one character.";
	public static final String FILE_CONTENT_TYPE_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER = "File content type must include at least one character.";
	public static final String CURRENT_VERSION_ONLY_MESSAGE = "Metadata can only be modified on the most current version of the file.";
	
	EditFileMetadataModalView view;
	SynapseClientAsync synapseClient;
	FileHandle fileHandle;
	FileEntity fileEntity;
	String startingName, startingFileName, startingContentType;
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
	private void updateFileEntity(final String name, final String fileName, final String contentType) {
		view.setLoading(true);
		fileEntity.setName(name);
		synapseClient.updateFileEntity(fileEntity, fileHandle.getId(), fileName, contentType, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				view.hide();
				handler.invoke();
			}
			@Override
			public void onFailure(Throwable caught) {
				// put the name back.
				fileEntity.setName(startingName);
				view.showError(caught.getMessage());
				view.setLoading(false);
			}
		});
	}

	@Override
	public void onPrimary() {
		String name = StringUtils.trimWithEmptyAsNull(view.getEntityName());
		String fileName = StringUtils.trimWithEmptyAsNull(view.getFileName());
		String contentType = StringUtils.trimWithEmptyAsNull(view.getContentType());
		if(name == null){
			view.showError(RenameEntityModalWidgetImpl.NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER);
		} else if(fileName == null){
			view.showError(FILE_NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER);
		} else if(contentType == null){
			view.showError(FILE_CONTENT_TYPE_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER);
		}else if(this.startingName.equals(name) && this.startingFileName.equals(fileName) && this.startingContentType.equals(contentType)){
			// just hide the view if nothing has changed
			view.hide();
		}else{
			updateFileEntity(name, fileName, contentType);
		}
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void configure(FileEntity fileEntity, List<FileHandle> fileHandles,
			Callback handler) {
		//can only be run on the current version.
		if (fileEntity.getVersionNumber() != null) {
			view.showErrorPopup(CURRENT_VERSION_ONLY_MESSAGE);
		} else {
			this.handler = handler;
			this.fileEntity = fileEntity;
			//find the non-preview file handle
			for (FileHandle fileHandle : fileHandles) {
				if (fileHandle.getId().equals(fileEntity.getDataFileHandleId())) {
					this.fileHandle = fileHandle;
					break;
				}
			}
			this.startingName = fileEntity.getName();
			this.startingFileName = fileHandle.getFileName();
			this.startingContentType = fileHandle.getContentType();
			this.view.clear();
			this.view.configure(startingName, startingFileName, startingContentType);
			this.view.show();	
		}
	}

}
