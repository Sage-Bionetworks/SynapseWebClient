package org.sagebionetworks.web.client.widget.entity.renderer;

import static org.sagebionetworks.web.client.ClientProperties.MB;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PDFPreviewWidget implements IsWidget {
	public static final String PDF_JS_VIEWER_PREFIX = "/pdf.js/web/viewer.html?file=";
	private IFrameView view;
	private SynapseJSNIUtils jsniUtils;
	private GWTWrapper gwt;
	FileHandleAssociation fha;

	public static final double MAX_PDF_FILE_SIZE = 30 * MB;
	public static final int DEFAULT_HEIGHT_PX = 800;
	public static String friendlyMaxPdfFileSize = null;
	FileHandle fileHandle;

	@Inject
	public PDFPreviewWidget(IFrameView view, SynapseJSNIUtils jsniUtils, GWTWrapper gwt) {
		this.view = view;
		this.jsniUtils = jsniUtils;
		this.gwt = gwt;
		view.addAttachHandler(event -> {
			if (event.isAttached()) {
				refreshContent();
			}
		});
		if (friendlyMaxPdfFileSize == null) {
			friendlyMaxPdfFileSize = gwt.getFriendlySize(MAX_PDF_FILE_SIZE, true);
		}
	}

	public void configure(String synapseId, FileHandle fileHandle) {
		this.fileHandle = fileHandle;
		fha = new FileHandleAssociation();
		fha.setAssociateObjectId(synapseId);
		fha.setAssociateObjectType(FileHandleAssociateType.FileEntity);
		fha.setFileHandleId(fileHandle.getId());
		refreshContent();
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	public void refreshContent() {
		if (fileHandle.getContentSize() != null && fileHandle.getContentSize() < MAX_PDF_FILE_SIZE) {
			if (fha != null) {
				String url = jsniUtils.getFileHandleAssociationUrl(fha.getAssociateObjectId(), fha.getAssociateObjectType(), fha.getFileHandleId());
				StringBuilder siteUrl = new StringBuilder();
				siteUrl.append(PDF_JS_VIEWER_PREFIX);
				siteUrl.append(gwt.encodeQueryString(url));
				int height = view.getParentOffsetHeight();
				if (height <= 0) {
					height = DEFAULT_HEIGHT_PX;
				}
				view.configure(siteUrl.toString(), height);

			}
		} else {
			view.showError("The PDF preview was not shown because the file size (" + gwt.getFriendlySize(fileHandle.getContentSize().doubleValue(), true) + ") exceeds the maximum preview size (" + friendlyMaxPdfFileSize + ")");
		}
	}
}
