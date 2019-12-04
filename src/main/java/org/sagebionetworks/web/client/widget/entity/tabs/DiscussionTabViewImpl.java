package org.sagebionetworks.web.client.widget.entity.tabs;

import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

public class DiscussionTabViewImpl implements DiscussionTabView {
	@UiField
	Div forumContainer;
	private Presenter presenter;

	Widget widget;

	public interface TabsViewImplUiBinder extends UiBinder<Widget, DiscussionTabViewImpl> {
	}

	public DiscussionTabViewImpl() {
		TabsViewImplUiBinder binder = GWT.create(TabsViewImplUiBinder.class);
		widget = binder.createAndBindUi(this);
	}

	@Override
	public void setForum(Widget w) {
		forumContainer.add(w);
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
	public void showErrorMessage(String errorMessage) {
		DisplayUtils.showErrorMessage(errorMessage);
	}
}
