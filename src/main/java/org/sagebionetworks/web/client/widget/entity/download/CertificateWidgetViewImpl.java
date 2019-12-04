package org.sagebionetworks.web.client.widget.entity.download;

import org.sagebionetworks.web.client.widget.InfoAlert;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CertificateWidgetViewImpl implements CertificateWidgetView, IsWidget {
	Widget widget;
	@UiField
	InfoAlert alert;
	public static final String PASSED_QUIZ_ON = "You passed the Synapse Certification Quiz on ";

	public interface Binder extends UiBinder<Widget, CertificateWidgetViewImpl> {
	}

	@Inject
	public CertificateWidgetViewImpl(Binder uiBinder) {
		widget = uiBinder.createAndBindUi(this);
	}

	@Override
	public void configure(String dateCertified) {
		alert.setMessage(PASSED_QUIZ_ON + dateCertified);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}
}
