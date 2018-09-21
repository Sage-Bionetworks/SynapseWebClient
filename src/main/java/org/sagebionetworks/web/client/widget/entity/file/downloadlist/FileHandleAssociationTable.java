package org.sagebionetworks.web.client.widget.entity.file.downloadlist;

import java.util.List;

import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileHandleAssociationTable implements IsWidget {
	
	private FileHandleAssociationTableView view;
	PortalGinInjector ginInjector;
	Callback accessRestrictionDetectedCallback;
	@Inject
	public FileHandleAssociationTable(
			FileHandleAssociationTableView view,
			PortalGinInjector ginInjector) {
		this.view = view;
		this.ginInjector = ginInjector;
		accessRestrictionDetectedCallback = () -> {
			view.showAccessRestrictionsDetectedUI();
		};
	}
	
	public void configure(List<FileHandleAssociation> fhas, CallbackP<Double> addToPackageSizeCallback, CallbackP<FileHandleAssociation> onRemove) {
		view.clear();
		// create a fha table row for each fha
		for (FileHandleAssociation fha : fhas) {
			FileHandleAssociationRow row = ginInjector.getFileHandleAssociationRow();
			row.configure(fha, accessRestrictionDetectedCallback, addToPackageSizeCallback, onRemove);
			view.addRow(row);
		}
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
