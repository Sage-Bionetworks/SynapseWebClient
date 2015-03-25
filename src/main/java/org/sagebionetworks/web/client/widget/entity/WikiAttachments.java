package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.FileHandleResults;
import org.sagebionetworks.repo.model.file.PreviewFileHandle;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiAttachments implements WikiAttachmentsView.Presenter,
		SynapseWidgetPresenter {

	private WikiAttachmentsView view;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	private WikiPageKey wikiKey;
	private List<FileHandle> allFileHandles;
	private Callback callback;
	
	public interface Callback{
		public void attachmentClicked(String fileName);
		public void attachmentsToDelete(String fileName, List<String> fileHandleIds);
	}
	
	@Inject
	public WikiAttachments(WikiAttachmentsView view, SynapseClientAsync synapseClient,
			NodeModelCreator nodeModelCreator) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		view.setPresenter(this);
	}
	
	@Override
	public void configure(final WikiPageKey wikiKey, WikiPage wikiPage, Callback callback) {
		this.wikiKey = wikiKey;
		if (callback == null) {
			this.callback = new Callback() {
				
				@Override
				public void attachmentsToDelete(String fileName, List<String> fileHandleIds) {
				}
				@Override
				public void attachmentClicked(String fileName) {
				}
			};
		}
		else
			this.callback = callback;	
		synapseClient.getV2WikiAttachmentHandles(wikiKey, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String results) {
				try {
					FileHandleResults fileHandleResults = nodeModelCreator.createJSONEntity(results, FileHandleResults.class);
					allFileHandles = fileHandleResults.getList();
					view.configure(wikiKey, getWorkingSet(allFileHandles));
				} catch (JSONObjectAdapterException e) {
					onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		});
	}
	
	private List<FileHandle> getWorkingSet(List<FileHandle> allFileHandles){
		//only include non-preview file handles
		List<FileHandle> workingSet = new ArrayList<FileHandle>();
		for (FileHandle fileHandle : allFileHandles) {
			if (!(fileHandle instanceof PreviewFileHandle)){
				workingSet.add(fileHandle);
			}
		}
		return workingSet;
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	@Override
	public void deleteAttachment(final String fileName) {
		if(fileName != null) {
			List<FileHandle> attachmentsToDelete = new ArrayList<FileHandle>();
			//find all file handles with this file name
			for (FileHandle fileHandle : allFileHandles) {
				if (fileHandle.getFileName().equals(fileName))
					attachmentsToDelete.add(fileHandle);
			}
			allFileHandles.removeAll(attachmentsToDelete);
			List<String> fileHandleIds = new ArrayList<String>();
			for (FileHandle fileHandle : attachmentsToDelete) {
				fileHandleIds.add(fileHandle.getId());
			}
			view.configure(wikiKey, getWorkingSet(allFileHandles));
			if (fileHandleIds.size() > 0)
				callback.attachmentsToDelete(fileName, fileHandleIds);
		} else {
			view.showErrorMessage(DisplayConstants.ERROR_DELETING_ATTACHMENT);
		}
	}

	@Override
	public void attachmentClicked(final String fileName) {
		if(fileName != null) {
			callback.attachmentClicked(fileName);
		}
	}
	
	public void show() {
		view.show();
	}

}
