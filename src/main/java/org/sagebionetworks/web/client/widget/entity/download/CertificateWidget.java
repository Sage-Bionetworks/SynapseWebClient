package org.sagebionetworks.web.client.widget.entity.download;

import org.sagebionetworks.repo.model.quiz.PassingRecord;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CertificateWidget implements SynapseWidgetPresenter {
	CertificateWidgetView view;
	DateTimeUtils dateTimeUtils;

	@Inject
	public CertificateWidget(CertificateWidgetView view, DateTimeUtils dateTimeUtils) {
		this.view = view;
		this.dateTimeUtils = dateTimeUtils;
	}

	public void configure(PassingRecord passingRecord) {
		String dateCertified = dateTimeUtils.getDateTimeString(passingRecord.getPassedOn());
		view.configure(dateCertified);
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
