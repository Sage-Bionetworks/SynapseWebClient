package org.sagebionetworks.web.client.widget.pagination;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * A basic pagination widget with a previous button, current page, total pages, and next button.
 * @author John
 *
 */
public class BasicPaginationWidget implements BasicPaginationView.Presenter, PaginationWidget {
	
	BasicPaginationView view;
	PageChangeListener listener;
	long limit;
	long offset;
	
	@Inject
	public BasicPaginationWidget(BasicPaginationView view){
		this.view = view;
		this.view.setPresenter(this);
	}
	
	/**
	 * Configure this widget with a limit, offset and count.
	 * @param limit
	 * @param offset
	 * @param count
	 */
	@Override
	public void configure(Long limit, Long offset, Long count, PageChangeListener listener){
		this.listener = listener;
		if(count == null || limit == null || offset == null || count < 1 || limit < 1){
			setLoading();
			view.setCurrentPage(1l);
		}else{
			this.limit = limit;
			this.offset = offset;
			long remainder = count%limit;
			long totalNumberOfPages = count/limit;
			if(remainder > 0){
				totalNumberOfPages++;
			}
			long currentPageNumber = offset/limit + 1;
			view.setNextEnabled(currentPageNumber < totalNumberOfPages);
			view.setPreviousEnabled(currentPageNumber > 1);
			view.setCurrentPage(currentPageNumber);
		}
	}

	@Override
	public void onNext() {
		setLoading();
		this.listener.onPageChange(this.offset+limit);
	}

	@Override
	public void onPrevious() {
		setLoading();
		this.listener.onPageChange(this.offset-limit);
	}
	
	/**
	 * For now loading just disables both buttons.
	 * 
	 */
	private void setLoading() {
		// Disabled both buttons
		view.setNextEnabled(false);
		view.setPreviousEnabled(false);
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

}
