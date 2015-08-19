package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.web.client.StringUtils;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.shared.PaginatedResults;

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
	public void configure(final FileEntity fileEntity, final List<FileHandle> fileHandles, final Callback handler) {
		this.handler = handler;
		this.fileEntity = fileEntity;
		this.startingName = fileEntity.getName();
		
		//can only be run on the current version.
		//get the current version
		synapseClient.getEntityVersions(fileEntity.getId(), 1, 1, new AsyncCallback<PaginatedResults<VersionInfo>>() {
			@Override
			public void onSuccess(PaginatedResults<VersionInfo> result) {
				if (fileEntity.getVersionNumber() != result.getResults().get(0).getVersionNumber()) {
					view.showErrorPopup(CURRENT_VERSION_ONLY_MESSAGE);
				} else {
					//find the non-preview file handle
					for (FileHandle handle : fileHandles) {
						if (handle.getId().equals(fileEntity.getDataFileHandleId())) {
							fileHandle = handle;
							break;
						}
					}
					startingFileName = fileHandle.getFileName();
					startingContentType = fileHandle.getContentType();
					
					view.clear();
					view.configure(startingName, startingFileName, startingContentType);
					view.show();	
				}
			};
			
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorPopup(caught.getMessage());
			}
		});
		
	}

}
