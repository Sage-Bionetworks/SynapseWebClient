package org.sagebionetworks.web.client.widget.table.modal.download;

import java.util.List;
import org.sagebionetworks.repo.model.table.FacetColumnRequest;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DownloadTableQueryModalWidgetImpl implements DownloadTableQueryModalWidget {

	private static final String DOWNLOAD_QUERY_RESULTS = "Download query results";
	// injected dependencies
	ModalWizardWidget modalWizardWidget;
	CreateDownloadPage createDownloadPage;

	@Inject
	public DownloadTableQueryModalWidgetImpl(ModalWizardWidget modalWizardWidget, CreateDownloadPage createDownloadPage) {
		this.modalWizardWidget = modalWizardWidget;
		this.createDownloadPage = createDownloadPage;
		this.modalWizardWidget.setTitle(DOWNLOAD_QUERY_RESULTS);
		this.modalWizardWidget.configure(this.createDownloadPage);
	}


	@Override
	public void configure(String sql, String tableId, List<FacetColumnRequest> selectedFacets) {
		this.createDownloadPage.configure(sql, tableId, selectedFacets);
	}


	@Override
	public Widget asWidget() {
		return modalWizardWidget.asWidget();
	}


	@Override
	public void showModal() {
		modalWizardWidget.showModal(null);
	}

}
