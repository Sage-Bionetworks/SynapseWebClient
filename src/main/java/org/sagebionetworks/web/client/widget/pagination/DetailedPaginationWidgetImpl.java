package org.sagebionetworks.web.client.widget.pagination;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DetailedPaginationWidgetImpl implements DetailedPaginationWidget, DetailedPaginationView.Presenter {
	
	public static final String NEXT = "Next";
	public static final String PREVIOUS = "Previous";
	/**
	 * The maximum number of page buttons by default.
	 */
	public static final int MAX_PAGE_BUTTONS_DEFAULT = 10;
	private DetailedPaginationView view;
	PageChangeListener listener;
	long limit;
	long offset;
	// start off with the default;
	int maxPageButtons = MAX_PAGE_BUTTONS_DEFAULT;
	
	@Inject
	public DetailedPaginationWidgetImpl(DetailedPaginationView view) {
		super();
		this.view = view;
		this.view.setPresenter(this);
	}

	@Override
	public void configure(Long limit, Long offset, Long count, final PageChangeListener listener) {
		this.listener = listener;
		if(count == null || limit == null || offset == null || count < 1 || limit < 1){
			view.setPagerVisible(false);
			view.setMessage("");
		}else{

			view.removeAllButtons();
			this.limit = limit;
			this.offset = offset;
			long remainder = count%limit;
			int totalNumberOfPages = (int)(count/limit);
			if(remainder > 0){
				totalNumberOfPages++;
			}
			// show the pager if we have more than on page.
			view.setPagerVisible(totalNumberOfPages > 1);
			int currentPageNumber = ((int)(offset/limit)) + 1;
			// previous
			long offsetIfClicked;
			if(currentPageNumber > 1){
				offsetIfClicked = offset - limit;
				view.addButton(offsetIfClicked, PREVIOUS, false);
			}
			// page buttons
			int buttonCount = Math.min(totalNumberOfPages, maxPageButtons);
			int minPageNumber = currentPageNumber;
			int maxPageNumber = currentPageNumber;
			for(int i=0; i<buttonCount-1; i++){
				if(currentPageNumber - minPageNumber < maxPageNumber - currentPageNumber){
					if(minPageNumber > 1){
						minPageNumber--;
					}else{
						maxPageNumber++;
					}
				}else{
					if(maxPageNumber < totalNumberOfPages){
						maxPageNumber++;
					}else{
						minPageNumber--;
					}
				}
			}
			boolean isActive = false;
			for(int i=minPageNumber; i < maxPageNumber+1; i++){
				isActive = i==currentPageNumber;
				offsetIfClicked = (i-1)*limit;
				view.addButton(offsetIfClicked, ""+i, isActive);
			}
			// Next
			if(currentPageNumber < totalNumberOfPages){
				offsetIfClicked = offset + limit;
				view.addButton(offsetIfClicked, NEXT, false);
			}
			setMessage(limit, offset, count);
		}
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void onPageChange(long clickedOffset) {
		this.listener.onPageChange(clickedOffset);
	}
	
	/**
	 * Overide the max number of page buttons.
	 * @param max
	 */
	public void setMaxPageButtons(int max){
		this.maxPageButtons = max;
	}

	private void setMessage(long limit, long offset, long count){
		long start = offset+1L;
		long end = Math.min(limit+offset, count);
		view.setMessage(start+" - "+end);
	}
}
