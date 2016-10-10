package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.file.FileResult;
import org.sagebionetworks.web.client.StringUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.asynch.FileHandleAsyncHandler;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.table.CellAddress;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileCellRendererImpl implements FileCellRenderer {
	
	public static final String UNABLE_TO_LOAD_FILE_DATA = "Unable to load file data";
	FileCellRendererView view;
	String fileHandleId;
	CellAddress address;
	AuthenticationController authController;
	FileHandleAsyncHandler fileHandleAsynHandler;
	FileHandleAssociation association;
	
	@Inject
	public FileCellRendererImpl(FileCellRendererView view, AuthenticationController authController, FileHandleAsyncHandler fileHandleAsynHandler) {
		this.view = view;
		this.authController = authController;
		this.fileHandleAsynHandler = fileHandleAsynHandler;
	}

	@Override
	public Widget asWidget() {
		return this.view.asWidget();
	}

	@Override
	public void setValue(final String value) {
		fileHandleId = StringUtils.trimWithEmptyAsNull(value);
		if(fileHandleId == null){
			view.setLoadingVisible(false);
		}else{
			view.setLoadingVisible(true);
			association = new FileHandleAssociation();
			association.setFileHandleId(fileHandleId);
			if (address.isView()) {
				association.setAssociateObjectType(FileHandleAssociateType.FileEntity);
				association.setAssociateObjectId(address.getRowId().toString());
			} else {
				association.setAssociateObjectType(FileHandleAssociateType.TableEntity);
				association.setAssociateObjectId(address.getTableId());
			}
			fileHandleAsynHandler.getFileHandle(association, new AsyncCallback<FileResult>() {
				
				@Override
				public void onSuccess(FileResult result) {
					if(view.isAttached()){
						view.setLoadingVisible(false);
						view.setAnchor(result.getFileHandle().getFileName(), createAnchorHref());
					}
				}
				
				@Override
				public void onFailure(Throwable caught) {
					if(view.isAttached()){
						view.setLoadingVisible(false);
						view.setErrorText(UNABLE_TO_LOAD_FILE_DATA + ": " + caught.getMessage());
					}
				}
			});
		}
	}
	
	/**
	 * Create the href using the address of this renderer.
	 * @return
	 */
	public String createAnchorHref(){
		StringBuilder builder = new StringBuilder();
		builder.append("/Portal/");
		builder.append(WebConstants.FILE_HANDLE_ASSOCIATION_SERVLET);
		builder.append("?");
		builder.append(WebConstants.ASSOCIATED_OBJECT_ID_PARAM_KEY);
		builder.append("=");
		builder.append(association.getAssociateObjectId());
		builder.append("&");
		builder.append(WebConstants.ASSOCIATED_OBJECT_TYPE_PARAM_KEY);
		builder.append("=");
		builder.append(association.getAssociateObjectType());
		builder.append("&");
		builder.append(WebConstants.FILE_HANDLE_ID_PARAM_KEY);
		builder.append("=");
		builder.append(association.getFileHandleId());
		builder.append("&");
		builder.append(WebConstants.XSRF_TOKEN_KEY);
		builder.append("=");
		builder.append(authController.getCurrentXsrfToken());
		return builder.toString();
	}

	@Override
	public String getValue() {
		return fileHandleId;
	}

	@Override
	public void setCellAddresss(CellAddress address) {
		this.address = address;
	}

}
