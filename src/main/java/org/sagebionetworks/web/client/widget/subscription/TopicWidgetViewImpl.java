package org.sagebionetworks.web.client.widget.subscription;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TopicWidgetViewImpl implements TopicWidgetView{

	@UiField
	Anchor topicLink;
	
	public interface Binder extends UiBinder<Widget, TopicWidgetViewImpl> {
	}
	
	Widget w;
	Presenter presenter;
	
	@Inject
	public TopicWidgetViewImpl(Binder binder){
		this.w = binder.createAndBindUi(this);
	}
	
	@Override
	public void setTopicHref(String href) {
		topicLink.setHref(href);
	}
	@Override
	public void setTopicText(String text) {
		topicLink.setText(text);
	}
	
	@Override
	public void addStyleNames(String styleNames) {
		topicLink.addStyleName(styleNames);
	}
	
	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
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
	@Override
	public void setIcon(IconType type) {
		topicLink.setIcon(type);
	}
}
