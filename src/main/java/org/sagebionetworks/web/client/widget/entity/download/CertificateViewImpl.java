package org.sagebionetworks.web.client.widget.entity.download;

import java.util.Date;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.place.Home;

import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CertificateViewImpl extends Composite implements CertificateView {
	
	@UiField
	Button continueButton;
	@UiField
	HeadingElement userNameCertificate;
	@UiField
	HeadingElement datePassed;
	
	private Presenter presenter;
	public interface Binder extends UiBinder<Widget, CertificateViewImpl> {}
	
	@Inject
	public CertificateViewImpl(Binder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));
		continueButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.goTo(new Home(ClientProperties.DEFAULT_PLACE_TOKEN));
			}
		});		
	}
	
	@Override
	public void setProfile(UserProfile profile) {
		userNameCertificate.setInnerHTML(DisplayUtils.getDisplayName(profile));
	}
	
	@Override
	public void setCertificationDate(Date dateCertified) {
		datePassed.setInnerHTML(DateTimeFormat.getLongDateFormat().format(dateCertified));
	}

	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void showInfo(String title, String message) {
	}

	@Override
	public void showErrorMessage(String message) {
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public void clear() {
		userNameCertificate.setInnerHTML("");
		datePassed.setInnerHTML("");
	}
}
