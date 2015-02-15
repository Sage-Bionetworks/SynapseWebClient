package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.web.client.StringUtils;
import org.sagebionetworks.web.client.widget.asynch.AsynchTableFileHandleProvider;
import org.sagebionetworks.web.client.widget.asynch.TableFileHandleRequest;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.table.CellAddress;

import com.google.gwt.core.client.Callback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileCellRendererImpl implements FileCellRenderer {
	
	public static final String UNABLE_TO_LOAD_FILE_DATA = "Unable to load file data";
	FileCellRendererView view;
	String fileHandleId;
	CellAddress address;
	AsynchTableFileHandleProvider fileHandleProvider;

	@Inject
	public FileCellRendererImpl(FileCellRendererView view, AsynchTableFileHandleProvider provider) {
		this.view = view;
		this.fileHandleProvider = provider;
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
			// Get the table fileHanle.
			fileHandleProvider.requestFileHandle(new TableFileHandleRequest(value, address, new Callback<FileHandle, Throwable>() {
				
				@Override
				public void onSuccess(FileHandle result) {
					if(view.isAttached()){
						view.setLoadingVisible(false);
						view.setAnchor(result.getFileName(), createAnchorHref());
					}
				}
				
				@Override
				public void onFailure(Throwable reason) {
					if(view.isAttached()){
						view.setLoadingVisible(false);
						view.setErrorText(UNABLE_TO_LOAD_FILE_DATA);
					}
				}
			}));
		}
	}
	
	/**
	 * Create the href using the address of this renderer.
	 * @return
	 */
	public String createAnchorHref(){
		StringBuilder builder = new StringBuilder();
		builder.append("/Portal/");
		builder.append(WebConstants.FILE_HANDLE_UPLOAD_SERVLET);
		builder.append("?");
		builder.append(WebConstants.ENTITY_PARAM_KEY);
		builder.append("=");
		builder.append(address.getTableId());
		builder.append("&");
		builder.append(WebConstants.TABLE_COLUMN_ID);
		builder.append("=");
		builder.append(address.getColumnId());
		builder.append("&");
		builder.append(WebConstants.TABLE_ROW_ID);
		builder.append("=");
		builder.append(address.getRowId());
		builder.append("&");
		builder.append(WebConstants.TABLE_ROW_VERSION_NUMBER);
		builder.append("=");
		builder.append(address.getRowVersion());
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
