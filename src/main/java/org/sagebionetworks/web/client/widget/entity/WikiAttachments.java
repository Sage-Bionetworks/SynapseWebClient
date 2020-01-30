package org.sagebionetworks.web.client.widget.entity;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import java.util.ArrayList;
import java.util.List;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.FileHandleResults;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.FileHandleUtils;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiAttachments implements WikiAttachmentsView.Presenter, SynapseWidgetPresenter {

	private WikiAttachmentsView view;
	private SynapseClientAsync synapseClient;
	private WikiPageKey wikiKey;
	private List<FileHandle> allFileHandles;
	private List<String> toDeleteFileHandles;
	private String selectedFilename;

	@Inject
	public WikiAttachments(WikiAttachmentsView view, SynapseClientAsync synapseClient) {
		this.view = view;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		view.setPresenter(this);
	}

	@Override
	public void configure(final WikiPageKey wikiKey) {
		allFileHandles = null;
		selectedFilename = null;
		toDeleteFileHandles = new ArrayList<String>();
		this.wikiKey = wikiKey;
		synapseClient.getV2WikiAttachmentHandles(wikiKey, new AsyncCallback<FileHandleResults>() {
			@Override
			public void onSuccess(FileHandleResults fileHandleResults) {
				allFileHandles = fileHandleResults.getList();
				updateFileList();
			}

			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		});
	}

	public void updateFileList() {
		List<FileHandle> workingSet = getWorkingSet(allFileHandles);
		view.reset();
		if (workingSet == null || workingSet.isEmpty()) {
			view.showNoAttachmentRow();
			selectedFilename = null;
		} else {
			view.addFileHandles(workingSet);
			if (selectedFilename == null) {
				selectedFilename = workingSet.get(0).getFileName();
			}
		}

		if (selectedFilename != null) {
			// try to select the filename
			view.setSelectedFilename(selectedFilename);
		}
	}

	private List<FileHandle> getWorkingSet(List<FileHandle> allFileHandles) {
		// only include non-preview file handles
		List<FileHandle> workingSet = new ArrayList<FileHandle>();
		for (FileHandle fileHandle : allFileHandles) {
			if (!FileHandleUtils.isPreviewFileHandle(fileHandle)) {
				workingSet.add(fileHandle);
			}
		}
		return workingSet;
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	public boolean isValid() {
		return selectedFilename != null;
	}

	@Override
	public void setSelectedFilename(String fileName) {
		selectedFilename = fileName;
		view.setSelectedFilename(fileName);
	}

	public String getSelectedFilename() {
		return selectedFilename;
	}

	public List<String> getFilesHandlesToDelete() {
		return toDeleteFileHandles;
	}

	@Override
	public void deleteAttachment(final String fileName) {
		if (fileName != null) {
			List<FileHandle> attachmentsToDelete = new ArrayList<FileHandle>();
			// find all file handles with this file name
			for (FileHandle fileHandle : allFileHandles) {
				if (fileHandle.getFileName().equals(fileName)) {
					attachmentsToDelete.add(fileHandle);
					toDeleteFileHandles.add(fileHandle.getId());
				}
			}
			allFileHandles.removeAll(attachmentsToDelete);
			updateFileList();
		} else {
			view.showErrorMessage(DisplayConstants.ERROR_DELETING_ATTACHMENT);
		}
	}
}
