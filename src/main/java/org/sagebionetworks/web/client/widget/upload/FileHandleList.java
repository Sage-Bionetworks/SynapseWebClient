package org.sagebionetworks.web.client.widget.upload;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.CheckBoxState;
import org.sagebionetworks.web.shared.WebConstants;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileHandleList implements FileHandleListView.Presenter, IsWidget {
	FileHandleUploadWidget uploadWidget;
	FileHandleListView view;
	PortalGinInjector ginInjector;
	boolean isToolbarVisible, changingSelection;
	Callback selectionChangedCallback;
	CallbackP<FileUpload> fileUploadedCallback;
	List<FileHandleLink> links;

	@Inject
	public FileHandleList(FileHandleListView view, FileHandleUploadWidget uploadWidget, PortalGinInjector ginInjector) {
		super();
		this.view = view;
		this.uploadWidget = uploadWidget;
		this.ginInjector = ginInjector;
		this.view.setPresenter(this);
		view.setUploadWidget(uploadWidget.asWidget());

		selectionChangedCallback = new Callback() {
			@Override
			public void invoke() {
				refreshLinkUI();
			}
		};

		fileUploadedCallback = new CallbackP<FileUpload>() {
			@Override
			public void invoke(FileUpload fileUpload) {
				addFileLink(fileUpload);
			}
		};
	}

	/**
	 * - canUpload if true then show upload
	 */
	public FileHandleList configure() {
		links = new ArrayList<FileHandleLink>();
		this.isToolbarVisible = false;
		view.setToolbarVisible(false);
		view.setUploadWidgetVisible(false);
		uploadWidget.reset();
		uploadWidget.configure(WebConstants.DEFAULT_FILE_HANDLE_WIDGET_TEXT, fileUploadedCallback);
		uploadWidget.allowMultipleFileUpload(true);
		return this;
	};

	public FileHandleList setUploadButtonText(String uploadButtonText) {
		uploadWidget.configure(uploadButtonText, fileUploadedCallback);
		return this;
	}

	public FileHandleList setCanUpload(boolean canUpload) {
		view.setUploadWidgetVisible(canUpload);
		return this;
	}

	/**
	 * If true then show toolbar with the delete button.
	 * 
	 * @param canDelete
	 * @return
	 */
	public FileHandleList setCanDelete(boolean canDelete) {
		this.isToolbarVisible = canDelete;
		view.setToolbarVisible(isToolbarVisible);
		return this;
	}

	public void addFileLink(FileUpload fileUpload) {
		addFileLink(fileUpload.getFileMeta().getFileName(), fileUpload.getFileHandleId());
		refreshLinkUI();
	}

	public void addFileLink(String fileName, String fileHandleId) {
		createNewLink().configure(fileName, fileHandleId);
		refreshLinkUI();
	}

	public void addFileLink(FileHandleAssociation fha) {
		createNewLink().configure(fha);
		refreshLinkUI();
	}

	private FileHandleLink createNewLink() {
		FileHandleLink link = ginInjector.getFileHandleLink();
		link.setFileSelectCallback(selectionChangedCallback).setSelectVisible(isToolbarVisible);
		links.add(link);
		return link;
	}

	public void refreshLinkUI() {
		view.clearFileLinks();
		for (FileHandleLink fileHandleLink : links) {
			view.addFileLink(fileHandleLink.asWidget());
		}

		boolean toolbarVisible = isToolbarVisible && links.size() > 0;
		view.setToolbarVisible(toolbarVisible);
		if (toolbarVisible) {
			checkSelectionState();
		}
	}

	@Override
	public void deleteSelected() {
		// remove all selected file links
		Iterator<FileHandleLink> it = links.iterator();
		while (it.hasNext()) {
			FileHandleLink row = it.next();
			if (row.isSelected()) {
				it.remove();
			}
		}
		refreshLinkUI();
		uploadWidget.reset();
	}

	/**
	 * Change the selection state of all rows to the passed value.
	 * 
	 * @param select
	 */
	private void changeAllSelection(boolean select) {
		try {
			changingSelection = true;
			// Select all
			for (FileHandleLink fileHandleLink : links) {
				fileHandleLink.setSelected(select);
			}
		} finally {
			changingSelection = false;
		}
		checkSelectionState();
	}


	/**
	 * The current selection state determines which buttons are enabled.
	 */
	public void checkSelectionState() {
		if (!changingSelection && isToolbarVisible) {
			int count = 0;
			for (FileHandleLink link : links) {
				if (link.isSelected()) {
					count++;
				}
			}
			view.setCanDelete(count > 0);
			CheckBoxState state = CheckBoxState.getStateFromCount(count, links.size());
			view.setSelectionState(state);
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

	public void clear() {
		selectAll();
		deleteSelected();
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
