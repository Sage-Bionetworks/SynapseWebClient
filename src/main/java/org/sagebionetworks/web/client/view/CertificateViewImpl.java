package org.sagebionetworks.web.client.view;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Panel;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.quiz.PassingRecord;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.LoadingSpinner;
import org.sagebionetworks.web.client.widget.entity.download.CertificateWidget;
import org.sagebionetworks.web.client.widget.header.Header;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CertificateViewImpl extends Composite implements CertificateView {
	@UiField
	SimplePanel errorContainer;	
	@UiField
	SimplePanel certificateContainer;
	@UiField
	Panel userNotCertifiedPanel;
	@UiField
	Heading userNotCertifiedHeading;
	@UiField
	LoadingSpinner loadingUI;
	
	@UiField
	Button okButton;
	
	private Presenter presenter;
	private CertificateWidget certificateWidget;
	private Header headerWidget;
	public interface Binder extends UiBinder<Widget, CertificateViewImpl> {}
	
	@Inject
	public CertificateViewImpl(Binder uiBinder,
			Header headerWidget,
			CertificateWidget certificateWidget) {
		initWidget(uiBinder.createAndBindUi(this));
		this.headerWidget = headerWidget;
		this.certificateWidget = certificateWidget;
		headerWidget.configure();
		certificateContainer.setWidget(certificateWidget.asWidget());
		okButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.okButtonClicked();
			}
		});
	}

	@Override
	public void setPresenter(Presenter loginPresenter) {
		this.presenter = loginPresenter;
		headerWidget.configure();
		DisplayUtils.scrollToTop();
	}
	
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {
		loadingUI.setVisible(true);
	}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}


	@Override
	public void clear() {
		hideAll();
	}
	
	
	@Override
	public void showSuccess(UserProfile profile, PassingRecord passingRecord) {
		//show certificate
		certificateWidget.configure(profile, passingRecord);
		hideLoading();
		certificateContainer.setVisible(true);
		okButton.setVisible(true);
		DisplayUtils.scrollToTop();
	}
	
	@Override
	public void setSynapseAlertWidget(Widget synAlert) {
		errorContainer.setWidget(synAlert);
	}
	
	
	private void hideAll() {
		certificateContainer.setVisible(false);
		userNotCertifiedPanel.setVisible(false);
		okButton.setVisible(false);
		hideLoading();
	}

	@Override
	public void hideLoading() {
		loadingUI.setVisible(false);
	}
	
	@Override
	public void showNotCertified(UserProfile profile) {
		userNotCertifiedHeading.setText(DisplayUtils.getDisplayName(profile) + " is not certified");
		hideLoading();
		userNotCertifiedPanel.setVisible(true);
		okButton.setVisible(true);
	}
}
