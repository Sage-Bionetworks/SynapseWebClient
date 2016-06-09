package org.sagebionetworks.web.client.widget.entity.tabs;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.ParameterizedToken;
import org.sagebionetworks.web.client.utils.CallbackP;

import com.google.inject.Inject;

public class DockerTab implements DockerTabView.Presenter{
	private static final String DOCKER_TAB_TITLE = "Docker";

	Tab tab;
	DockerTabView view;
	CookieProvider cookies;

	String entityId;
	String entityName;
	//use this token to navigate between threads within the docker tab
	ParameterizedToken params;

	@Inject
	public DockerTab(
			DockerTabView view,
			Tab tab,
			CookieProvider cookies
			) {
		this.view = view;
		this.tab = tab;
		this.cookies = cookies;
		tab.configure(DOCKER_TAB_TITLE, view.asWidget());
		view.setPresenter(this);
	}

	public void setTabClickedCallback(CallbackP<Tab> onClickCallback) {
		tab.addTabClickedCallback(onClickCallback);
	}

	public void configure(String entityId, String entityName, String areaToken) {
		this.entityId = entityId;
		this.entityName = entityName;
		this.params = new ParameterizedToken(areaToken);
		tab.setTabListItemVisible(DisplayUtils.isInTestWebsite(cookies));
	}

	public Tab asTab(){
		return tab;
	}
}
