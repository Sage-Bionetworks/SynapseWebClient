package org.sagebionetworks.web.client.widget;

import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.file.FileResult;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.asynch.FileHandleAsyncHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileHandleWidget implements IsWidget {

	public static final String UNABLE_TO_LOAD_FILE_DATA = "Unable to load file data";
	FileHandleWidgetView view;
	AuthenticationController authController;
	FileHandleAsyncHandler fileHandleAsynHandler;
	SynapseJSNIUtils jsniUtils;
	String fileHandleId;

	@Inject
	public FileHandleWidget(FileHandleWidgetView view, AuthenticationController authController, FileHandleAsyncHandler fileHandleAsynHandler, SynapseJSNIUtils jsniUtils) {
		this.view = view;
		this.authController = authController;
		this.fileHandleAsynHandler = fileHandleAsynHandler;
		this.jsniUtils = jsniUtils;
	}

	@Override
	public Widget asWidget() {
		return this.view.asWidget();
	}

	public void configure(final FileHandleAssociation fha) {
		fileHandleId = fha.getFileHandleId();
		view.setLoadingVisible(true);
		fileHandleAsynHandler.getFileResult(fha, new AsyncCallback<FileResult>() {
			@Override
			public void onSuccess(FileResult result) {
				if (view.isAttached() && result != null) {
					view.setLoadingVisible(false);
					if (result.getFileHandle() != null) {
						view.setAnchor(result.getFileHandle().getFileName(), createAnchorHref(fha));
					} else if (result.getFailureCode() != null) {
						// failed
						view.setErrorText(UNABLE_TO_LOAD_FILE_DATA + ": " + result.getFailureCode().toString());
					}
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				if (view.isAttached()) {
					view.setLoadingVisible(false);
					view.setErrorText(UNABLE_TO_LOAD_FILE_DATA + ": " + caught.getMessage());
				}
			}
		});
	}

	public String createAnchorHref(FileHandleAssociation fha) {
		return jsniUtils.getFileHandleAssociationUrl(fha.getAssociateObjectId(), fha.getAssociateObjectType(), fha.getFileHandleId());
	}

	public void configure(String fileName, String rawFileHandleId) {
		fileHandleId = rawFileHandleId;
		view.setLoadingVisible(false);
		view.setAnchor(fileName, createAnchorHref(rawFileHandleId));
	}

	public String createAnchorHref(String rawFileHandleId) {
		return jsniUtils.getBaseFileHandleUrl() + "?rawFileHandleId=" + rawFileHandleId;
	}

	public String getFileHandleId() {
		return fileHandleId;
	}

	public void setVisible(boolean visible) {
		view.setVisible(visible);
	}

}
