package org.sagebionetworks.web.client.widget.entity;

import java.util.HashSet;
import java.util.List;

import org.gwtbootstrap3.extras.bootbox.client.callback.ConfirmCallback;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiMarkdownEditor implements IsWidget, WikiMarkdownEditorView.Presenter{

	MarkdownEditorWidget editor;
	private WikiPage currentPage;
	private WikiPageKey wikiKey;
	private CallbackP<WikiPage> wikiPageUpdatedHandler;
	private SynapseClientAsync synapseClient;
	WikiMarkdownEditorView view;
	GlobalApplicationState globalApplicationState;
	private MarkdownWidget markdownPreview;
	
	@Inject
	public WikiMarkdownEditor(
			WikiMarkdownEditorView view,
			MarkdownEditorWidget editor,
			SynapseClientAsync synapseClient,
			GlobalApplicationState globalApplicationState,
			MarkdownWidget markdownPreview) {
		this.view = view;
		this.editor = editor;
		this.synapseClient = synapseClient;
		this.globalApplicationState = globalApplicationState;
		this.markdownPreview = markdownPreview;
		view.setPresenter(this);
		view.setMarkdownPreviewWidget(markdownPreview.asWidget());
		view.setMarkdownEditorWidget(editor.asWidget());
		editor.setFilesAddedCallback(new CallbackP<List<String>>() {
			@Override
			public void invoke(List<String> fileHandleIds) {
				filesAdded(fileHandleIds);
			}
		});
		editor.setFilesRemovedCallback(new CallbackP<List<String>>() {
			@Override
			public void invoke(List<String> fileHandleIds) {
				filesRemoved(fileHandleIds);
			}
		});
	}
	
	public void filesAdded(List<String> fileHandleIds) {
		HashSet<String> fileHandleIdsSet = new HashSet<String>();
        fileHandleIdsSet.addAll(currentPage.getAttachmentFileHandleIds());
        fileHandleIdsSet.addAll(fileHandleIds);
        currentPage.getAttachmentFileHandleIds().clear();
        currentPage.getAttachmentFileHandleIds().addAll(fileHandleIdsSet);
	}
	
	public void filesRemoved(List<String> fileHandleIds) {
		currentPage.getAttachmentFileHandleIds().removeAll(fileHandleIds);
	}
	
	/**
	 * 
	 * @param ownerId
	 * @param ownerType
	 * @param markdownTextArea
	 * @param formPanel
	 * @param finishedUploadingCallback
	 * @param closeHandler if no save handler is specified, then a Save button is not shown.  If it is specified, then Save is shown and saveClicked is called when that button is clicked.
	 */
	public void configure(final WikiPageKey wikiKey, CallbackP<WikiPage> wikiPageUpdatedHandler) {
		this.wikiPageUpdatedHandler = wikiPageUpdatedHandler;
		this.wikiKey = wikiKey;
		editor.setWikiKey(wikiKey);
		initWikiPage();
	}
	
	public void initWikiPage() {
		//get the wiki page
		synapseClient.getV2WikiPageAsV1(wikiKey, new AsyncCallback<WikiPage>() {
			@Override
			public void onSuccess(WikiPage result) {
				try {
					configure(result);
				} catch (Exception e) {
					onFailure(e);
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				//if it is because of a missing root (and we have edit permission), then the pages browser should have a Create Wiki button
				if (caught instanceof NotFoundException) {
					//create a wiki page and configure with that
					createNewWiki();
				} else {
					view.showErrorMessage(DisplayConstants.ERROR_LOADING_WIKI_FAILED+caught.getMessage());
				}
			}
		});
	}
	
	public void createNewWiki() {
		WikiPage page = new WikiPage();
		synapseClient.createV2WikiPageWithV1(wikiKey.getOwnerObjectId(), wikiKey.getOwnerObjectType(), page, new AsyncCallback<WikiPage>(){
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}

			@Override
			public void onSuccess(WikiPage result) {
				configure(result);
			}});
	}
	
	public void configure(WikiPage page) {
		currentPage = page;
		view.clear();
		view.setSaving(false);
		editor.configure(currentPage.getMarkdown());
		view.setTitleEditorVisible(currentPage.getParentWikiId() != null);
		view.setTitle(currentPage.getTitle());
		globalApplicationState.setIsEditing(true);
		view.showEditorModal();
	}

	public void saveClicked() {
		view.setSaving(true);
		currentPage.setTitle(view.getTitle());
		currentPage.setMarkdown(editor.getMarkdown());
		synapseClient.updateV2WikiPageWithV1(wikiKey.getOwnerObjectId(), wikiKey.getOwnerObjectType(), currentPage, new AsyncCallback<WikiPage>() {
			@Override
			public void onSuccess(WikiPage result) {
				//we have successfully saved, so we are no longer editing
				globalApplicationState.setIsEditing(false);
				view.hideEditorModal();
				if (wikiPageUpdatedHandler != null)
					wikiPageUpdatedHandler.invoke(result);
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(DisplayConstants.ERROR_SAVING_WIKI + caught.getMessage());
			}
		});
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	public WikiPage getWikiPage() {
		return currentPage;
	}
	
	public void previewClicked() {
	    //get the html for the markdown
		markdownPreview.configure(editor.getMarkdown(), wikiKey, null);
		view.showPreviewModal();
	}
	
	public void cancelClicked() {
		globalApplicationState.setIsEditing(false);
		view.hideEditorModal();
		//TODO: update should not be necessary, but widget loading is based on div ids that are overloaded when the formatting guide is initialized
		if (wikiPageUpdatedHandler != null)
			wikiPageUpdatedHandler.invoke(currentPage);
	}

	public void hideEditorModal() {
		view.hideEditorModal();
	}
	
	@Override
	public void deleteClicked() {
		view.confirm(DisplayConstants.PROMPT_SURE_DELETE + " Page and Subpages?", new ConfirmCallback() {
			@Override
			public void callback(boolean isConfirmed) {
				if (isConfirmed)
					deleteConfirmed();
			}
		});
	}
	
	public void deleteConfirmed() {
		synapseClient.deleteV2WikiPage(wikiKey, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				globalApplicationState.setIsEditing(false);
				view.hideEditorModal();
				globalApplicationState.getPlaceChanger().goTo(new Synapse(wikiKey.getOwnerObjectId()));
			}
			
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		});	
	}
	
}
