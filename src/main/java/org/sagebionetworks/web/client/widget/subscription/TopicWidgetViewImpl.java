package org.sagebionetworks.web.client.widget.subscription;

import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TopicWidgetViewImpl implements TopicWidgetView{

	public interface Binder extends UiBinder<Widget, TopicWidgetViewImpl> {
	}
	
	Widget w;
	Presenter presenter;
	
	@Inject
	public TopicWidgetViewImpl(Binder binder){
		this.w = binder.createAndBindUi(this);
	}
	
	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
		
	}
	
	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public Widget asWidget() {
		return w;
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void clear() {
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
}
