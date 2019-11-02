package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.file.FileResult;
import org.sagebionetworks.web.client.StringUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.asynch.FileHandleAsyncHandler;
import org.sagebionetworks.web.client.widget.table.modal.fileview.TableType;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.table.CellAddress;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileCellRenderer implements TakesAddressCell {

	public static final String FILE_SYNAPSE_ID_UNAVAILABLE = "File Synapse ID is unavailable in the row.";
	public static final String UNABLE_TO_LOAD_FILE_DATA = "Unable to load file data";
	FileCellRendererView view;
	String fileHandleId;
	CellAddress address;
	AuthenticationController authController;
	FileHandleAsyncHandler fileHandleAsynHandler;
	FileHandleAssociation association;

	@Inject
	public FileCellRenderer(FileCellRendererView view, AuthenticationController authController, FileHandleAsyncHandler fileHandleAsynHandler) {
		this.view = view;
		this.authController = authController;
		this.fileHandleAsynHandler = fileHandleAsynHandler;
	}

	@Override
	public Widget asWidget() {
		return this.view.asWidget();
	}

	@Override
	public void setValue(String value) {
		fileHandleId = StringUtils.emptyAsNull(value);
		if (fileHandleId == null) {
			view.setLoadingVisible(false);
		} else {
			view.setLoadingVisible(true);
			association = new FileHandleAssociation();
			association.setFileHandleId(fileHandleId);
			if (TableType.table.equals(address.getTableType())) {
				association.setAssociateObjectType(FileHandleAssociateType.TableEntity);
				association.setAssociateObjectId(address.getTableId());
			} else {
				association.setAssociateObjectType(FileHandleAssociateType.FileEntity);
				if (address.getRowId() != null) {
					association.setAssociateObjectId(address.getRowId().toString());
				} else {
					view.setLoadingVisible(false);
					view.setErrorText(FILE_SYNAPSE_ID_UNAVAILABLE);
					return;
				}
			}
			fileHandleAsynHandler.getFileResult(association, new AsyncCallback<FileResult>() {

				@Override
				public void onSuccess(FileResult result) {
					if (view.isAttached() && result != null) {
						view.setLoadingVisible(false);
						if (result.getFileHandle() != null) {
							view.setAnchor(result.getFileHandle().getFileName(), createAnchorHref());
							Long contentSize = result.getFileHandle().getContentSize();
							if (contentSize != null) {
								view.setTooltip(contentSize);
							}
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
	}

	/**
	 * Create the href using the address of this renderer.
	 * 
	 * @return
	 */
	public String createAnchorHref() {
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
