package org.sagebionetworks.web.client.widget.entity.file.downloadlist;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.widget.InfoAlert;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
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
	@UiField
	Button createPackageButton;
	@UiField
	TextBox fileName;
	@UiField
	Div progressTrackingContainer;
	
	Presenter presenter;
	@UiField
	Span createPackageReadyUI;
	@UiField
	Span downloadPackageReadyUI;
	@UiField
	Button downloadPackageButton;
	@UiField
	InfoAlert multiplePackagesRequiredAlert;
	@UiField
	InfoAlert filesDownloadedAlert;
	String downloadUrl;
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
		createPackageButton.addClickHandler(event-> {
			presenter.onCreatePackage(fileName.getText());
		});
		downloadPackageButton.addClickHandler(event -> {
			Window.open(downloadUrl, "_self", "");
			presenter.onDownloadPackage();
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
	@Override
	public void setProgressTrackingWidget(IsWidget w) {
		progressTrackingContainer.clear();
		progressTrackingContainer.add(w);
	}
	@Override
	public void setProgressTrackingWidgetVisible(boolean visible) {
		progressTrackingContainer.setVisible(visible);
	}
	
	@Override
	public void setCreatePackageUIVisible(boolean visible) {
		createPackageReadyUI.setVisible(visible);
		createPackageButton.setVisible(visible);
	}
	@Override
	public void setDownloadPackageUIVisible(boolean visible) {
		downloadPackageReadyUI.setVisible(visible);
		downloadPackageButton.setVisible(visible);
	}
	@Override
	public void setPackageDownloadURL(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}
	@Override
	public void setMultiplePackagesRequiredVisible(boolean visible) {
		multiplePackagesRequiredAlert.setVisible(visible);
	}
	@Override
	public void showFilesDownloadedAlert(int fileCount) {
		filesDownloadedAlert.setMessage(fileCount + " files were downloaded and removed from the list.");
		filesDownloadedAlert.setVisible(true);
	}
	@Override
	public void hideFilesDownloadedAlert() {
		filesDownloadedAlert.setVisible(false);	
	}
	@Override
	public void setPackageName(String zipFileName) {
		fileName.setText(zipFileName);
	}
}
