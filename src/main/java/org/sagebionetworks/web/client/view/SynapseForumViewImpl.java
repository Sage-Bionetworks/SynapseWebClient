package org.sagebionetworks.web.client.view;

import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.header.Header;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SynapseForumViewImpl implements SynapseForumView {

	@UiField
	Div synAlertContainer;
	@UiField
	Div wikiContainer;
	@UiField
	Div forumWidgetContainer;
	private Presenter presenter;

	Widget widget;

	public interface SynapseForumViewImplUiBinder extends UiBinder<Widget, SynapseForumViewImpl> {}

	@Inject
	public SynapseForumViewImpl(
			SynapseForumViewImplUiBinder binder, 
			Header headerWidget) {
		widget = binder.createAndBindUi(this);
		headerWidget.configure(false);
		headerWidget.refresh();
		Window.scrollTo(0, 0); // scroll user to top of page
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void setAlert(Widget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}

	@Override
	public void showErrorMessage(String errorMessage) {
		DisplayUtils.showErrorMessage(errorMessage);
	}
	@Override
	public void setWikiWidget(Widget w) {
		wikiContainer.clear();
		wikiContainer.add(w);
	}

	@Override
	public void setForumWidget(Widget widget) {
		forumWidgetContainer.add(widget);
	}
}
