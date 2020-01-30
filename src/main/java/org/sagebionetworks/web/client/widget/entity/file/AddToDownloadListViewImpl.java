package org.sagebionetworks.web.client.widget.entity.file;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.InfoAlert;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AddToDownloadListViewImpl implements AddToDownloadListView, IsWidget {
	Div w;

	interface PackageSizeSummaryViewImplUiBinder extends UiBinder<Div, AddToDownloadListViewImpl> {
	}

	@UiField
	Alert confirmationUI;
	@UiField
	Span packageSizeContainer;
	@UiField
	Button confirmButton;
	@UiField
	Anchor cancelLink;
	@UiField
	Alert progressContainer;
	@UiField
	InfoAlert addedToDownloadListAlert;

	Presenter presenter;
	private static PackageSizeSummaryViewImplUiBinder uiBinder = GWT.create(PackageSizeSummaryViewImplUiBinder.class);

	@Inject
	public AddToDownloadListViewImpl(AuthenticationController authController, GlobalApplicationState globalAppState) {
		w = uiBinder.createAndBindUi(this);
		addedToDownloadListAlert.addClickHandler(event -> {
			Profile place = new Profile(authController.getCurrentUserPrincipalId() + "/downloads");
			globalAppState.getPlaceChanger().goTo(place);
		});
		cancelLink.addClickHandler(event -> {
			hideAll();
		});
		confirmButton.addClickHandler(event -> {
			presenter.onConfirmAddToDownloadList();
		});
	}

	@Override
	public void setPresenter(Presenter p) {
		presenter = p;
	}

	@Override
	public Widget asWidget() {
		return w;
	}

	@Override
	public void hideAll() {
		confirmationUI.setVisible(false);
		progressContainer.setVisible(false);
		addedToDownloadListAlert.setVisible(false);
	}

	@Override
	public void setPackageSizeSummary(IsWidget w) {
		packageSizeContainer.clear();
		packageSizeContainer.add(w);
	}

	@Override
	public void showConfirmAdd() {
		confirmationUI.setVisible(true);
	}

	@Override
	public void setAsynchronousProgressWidget(IsWidget w) {
		progressContainer.clear();
		progressContainer.add(w);
	}

	@Override
	public void showAsynchronousProgressWidget() {
		progressContainer.setVisible(true);
	}

	@Override
	public void showSuccess(int fileCount) {
		addedToDownloadListAlert.setMessage(fileCount + " files added to your Downloads List.");
		addedToDownloadListAlert.setVisible(true);
	}

	@Override
	public void add(IsWidget widget) {
		w.add(widget);
	}
}
