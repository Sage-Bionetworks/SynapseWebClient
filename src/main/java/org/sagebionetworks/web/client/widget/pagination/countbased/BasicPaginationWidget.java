package org.sagebionetworks.web.client.widget.pagination.countbased;

import org.sagebionetworks.web.client.widget.pagination.BasicPaginationView;
import org.sagebionetworks.web.client.widget.pagination.PageChangeListener;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * A basic pagination widget with a previous button, current page, total pages, and next button.
 * 
 * @author John
 *
 */
@Deprecated
public class BasicPaginationWidget implements BasicPaginationView.Presenter, IsWidget {

	BasicPaginationView view;
	PageChangeListener listener;
	long limit;
	long offset;

	@Inject
	public BasicPaginationWidget(BasicPaginationView view) {
		this.view = view;
		this.view.setPresenter(this);
	}

	/**
	 * Configure this widget with a limit, offset and count.
	 * 
	 * @param limit
	 * @param offset
	 * @param count
	 */
	public void configure(Long limit, Long offset, Long count, PageChangeListener listener) {
		this.listener = listener;
		if (count == null || limit == null || offset == null || count < 1 || limit < 1) {
			setLoading();
			view.setCurrentPage(1l);
		} else {
			this.limit = limit;
			this.offset = offset;
			long remainder = count % limit;
			long totalNumberOfPages = count / limit;
			if (remainder > 0) {
				totalNumberOfPages++;
			}
			long currentPageNumber = offset / limit + 1;
			view.setNextVisible(currentPageNumber < totalNumberOfPages);
			view.setPreviousVisible(currentPageNumber > 1);
			view.setCurrentPage(currentPageNumber);
		}
	}

	@Override
	public void onNext() {
		setLoading();
		this.listener.onPageChange(this.offset + limit);
	}

	@Override
	public void onPrevious() {
		setLoading();
		this.listener.onPageChange(this.offset - limit);
	}

	/**
	 * For now loading just disables both buttons.
	 * 
	 */
	private void setLoading() {
		// Disabled both buttons
		view.setNextVisible(false);
		view.setPreviousVisible(false);
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

}
