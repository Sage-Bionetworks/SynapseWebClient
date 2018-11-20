package org.sagebionetworks.web.client.widget.discussion;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadOrder;
import org.sagebionetworks.web.client.widget.table.v2.results.SortableTableHeaderImpl;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DiscussionThreadListWidgetViewImpl implements DiscussionThreadListWidgetView{

	public interface Binder extends UiBinder<Widget, DiscussionThreadListWidgetViewImpl> {}

	@UiField
	Column threadListContainer;
	@UiField
	Div synAlertContainer;
	@UiField
	Div threadCountAlertContainer;
	
	@UiField
	Anchor sortByReplies;
	@UiField
	Icon sortByRepliesIcon;
	@UiField
	Anchor sortByViews;
	@UiField
	Icon sortByViewsIcon;
	@UiField
	Anchor sortByActivity;
	@UiField
	Icon sortByActivityIcon;
	@UiField
	Anchor sortByTopic;
	@UiField
	Icon sortByTopicIcon;
	@UiField
	Div threadHeader;
	@UiField
	Span noThreadsFound;
	
	Widget widget;
	private DiscussionThreadListWidget presenter;
	
	@Inject
	public DiscussionThreadListWidgetViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);

		ClickHandler onSortRepliesClick = event -> {
			clearSortUI();
			presenter.sortBy(DiscussionThreadOrder.NUMBER_OF_REPLIES);
		};
		sortByReplies.addClickHandler(onSortRepliesClick);
		sortByRepliesIcon.addClickHandler(onSortRepliesClick);
		
		ClickHandler onSortViewsClick = event -> {
			clearSortUI();
			presenter.sortBy(DiscussionThreadOrder.NUMBER_OF_VIEWS);
		};
		sortByViews.addClickHandler(onSortViewsClick);
		sortByViewsIcon.addClickHandler(onSortViewsClick);

		ClickHandler onSortActivityClick = event -> {
			clearSortUI();
			presenter.sortBy(DiscussionThreadOrder.PINNED_AND_LAST_ACTIVITY);
		};
		sortByActivity.addClickHandler(onSortActivityClick);
		sortByActivityIcon.addClickHandler(onSortActivityClick);


		ClickHandler onSortThreadTitle = event -> {
			clearSortUI();
			presenter.sortBy(DiscussionThreadOrder.THREAD_TITLE);
		};
		sortByTopic.addClickHandler(onSortThreadTitle);
		sortByTopicIcon.addClickHandler(onSortThreadTitle);
		
		clearSortUI();
	}

	@Override
	public void setThreadsContainer(IsWidget container) {
		threadListContainer.clear();
		threadListContainer.add(container);
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setPresenter(DiscussionThreadListWidget presenter) {
		this.presenter = presenter;
	}

	@Override
	public void setAlert(Widget w) {
		synAlertContainer.add(w);
	}

	@Override
	public void setThreadCountAlert(Widget w) {
		threadCountAlertContainer.clear();
		threadCountAlertContainer.add(w);
	};

	@Override
	public void setThreadHeaderVisible(boolean visible){
		threadHeader.setVisible(visible);
	}

	@Override
	public void setNoThreadsFoundVisible(boolean visible) {
		noThreadsFound.setVisible(visible);
	}
	
	@Override
	public void scrollIntoView(Widget w) {
		Window.scrollTo(0, w.getElement().getOffsetTop());
	}
	

	// called whenever sort is called
	private void clearSortUI() {
		Icon[] icons = new Icon[] {sortByRepliesIcon, sortByViewsIcon, sortByActivityIcon, sortByTopicIcon};
		for (Icon icon : icons) {
			icon.setType(IconType.SYN_SORT_DESC);
			icon.removeStyleName(SortableTableHeaderImpl.SORTED_STYLES);
			icon.addStyleName(SortableTableHeaderImpl.UNSORTED_STYLES);
		}
	}
	
	@Override
	public void setSorted(DiscussionThreadOrder column, boolean ascending) {
		switch (column) {
			case NUMBER_OF_REPLIES:
				updateSortUI(sortByRepliesIcon, ascending);
				break;
			case NUMBER_OF_VIEWS:
				updateSortUI(sortByViewsIcon, ascending);
				break;
			case PINNED_AND_LAST_ACTIVITY:
				updateSortUI(sortByActivityIcon, ascending);
				break;
			case THREAD_TITLE:
				updateSortUI(sortByTopicIcon, ascending);
				break;
			default:
				break;
		}
	}
	
	private void updateSortUI(Icon sortColumnIcon, boolean ascending) {
		IconType newType = ascending ? IconType.SYN_SORT_ASC : IconType.SYN_SORT_DESC;
		sortColumnIcon.setType(newType);
		sortColumnIcon.removeStyleName(SortableTableHeaderImpl.UNSORTED_STYLES);
		sortColumnIcon.addStyleName(SortableTableHeaderImpl.SORTED_STYLES);
	}
	
}
