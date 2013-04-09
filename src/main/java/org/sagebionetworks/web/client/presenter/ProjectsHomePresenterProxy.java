package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.web.client.place.ProjectsHome;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AsyncProvider;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

/**
* Code split point for ProjectsHomePresenter.
* 
 * @author John
 *
 */
public class ProjectsHomePresenterProxy extends AbstractActivity implements PresenterProxy<ProjectsHome> {
	
	AsyncProvider<ProjectsHomePresenter> provider;
	
	@Inject
	public ProjectsHomePresenterProxy(AsyncProvider<ProjectsHomePresenter> provider){
		this.provider = provider;
	}

	@Override
	public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
		provider.get(new AsyncCallback<ProjectsHomePresenter>() {
			
			@Override
			public void onSuccess(ProjectsHomePresenter result) {
				result.start(panel, eventBus);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// Not sure what to do here.
				
			}
		});
	}

	@Override
	public void setPlace(final ProjectsHome place) {
		provider.get(new AsyncCallback<ProjectsHomePresenter>() {
			
			@Override
			public void onSuccess(ProjectsHomePresenter result) {
				result.setPlace(place);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// Not sure what to do here.
			}
		});
	}

}
