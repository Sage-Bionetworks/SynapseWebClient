package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.web.client.StringUtils;
import org.sagebionetworks.web.client.widget.asynch.AsynchTableFileHandleProvider;
import org.sagebionetworks.web.shared.table.CellAddress;

import com.google.gwt.core.client.Callback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileCellRendererImpl implements FileCellRenderer {
	
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
						if(!value.equals(result.getId())){
							view.setFileName("IDs do not match");
						}else{
							view.setLoadingVisible(false);
							view.setFileName(result.getFileName());
						}
					}
				}
				
				@Override
				public void onFailure(Throwable reason) {
					if(view.isAttached()){
						view.setLoadingVisible(false);
						view.setFileName("Unable to load file data");
					}
				}
			});
		}
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
