package org.sagebionetworks.web.client.widget.discussion;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.DisplayUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DiscussionThreadListItemWidgetViewImpl implements DiscussionThreadListItemWidgetView {

	public interface Binder extends UiBinder<Widget, DiscussionThreadListItemWidgetViewImpl> {
	}

	@UiField
	Span threadAuthor;
	@UiField
	Span activeUsers;
	@UiField
	Span numberOfReplies;
	@UiField
	Span numberOfViews;
	@UiField
	Span lastActivity;
	@UiField
	Anchor threadLink;

	private Widget widget;
	private DiscussionThreadListItemWidget presenter;

	@Inject
	public DiscussionThreadListItemWidgetViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
		threadLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!DisplayUtils.isAnyModifierKeyDown(event)) {
					event.preventDefault();
					presenter.onClickThread();
				}
			}
		});
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setPresenter(DiscussionThreadListItemWidget presenter) {
		this.presenter = presenter;
	}

	@Override
	public void setTitle(String title) {
		threadLink.setText(title);
	}

	@Override
	public void setNumberOfReplies(String numberOfReplies) {
		this.numberOfReplies.setText(numberOfReplies);
	}

	@Override
	public void setNumberOfViews(String numberOfViews) {
		this.numberOfViews.setText(numberOfViews);
	}

	@Override
	public void setLastActivity(String lastActivity) {
		this.lastActivity.setText(lastActivity);
	}

	@Override
	public void clearActiveAuthors() {
		activeUsers.clear();
	}

	@Override
	public void addActiveAuthor(Widget user) {
		activeUsers.add(user);
	}

	@Override
	public void setThreadAuthor(Widget widget) {
		threadAuthor.add(widget);
	}

	@Override
	public void setPinnedIconVisible(boolean visible) {
		if (visible) {
			threadLink.setIcon(IconType.THUMB_TACK);
		} else {
			threadLink.setIcon(null);
		}
	}

	@Override
	public void setThreadUrl(String url) {
		threadLink.setHref(url);
	}
}
