package org.sagebionetworks.web.client.view;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.SynapseView;
import com.google.gwt.user.client.ui.IsWidget;

public interface HomeView extends IsWidget, SynapseView {
	void refresh();

	void showLoggedInUI(UserProfile profile);

	void showRegisterUI();

	void showLoginUI();

	void scrollToTop();
}
