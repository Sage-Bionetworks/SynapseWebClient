package org.sagebionetworks.web.client.widget.discussion;

import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadOrder;
import org.sagebionetworks.repo.model.table.SortDirection;
import org.sagebionetworks.web.client.widget.table.v2.results.SortableTableHeaderImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.SortingListener;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DiscussionThreadListWidgetViewImpl implements DiscussionThreadListWidgetView {

	public interface Binder extends UiBinder<Widget, DiscussionThreadListWidgetViewImpl> {
	}

	@UiField
	Column threadListContainer;
	@UiField
	Div synAlertContainer;
	@UiField
	Div threadCountAlertContainer;

	@UiField
	SortableTableHeaderImpl sortByReplies;
	@UiField
	SortableTableHeaderImpl sortByViews;
	@UiField
	SortableTableHeaderImpl sortByActivity;
	@UiField
	SortableTableHeaderImpl sortByTopic;
	@UiField
	Div threadHeader;
	@UiField
	Span noThreadsFound;

	Widget widget;
	private DiscussionThreadListWidget presenter;

	@Inject
	public DiscussionThreadListWidgetViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);


		SortingListener onSortRepliesClick = headerName -> {
			clearSort();
			presenter.sortBy(DiscussionThreadOrder.NUMBER_OF_REPLIES);
		};
		sortByReplies.setSortingListener(onSortRepliesClick);

		SortingListener onSortViewsClick = headerName -> {
			clearSort();
			presenter.sortBy(DiscussionThreadOrder.NUMBER_OF_VIEWS);
		};
		sortByViews.setSortingListener(onSortViewsClick);

		SortingListener onSortActivityClick = headerName -> {
			clearSort();
			presenter.sortBy(DiscussionThreadOrder.PINNED_AND_LAST_ACTIVITY);
		};
		sortByActivity.setSortingListener(onSortActivityClick);

		SortingListener onSortThreadTitle = headerName -> {
			clearSort();
			presenter.sortBy(DiscussionThreadOrder.THREAD_TITLE);
		};
		sortByTopic.setSortingListener(onSortThreadTitle);

		clearSort();
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
	public void setThreadHeaderVisible(boolean visible) {
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

	@Override
	public void clearSort() {
		SortableTableHeaderImpl[] sortableColumns = new SortableTableHeaderImpl[] {sortByReplies, sortByViews, sortByActivity, sortByTopic};
		for (SortableTableHeaderImpl column : sortableColumns) {
			column.setSortDirection(null);
		}
	}

	@Override
	public void setSorted(DiscussionThreadOrder column, boolean ascending) {
		switch (column) {
			case NUMBER_OF_REPLIES:
				updateSortUI(sortByReplies, ascending);
				break;
			case NUMBER_OF_VIEWS:
				updateSortUI(sortByViews, ascending);
				break;
			case PINNED_AND_LAST_ACTIVITY:
				updateSortUI(sortByActivity, ascending);
				break;
			case THREAD_TITLE:
				updateSortUI(sortByTopic, ascending);
				break;
			default:
				break;
		}
	}

	private void updateSortUI(SortableTableHeaderImpl sortColumn, boolean ascending) {
		SortDirection newDirection = ascending ? SortDirection.ASC : SortDirection.DESC;
		sortColumn.setSortDirection(newDirection);
	}
}
