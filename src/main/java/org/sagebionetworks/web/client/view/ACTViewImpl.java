package org.sagebionetworks.web.client.view;

import java.util.List;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.header.Header;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ACTViewImpl implements ACTView {

	public interface ACTViewImplUiBinder extends UiBinder<Widget, ACTViewImpl> {
	}

	@UiField
	DropDownMenu stateDropdownMenu;
	@UiField
	Div userSelectContainer;
	@UiField
	Div synAlertContainer;

	@UiField
	Div tableData;
	@UiField
	Button clearStateFilter;
	@UiField
	Button clearUserFilter;
	@UiField
	Span currentState;
	@UiField
	Div currentUserContainer;

	private Presenter presenter;
	private Header headerWidget;

	Widget widget;

	@Inject
	public ACTViewImpl(ACTViewImplUiBinder binder, Header headerWidget) {
		widget = binder.createAndBindUi(this);
		this.headerWidget = headerWidget;
		headerWidget.configure();

		clearStateFilter.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onClearStateFilter();
			}
		});
		clearUserFilter.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onClearUserFilter();
			}
		});


	}

	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
		headerWidget.configure();
		headerWidget.refresh();
		Window.scrollTo(0, 0); // scroll user to top of page
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void clear() {}

	@Override
	public void setSelectedStateText(String state) {
		currentState.setText(state);
	}

	@Override
	public void setSelectedUserBadge(Widget w) {
		currentUserContainer.add(w);
	}

	@Override
	public void setSelectedUserBadgeVisible(boolean visible) {
		currentUserContainer.setVisible(visible);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setStates(List<String> states) {
		stateDropdownMenu.clear();
		for (final String state : states) {
			AnchorListItem item = new AnchorListItem(state);
			item.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					presenter.onStateSelected(state);
				}
			});
			stateDropdownMenu.add(item);
		}
	}

	@Override
	public void setUserPickerWidget(Widget w) {
		userSelectContainer.clear();
		userSelectContainer.add(w);
	}

	@Override
	public void setLoadMoreContainer(Widget w) {
		tableData.clear();
		tableData.add(w);
	}

	@Override
	public void setSynAlert(Widget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}
}
