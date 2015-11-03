package org.sagebionetworks.web.client.widget.upload;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.CallbackP;

import com.google.inject.Inject;

public class FileHandleList implements FileHandleListView.Presenter {
	FileHandleUploadWidget uploadWidget;
	FileHandleListView view;
	PortalGinInjector ginInjector;
	boolean isToolbarVisible, changingSelection;
	CallbackP<String> fileHandleClickedCallback;
	
	List<FileHandleLink> links;
	
	@Inject
	public FileHandleList(
			FileHandleListView view, 
			FileHandleUploadWidget uploadWidget,
			PortalGinInjector ginInjector) {
		super();
		this.view = view;
		this.uploadWidget = uploadWidget;
		this.ginInjector = ginInjector;
		this.view.setPresenter(this);
		view.setUploadWidget(uploadWidget.asWidget());
	}
	
	/**
	 * 
	 * - canDelete if true then show delete buttons.
	 * - canUpload if true then show upload
	 * - List of file handles.  This greatly depends on the context, so for the enhanced profile feature I'll need to find a way to get this list from the file handle ids for both the ACT and owner.
	 * - On file click handler callback.  When file handle is clicked, then this will be called and given the file handle id clicked.
	 */
	public void configure(
			String uploadButtonText,
			boolean canDelete, 
			boolean canUpload, 
			List<FileHandle> fileList, 
			CallbackP<String> fileHandleClickedCallback){
		links = new ArrayList<FileHandleLink>();
		this.isToolbarVisible = canDelete;
		this.fileHandleClickedCallback = fileHandleClickedCallback;
		view.setToolbarVisible(canDelete);
		view.setUploadWidgetVisible(canUpload);
		
		uploadWidget.reset();
		uploadWidget.configure(uploadButtonText, new CallbackP<FileUpload>() {
			@Override
			public void invoke(FileUpload fileUpload) {
				addFileLink(fileUpload);
			}
		});	
		
	};
	
	public void addFileHandles(List<FileHandle> fileList) {
		for (FileHandle fileHandle : fileList) {
			addLink(fileHandle.getId(), fileHandle.getFileName());
		}
	}
	
	public void addFileLink(FileUpload fileUpload) {
		addLink(fileUpload.getFileHandleId(), fileUpload.getFileMeta().getFileName());
	}
	
	private void addLink(String fileHandleId, String fileName) {
		FileHandleLink link = ginInjector.getFileHandleLink();
		link.configure(fileHandleId, fileName, fileHandleClickedCallback);
		links.add(link);
		view.addFileLink(link.asWidget());
	}
	
	private void refreshLinks() {
		view.clearFileLinks();
		for (FileHandleLink fileHandleLink : links) {
			view.addFileLink(fileHandleLink.asWidget());
		}
		
		boolean toolbarVisible = isToolbarVisible && links.size() > 0;
		view.setToolbarVisible(toolbarVisible);
		checkSelectionState();
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
		refreshLinks();
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
}
