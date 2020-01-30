package org.sagebionetworks.web.client.widget.pagination;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.html.ClearFix;
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
public class BasicPaginationViewImpl implements BasicPaginationView {

	public interface Binder extends UiBinder<Widget, BasicPaginationViewImpl> {
	}

	@UiField
	Anchor previousButton;
	@UiField
	Anchor nextButton;
	@UiField
	ClearFix clearFix;
	Widget widget;

	@Inject
	public BasicPaginationViewImpl(Binder binder) {
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
	public void setNextVisible(boolean visible) {
		nextButton.setVisible(visible);
	}

	@Override
	public void setPreviousVisible(boolean visible) {
		previousButton.setVisible(visible);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setCurrentPage(long currentPageNumber) {}

	@Override
	public void setVisible(boolean visible) {
		widget.setVisible(visible);
	}

	@Override
	public void hideClearFix() {
		clearFix.setVisible(false);
	}
}
