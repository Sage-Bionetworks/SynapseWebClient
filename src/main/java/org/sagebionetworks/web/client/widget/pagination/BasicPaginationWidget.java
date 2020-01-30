package org.sagebionetworks.web.client.widget.pagination;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * A basic pagination widget with a previous button and next button.
 * 
 * @author Jay
 *
 */
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
	public void configure(Long limit, Long offset, Long currentPageItemCount, PageChangeListener listener) {
		this.listener = listener;
		if (limit == null || offset == null || limit < 1) {
			setLoading();
			view.setCurrentPage(1l);
		} else {
			this.limit = limit;
			this.offset = offset;
			long currentPageNumber = offset / limit + 1;
			view.setNextVisible(currentPageItemCount >= limit);
			view.setPreviousVisible(currentPageNumber > 1);
			view.setCurrentPage(currentPageNumber);
			view.setVisible(offset > 0 || currentPageItemCount >= limit);
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

	public void hideClearFix() {
		view.hideClearFix();
	}

}
