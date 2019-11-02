package org.sagebionetworks.web.client.widget.entity;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.file.FileHandleCopyRequest;
import org.sagebionetworks.web.client.StringUtils;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.Callback;
import com.google.common.base.Objects;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EditFileMetadataModalWidgetImpl implements EditFileMetadataModalView.Presenter, EditFileMetadataModalWidget {
	public static final String FILE_NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER = "File name must include at least one character.";
	public static final String CURRENT_VERSION_ONLY_MESSAGE = "Metadata can only be modified on the most current version of the file.";

	EditFileMetadataModalView view;
	SynapseClientAsync synapseClient;
	SynapseJavascriptClient jsClient;
	FileHandle fileHandle;
	FileEntity fileEntity;
	String startingName;
	Callback handler;
	AsyncCallback<Entity> entityUpdatedCallback;

	@Inject
	public EditFileMetadataModalWidgetImpl(EditFileMetadataModalView view, SynapseClientAsync synapseClient, SynapseJavascriptClient jsClient) {
		super();
		this.view = view;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.jsClient = jsClient;
		this.view.setPresenter(this);
		this.entityUpdatedCallback = getEntityUpdatedCallback();
	}

	private AsyncCallback<Entity> getEntityUpdatedCallback() {
		return new AsyncCallback<Entity>() {
			@Override
			public void onSuccess(Entity result) {
				view.hide();
				handler.invoke();
			}

			@Override
			public void onFailure(Throwable caught) {
				fileEntity.setName(startingName);
				view.showError(caught.getMessage());
				view.setLoading(false);
			}
		};
	}

	/**
	 * Update entity with a new name.
	 * 
	 * @param name
	 */
	private void updateFileEntityFileHandle() {
		view.setLoading(true);
		fileEntity.setName(getEntityNameFromView());
		FileHandleCopyRequest copyRequest = new FileHandleCopyRequest();
		copyRequest.setNewContentType(getFileContentTypeFromView());
		copyRequest.setNewFileName(getFileNameFromView());
		FileHandleAssociation fha = new FileHandleAssociation();
		fha.setAssociateObjectId(fileEntity.getId());
		fha.setAssociateObjectType(FileHandleAssociateType.FileEntity);
		fha.setFileHandleId(fileHandle.getId());
		copyRequest.setOriginalFile(fha);
		fileEntity.setFileNameOverride(null);
		synapseClient.updateFileEntity(fileEntity, copyRequest, entityUpdatedCallback);
	}

	/**
	 * Update entity with a new name.
	 * 
	 * @param name
	 */
	private void updateFileEntity() {
		view.setLoading(true);
		fileEntity.setName(getEntityNameFromView());
		jsClient.updateEntity(fileEntity, null, null, entityUpdatedCallback);
	}

	@Override
	public void onPrimary() {
		if (getEntityNameFromView() == null) {
			view.showError(RenameEntityModalWidgetImpl.NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER);
		} else if (getFileNameFromView() == null) {
			view.showError(FILE_NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER);
		} else if (isFileHandleChange()) {
			updateFileEntityFileHandle();
		} else if (isEntityChange()) {
			updateFileEntity();
		} else {
			// just hide the view if nothing has changed
			view.hide();
		}
	}

	private String getEntityNameFromView() {
		return StringUtils.emptyAsNull(view.getEntityName());
	}

	private String getFileNameFromView() {
		return StringUtils.emptyAsNull(view.getFileName());
	}

	private String getFileContentTypeFromView() {
		return StringUtils.emptyAsNull(view.getContentType());
	}

	private boolean isEntityChange() {
		return !this.startingName.equals(getEntityNameFromView());
	}

	private boolean isFileHandleChange() {
		return !this.fileHandle.getFileName().equals(getFileNameFromView()) || !Objects.equal(this.fileHandle.getContentType(), getFileContentTypeFromView());
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void configure(final FileEntity fileEntity, FileHandle fileHandle, final Callback handler) {
		this.handler = handler;
		this.fileEntity = fileEntity;
		this.fileHandle = fileHandle;
		this.startingName = fileEntity.getName();
		view.clear();
		view.configure(startingName, fileHandle.getFileName(), fileHandle.getContentType());
		view.show();
	}

}
