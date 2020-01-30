package org.sagebionetworks.web.client.widget.profile;

import static org.sagebionetworks.web.client.presenter.ProfilePresenter.IS_CERTIFIED;
import static org.sagebionetworks.web.client.presenter.ProfilePresenter.IS_VERIFIED;
import org.sagebionetworks.repo.model.UserBundle;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProfileCertifiedValidatedWidget implements IsWidget {
	SynapseJavascriptClient jsClient;
	ProfileCertifiedValidatedView view;
	Long userId;

	@Inject
	public ProfileCertifiedValidatedWidget(ProfileCertifiedValidatedView view, SynapseJavascriptClient jsClient) {
		this.jsClient = jsClient;
		this.view = view;
	};

	public void configure(Long userId) {
		this.userId = userId;
		loadData();
	}

	public void loadData() {
		int mask = IS_CERTIFIED | IS_VERIFIED;
		jsClient.getUserBundle(userId, mask, new AsyncCallback<UserBundle>() {
			@Override
			public void onSuccess(UserBundle bundle) {
				view.setCertifiedVisible(bundle.getIsCertified());
				view.setVerifiedVisible(bundle.getIsVerified());
			}

			@Override
			public void onFailure(Throwable caught) {
				view.setError(caught.getMessage());
			}
		});
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
