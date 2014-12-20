package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.web.client.StringUtils;
import org.sagebionetworks.web.client.widget.asynch.AsynchTableFileHandleProvider;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.table.CellAddress;

import com.google.gwt.core.client.Callback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileCellRendererImpl implements FileCellRenderer {
	
	private static final String I_DS_DO_NOT_MATCH = "IDs do not match";
	private static final String UNABLE_TO_LOAD_FILE_DATA = "Unable to load file data";
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
			fileHandleProvider.requestFileHandle(address, new Callback<FileHandle, Throwable>() {
				
				@Override
				public void onSuccess(FileHandle result) {
					if(view.isAttached()){
						view.setLoadingVisible(false);
						if(!value.equals(result.getId())){
							view.setErrorText(I_DS_DO_NOT_MATCH);
						}else{
							String href = "";
							
							view.setAnchor(result.getFileName(), createAnchorHref());
						}
					}
				}
				
				@Override
				public void onFailure(Throwable reason) {
					if(view.isAttached()){
						view.setLoadingVisible(false);
						view.setErrorText(UNABLE_TO_LOAD_FILE_DATA);
					}
				}
			});
		}
	}
	
	private String createAnchorHref(){
		return "/Portal/"+WebConstants.FILE_HANDLE_UPLOAD_SERVLET+"?"+WebConstants.ENTITY_PARAM_KEY+"="+address.getTableId()+"&"+WebConstants.TABLE_COLUMN_ID+"="+address.getColumnId()+"&"+WebConstants.TABLE_ROW_ID+"="+address.getRowId()+"&"+WebConstants.TABLE_ROW_VERSION_NUMBER+"="+address.getRowVersion();
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
