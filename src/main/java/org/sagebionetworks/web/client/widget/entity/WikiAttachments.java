package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.FileHandleResults;
import org.sagebionetworks.repo.model.file.PreviewFileHandle;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
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
	private GlobalApplicationState globalApplicationState;
	private AuthenticationController authenticationController;
	private NodeModelCreator nodeModelCreator;
	private JSONObjectAdapter jsonObjectAdapter;
	private WikiPage wikiPage;
	private WikiPageKey wikiKey;
	private List<FileHandle> allFileHandles;
	private Callback callback;
	
	public interface Callback{
		public void attachmentClicked(String fileName);
		public void attachmentDeleted(String fileName);
	}
	
	@Inject
	public WikiAttachments(WikiAttachmentsView view, SynapseClientAsync synapseClient,
			GlobalApplicationState globalApplicationState,
			AuthenticationController authenticationController,
			JSONObjectAdapter jsonObjectAdapter,NodeModelCreator nodeModelCreator) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.nodeModelCreator = nodeModelCreator;
		view.setPresenter(this);
	}
	
	@Override
	public void configure(final WikiPageKey wikiKey, WikiPage wikiPage, Callback callback) {
		this.wikiPage = wikiPage;
		this.wikiKey = wikiKey;
		if (callback == null) {
			this.callback = new Callback() {
				
				@Override
				public void attachmentDeleted(String fileName) {
				}
				@Override
				public void attachmentClicked(String fileName) {
				}
			};
		}
		else
			this.callback = callback;	
		synapseClient.getWikiAttachmentHandles(wikiKey, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String results) {
				try {
					FileHandleResults fileHandleResults = nodeModelCreator.createJSONEntity(results, FileHandleResults.class);
					allFileHandles = fileHandleResults.getList();
					//only include non-preview file handles
					List<FileHandle> workingSet = new ArrayList<FileHandle>();
					for (FileHandle fileHandle : fileHandleResults.getList()) {
						if (!(fileHandle instanceof PreviewFileHandle)){
							workingSet.add(fileHandle);
						}
					}
					
					view.configure(wikiKey, workingSet);
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
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	@Override
	public void deleteAttachment(final String fileName) {
		if(fileName != null) {
			List<String> attachmentFileHandleIds = wikiPage.getAttachmentFileHandleIds();
			//find all file handles with this file name
			for (FileHandle fileHandle : allFileHandles) {
				if (fileHandle.getFileName().equals(fileName))
					attachmentFileHandleIds.remove(fileHandle.getId());
			}
			JSONObjectAdapter adapter = jsonObjectAdapter.createNew();
			try {
				wikiPage.writeToJSONObject(adapter);
			} catch (JSONObjectAdapterException e) {
				view.showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);
				return;
			}

			// update wiki page minus attachment
			synapseClient.updateWikiPage(wikiKey.getOwnerObjectId(), wikiKey.getOwnerObjectType(), adapter.toJSONString(), new AsyncCallback<String>() {

				@Override
				public void onSuccess(String result) {
					try{
						WikiPage updatedPage = nodeModelCreator.createJSONEntity(result, WikiPage.class);
						configure(wikiKey, updatedPage, callback);
						callback.attachmentDeleted(fileName);
					} catch (JSONObjectAdapterException e) {
						view.showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);
						return;
					}
				}
				@Override
				public void onFailure(Throwable caught) {
					if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.getLoggedInUser())) {
						view.showErrorMessage(DisplayConstants.ERROR_DELETING_ATTACHMENT);
					}
				}
			});
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
	
	@Override
	public void setAttachmentColumnWidth(int width) {
		view.setAttachmentColumnWidth(width);
	}

}
