package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.file.FileResult;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.widget.asynch.PresignedURLAsyncHandler;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PDFPreviewWidget implements IsWidget {
	private IFrameView view;
	private PresignedURLAsyncHandler presignedURLAsyncHandler;
	private GWTWrapper gwt;
	
	@Inject
	public PDFPreviewWidget(
			IFrameView view,
			PresignedURLAsyncHandler presignedURLAsyncHandler,
			GWTWrapper gwt) {
		this.view = view;
		this.presignedURLAsyncHandler = presignedURLAsyncHandler;
		this.gwt = gwt;
	}
	
	public void configure(String synapseId, String fileHandleId) {
		FileHandleAssociation fha = new FileHandleAssociation();
		fha.setAssociateObjectId(synapseId);
		fha.setAssociateObjectType(FileHandleAssociateType.FileEntity);
		fha.setFileHandleId(fileHandleId);
		presignedURLAsyncHandler.getFileResult(fha, new AsyncCallback<FileResult>() {
			@Override
			public void onSuccess(FileResult fileResult) {
				String presignedUrl = fileResult.getPreSignedURL();
				StringBuilder siteUrl = new StringBuilder();
				siteUrl.append("/pdf.js/web/viewer.html?file=");
				siteUrl.append(gwt.encodeQueryString(presignedUrl));
				view.configure(siteUrl.toString(), 3000);
			}
			
			@Override
			public void onFailure(Throwable ex) {
				view.showInvalidSiteUrl(ex.getMessage());
			}
		});
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
