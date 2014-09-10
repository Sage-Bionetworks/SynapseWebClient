package org.sagebionetworks.web.client.widget.entity.download;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.quiz.PassingRecord;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.download.CertificateWidgetView.Presenter;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CertificateWidget implements Presenter, SynapseWidgetPresenter {
	private CertificateWidgetView view;
	
	@Inject
	public CertificateWidget(CertificateWidgetView view 
		) {
		this.view = view;
		view.setPresenter(this);
	}

	public void configure(UserProfile profile, PassingRecord passingRecord) {
		view.setProfile(profile);
		view.setCertificationDate(passingRecord.getPassedOn());
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

}
