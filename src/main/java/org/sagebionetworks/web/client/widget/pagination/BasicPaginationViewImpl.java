package org.sagebionetworks.web.client.widget.pagination;

import org.gwtbootstrap3.client.ui.Badge;
import org.gwtbootstrap3.client.ui.Button;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryResultEditorViewImpl;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * The basic view of a basic pagination widget with zero business logic.
 * 
 * @author John
 *
 */
public class BasicPaginationViewImpl implements BasicPaginationView{
	
	public interface Binder extends UiBinder<Widget, BasicPaginationViewImpl> {}
	
	@UiField
	Button previousButton;
	@UiField
	Badge currentPage;
	@UiField
	Button nextButton;
	Widget widget;
	
	@Inject
	public BasicPaginationViewImpl(Binder binder){
		widget = binder.createAndBindUi(this);
	}

	@Override
	public void setPresenter(final Presenter presenter) {
		previousButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onPrevious();
			}
		});
		nextButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				presenter.onNext();
			}
		});
	}

	@Override
	public void setNextEnabled(boolean enabled) {
		nextButton.setEnabled(enabled);
	}

	@Override
	public void setPreviousEnabled(boolean enabled) {
		previousButton.setEnabled(enabled);
	}

	@Override
	public void setCurrentPage(long currentPageNumber) {
		this.currentPage.setText(""+currentPageNumber);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

}
