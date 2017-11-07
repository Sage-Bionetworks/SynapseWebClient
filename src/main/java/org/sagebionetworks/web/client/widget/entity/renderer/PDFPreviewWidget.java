package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.file.FileResult;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.widget.asynch.PresignedURLAsyncHandler;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PDFPreviewWidget implements IsWidget {
	private IFrameView view;
	private PresignedURLAsyncHandler presignedURLAsyncHandler;
	private GWTWrapper gwt;
	FileHandleAssociation fha;
	@Inject
	public PDFPreviewWidget(
			IFrameView view,
			PresignedURLAsyncHandler presignedURLAsyncHandler,
			GWTWrapper gwt) {
		this.view = view;
		this.presignedURLAsyncHandler = presignedURLAsyncHandler;
		this.gwt = gwt;
		view.addAttachHandler(event -> {
			if (event.isAttached()) {
				refreshContent();
			}
		});
	}
	
	public void configure(String synapseId, String fileHandleId) {
		fha = new FileHandleAssociation();
		fha.setAssociateObjectId(synapseId);
		fha.setAssociateObjectType(FileHandleAssociateType.FileEntity);
		fha.setFileHandleId(fileHandleId);
		refreshContent();
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	public void refreshContent() {
		if (fha != null) {
			presignedURLAsyncHandler.getFileResult(fha, new AsyncCallback<FileResult>() {
				@Override
				public void onSuccess(FileResult fileResult) {
					String presignedUrl = fileResult.getPreSignedURL();
					StringBuilder siteUrl = new StringBuilder();
					siteUrl.append("/pdf.js/web/viewer.html?file=");
					siteUrl.append(gwt.encodeQueryString(presignedUrl));
					view.configure(siteUrl.toString(), view.getParentOffsetHeight());
				}
				
				@Override
				public void onFailure(Throwable ex) {
					view.showInvalidSiteUrl(ex.getMessage());
				}
			});
		}
	}
}
