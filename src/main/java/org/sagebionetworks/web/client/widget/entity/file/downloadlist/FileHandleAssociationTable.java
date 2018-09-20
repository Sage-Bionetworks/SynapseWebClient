package org.sagebionetworks.web.client.widget.entity.file.downloadlist;

import java.util.List;

import org.sagebionetworks.repo.model.file.DownloadList;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.events.DownloadListUpdatedEvent;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileHandleAssociationTable implements IsWidget {
	
	private FileHandleAssociationTableView view;
	PortalGinInjector ginInjector;
	@Inject
	public FileHandleAssociationTable(
			FileHandleAssociationTableView view,
			PortalGinInjector ginInjector) {
		this.view = view;
		this.ginInjector = ginInjector;
	}
	
	public void configure(List<FileHandleAssociation> fhas, CallbackP<FileHandleAssociation> onDelete) {
		view.clear();
		// create a fha table row for each fha
		for (FileHandleAssociation fha : fhas) {
			FileHandleAssociationRow row = ginInjector.getFileHandleAssociationRow();
			row.configure(fha, onDelete);
			view.addRow(row);
		}
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
