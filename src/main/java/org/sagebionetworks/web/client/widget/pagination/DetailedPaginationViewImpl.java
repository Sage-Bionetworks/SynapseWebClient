package org.sagebionetworks.web.client.widget.pagination;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Pagination;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * The view with zero business logic.
 * 
 * @author John
 *
 */
public class DetailedPaginationViewImpl implements DetailedPaginationView {

	public interface Binder extends UiBinder<Widget, DetailedPaginationViewImpl> {}
	
	@UiField
	Pagination pageContainer;
	Presenter presenter;
	Widget widget;
	
	@Inject
	public DetailedPaginationViewImpl(Binder binder){
		widget = binder.createAndBindUi(this);
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setPagerVisible(boolean visible) {
		pageContainer.setVisible(visible);
		
	}

	@Override
	public void addButton(final Long clickedOffset, String text, boolean isActive) {
		AnchorListItem anchor = new AnchorListItem(text);
		anchor.setActive(isActive);
		pageContainer.add(anchor);
		// Forward back the the presenter when clicked.
		anchor.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onPageChange(clickedOffset);
			}
		});
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void removeAllButtons() {
		pageContainer.clear();		
	}
}
