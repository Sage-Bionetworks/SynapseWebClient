package org.sagebionetworks.web.client.widget.entity.act;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestion;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import org.sagebionetworks.web.client.widget.upload.FileHandleLink;
import org.sagebionetworks.web.client.widget.upload.FileHandleListView;
import org.sagebionetworks.web.client.widget.upload.FileHandleUploadWidget;
import org.sagebionetworks.web.client.widget.upload.FileUpload;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;


public class UserBadgeList implements UserBadgeListView.Presenter, IsWidget {

	UserBadgeListView view;
	PortalGinInjector ginInjector;
	boolean isToolbarVisible, changingSelection; //canDelete
	CallbackP<String> fileHandleClickedCallback;
	Callback selectionChangedCallback;
	List<FileHandleLink> links; //UserBadgeLink?
	SynapseSuggestBox peopleSuggestWidget;	
	
	@Inject
	public UserBadgeList (
			SynapseSuggestBox peopleSuggestBox,
			UserGroupSuggestionProvider provider, 
			UserBadgeListView view, 
			PortalGinInjector ginInjector) {
		super();
		this.view = view;
		this.peopleSuggestWidget = peopleSuggestBox;
		peopleSuggestWidget.setSuggestionProvider(provider);
		this.ginInjector = ginInjector;
		this.view.setPresenter(this);
		this.view.setSelectorWidget(peopleSuggestWidget.asWidget());
		peopleSuggestBox.addItemSelectedHandler(new CallbackP<SynapseSuggestion>() {
			@Override
			public void invoke(SynapseSuggestion suggestion) {
				onUserSelected(suggestion);
			}
		});
		
		selectionChangedCallback = new Callback() {
			@Override
			public void invoke() {
				refreshLinkUI();
			}
		};
		
	}
	
	public void onUserSelected(SynapseSuggestion suggestion) {
		
		view.addUserBadge(suggestion.getReplacementString());
		peopleSuggestWidget.clear();
	}
	
	/**
	 * - canUpload if true then show upload
	 * - On file click handler callback.  When file handle is clicked, then this will be called and given the file handle id clicked.
	 */
	public UserBadgeList configure(
			CallbackP<String> fileHandleClickedCallback){
		links = new ArrayList<FileHandleLink>();
		this.isToolbarVisible = false;
		view.setToolbarVisible(false);
		view.setUploadWidgetVisible(false);
		this.fileHandleClickedCallback = fileHandleClickedCallback;
		return this;
	};
	
	/**
	 * If true then show toolbar with the delete button.
	 * @param canDelete
	 * @return
	 */
	public UserBadgeList setCanDelete(boolean canDelete) {
		this.isToolbarVisible = canDelete;
		view.setToolbarVisible(isToolbarVisible);
		return this;
	}
	
	public void addFileLink(FileUpload fileUpload) {
		addFileLink(fileUpload.getFileHandleId(), fileUpload.getFileMeta().getFileName());
		refreshLinkUI();
	}
	
	public void addFileLink(String fileHandleId, String fileName) {
		FileHandleLink link = ginInjector.getFileHandleLink();
		link.configure(fileHandleId, fileName, fileHandleClickedCallback)
		.setFileSelectCallback(selectionChangedCallback)
		.setSelectVisible(isToolbarVisible);
		links.add(link);
	}
	
	public void refreshLinkUI() {
		view.clearFileLinks();
		for (FileHandleLink fileHandleLink : links) {
			view.addUserBadge(fileHandleLink.getFileHandleId());
		}
		
		boolean toolbarVisible = isToolbarVisible && links.size() > 0;
		view.setToolbarVisible(toolbarVisible);
		if (toolbarVisible) {
			checkSelectionState();	
		}
	}
	
	@Override
	public void deleteSelected() {
		//remove all selected file links
		Iterator<FileHandleLink> it = links.iterator();
		while(it.hasNext()){
			FileHandleLink row = it.next();
			if(row.isSelected()){
				it.remove();
			}
		}
		refreshLinkUI();
	}
	
	/**
	 * Change the selection state of all rows to the passed value.
	 * 
	 * @param select
	 */
	private void changeAllSelection(boolean select){
		try{
			changingSelection = true;
			// Select all
			for (FileHandleLink fileHandleLink : links) {
				fileHandleLink.setSelected(select);
			}
		}finally{
			changingSelection = false;
		}
		checkSelectionState();
	}
	

	/**
	 * The current selection state determines which buttons are enabled.
	 */
	public void checkSelectionState(){
		if(!changingSelection && isToolbarVisible){
			int count = 0;
			for (FileHandleLink link : links) {
				if(link.isSelected()){
					count++;
				}
			}
			view.setCanDelete(count > 0);
		}
	}
	
	@Override
	public void selectAll() {
		changeAllSelection(true);
	}
	
	@Override
	public void selectNone() {
		changeAllSelection(false);
	}
	
	@Override
	public List<String> getFileHandleIds() {
		List<String> fileHandleIds = new ArrayList<String>();
		for (FileHandleLink link : links) {
			fileHandleIds.add(link.getFileHandleId());
		}
		return fileHandleIds;
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
