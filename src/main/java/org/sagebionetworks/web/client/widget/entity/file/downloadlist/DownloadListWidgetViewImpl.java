package org.sagebionetworks.web.client.widget.entity.file.downloadlist;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.html.Div;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DownloadListWidgetViewImpl implements DownloadListWidgetView, IsWidget {
	@UiField
	Div synAlertContainer;
	@UiField
	Anchor clearAllLink;
	@UiField
	Div fileHandleAssociationTableContainer;
	@UiField
	Div packageSummaryContainer;
	Presenter presenter;
	Widget w;
	interface DownloadListWidgetViewImplUiBinder extends UiBinder<Widget, DownloadListWidgetViewImpl> {}
	
	private static DownloadListWidgetViewImplUiBinder uiBinder = GWT
			.create(DownloadListWidgetViewImplUiBinder.class);
	@Inject
	public DownloadListWidgetViewImpl() {
		w = uiBinder.createAndBindUi(this);
		clearAllLink.addClickHandler(event -> {
			presenter.onClearDownloadList();
		});
	}
	@Override
	public void setPresenter(Presenter p) {
		this.presenter = p;
	}
	@Override
	public Widget asWidget() {
		return w;
	}
	@Override
	public void clear() {
	}
	@Override
	public void setSynAlert(IsWidget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}
	@Override
	public void setFileHandleAssociationTable(IsWidget w) {
		fileHandleAssociationTableContainer.clear();
		fileHandleAssociationTableContainer.add(w);
	}
	@Override
	public void setPackageSizeSummary(IsWidget w) {
		packageSummaryContainer.clear();
		packageSummaryContainer.add(w);
	}
}
