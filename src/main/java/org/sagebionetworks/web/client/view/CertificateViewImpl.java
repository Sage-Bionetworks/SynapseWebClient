package org.sagebionetworks.web.client.view;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Panel;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.quiz.PassingRecord;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.entity.download.CertificateWidget;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.login.LoginWidget;

import com.google.gwt.dom.client.SpanElement;
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
	SimplePanel header;
	@UiField
	SimplePanel footer;
	
	@UiField
	SimplePanel certificateContainer;
	
	@UiField
	Panel userNotCertifiedPanel;
	@UiField
	Heading userNotCertifiedHeading;
	@UiField
	SpanElement loadingUI;
	
	@UiField
	Button okButton;
	
	private Presenter presenter;
	private CertificateWidget certificateWidget;
	private Header headerWidget;
	private Footer footerWidget;
	public interface Binder extends UiBinder<Widget, CertificateViewImpl> {}
	
	@Inject
	public CertificateViewImpl(Binder uiBinder,
			Header headerWidget, 
			Footer footerWidget,
			LoginWidget loginWidget, 
			CertificateWidget certificateWidget) {
		initWidget(uiBinder.createAndBindUi(this));
		this.headerWidget = headerWidget;
		this.footerWidget = footerWidget;
		this.certificateWidget = certificateWidget;
		headerWidget.configure();
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());
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
		header.clear();
		headerWidget.configure();
		header.add(headerWidget.asWidget());
		footer.clear();
		footer.add(footerWidget.asWidget());
		DisplayUtils.scrollToTop();
	}
	
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {
		DisplayUtils.show(loadingUI);
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
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
	
	
	private void hideAll() {
		certificateContainer.setVisible(false);
		userNotCertifiedPanel.setVisible(false);
		okButton.setVisible(false);
		hideLoading();
	}

	@Override
	public void hideLoading() {
		DisplayUtils.hide(loadingUI);
	}
	
	@Override
	public void showNotCertified(UserProfile profile) {
		userNotCertifiedHeading.setText(DisplayUtils.getDisplayName(profile) + " is not certified");
		hideLoading();
		userNotCertifiedPanel.setVisible(true);
		okButton.setVisible(true);
	}
}
