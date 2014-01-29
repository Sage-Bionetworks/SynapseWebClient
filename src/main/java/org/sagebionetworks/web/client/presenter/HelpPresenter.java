package org.sagebionetworks.web.client.presenter;

import java.util.HashMap;
import java.util.Map;

import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.web.client.place.Help;
import org.sagebionetworks.web.client.view.HelpView;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class HelpPresenter extends AbstractActivity implements HelpView.Presenter, Presenter<Help> {
		
	private Help place;
	private HelpView view;
	private Map<String, WikiPageKey> pageName2WikiKeyMap;
	public static final String USER_GUIDE = "UserGuide";
	public static final String GETTING_STARTED = "GettingStarted";
	public static final String CREATE_PROJECT = "CreateProject";
	public static final String R_CLIENT = "RClient";
	public static final String PYTHON_CLIENT = "PythonClient";
	
	
	@Inject
	public HelpPresenter(HelpView view){
		this.view = view;
		view.setPresenter(this);
		pageName2WikiKeyMap = new HashMap<String, WikiPageKey>();
		pageName2WikiKeyMap.put(USER_GUIDE, new WikiPageKey("syn1669771", ObjectType.ENTITY.toString(), null));
		pageName2WikiKeyMap.put(GETTING_STARTED, new WikiPageKey("syn1669771", ObjectType.ENTITY.toString(), "54546"));
		pageName2WikiKeyMap.put(CREATE_PROJECT, new WikiPageKey("syn1669771", ObjectType.ENTITY.toString(), "54547"));
		pageName2WikiKeyMap.put(R_CLIENT, new WikiPageKey("syn1834618", ObjectType.ENTITY.toString(), null));
		pageName2WikiKeyMap.put(PYTHON_CLIENT, new WikiPageKey("syn1768504", ObjectType.ENTITY.toString(), null));
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	@Override
	public void setPlace(Help place) {
		this.place = place;
		this.view.setPresenter(this);
		String pageName = place.toToken();
		WikiPageKey key = pageName2WikiKeyMap.get(pageName);
		if (key != null)
			view.showHelpPage(key);
	}
	
	@Override
    public String mayStop() {
        view.clear();
        return null;
    }
}
