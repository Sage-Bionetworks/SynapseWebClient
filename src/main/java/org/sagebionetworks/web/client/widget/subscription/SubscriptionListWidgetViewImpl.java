package org.sagebionetworks.web.client.widget.subscription;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.RadioButton;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.repo.model.subscription.SortDirection;
import org.sagebionetworks.repo.model.subscription.SubscriptionObjectType;
import org.sagebionetworks.web.client.view.bootstrap.table.Table;
import org.sagebionetworks.web.client.widget.LoadingSpinner;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SubscriptionListWidgetViewImpl implements SubscriptionListWidgetView {

	@UiField
	RadioButton projectFilter;
	@UiField
	RadioButton threadFilter;

	@UiField
	Div synAlertContainer;
	@UiField
	SimplePanel emptyUI;
	@UiField
	Div paginationWidgetContainer;
	@UiField
	LoadingSpinner loadingUI;
	@UiField
	Table subscriptionsContainer;
	@UiField
	AnchorListItem sortAscending;
	@UiField
	AnchorListItem sortDescending;
	@UiField
	Button sortButton;

	public interface Binder extends UiBinder<Widget, SubscriptionListWidgetViewImpl> {
	}

	Widget w;
	Presenter presenter;

	@Inject
	public SubscriptionListWidgetViewImpl(Binder binder) {
		this.w = binder.createAndBindUi(this);
		projectFilter.addClickHandler(event -> {
			presenter.onFilter(SubscriptionObjectType.FORUM);
		});
		threadFilter.addClickHandler(event -> {
			presenter.onFilter(SubscriptionObjectType.THREAD);
		});
		sortAscending.addClickHandler(event -> {
			sortButton.setText(sortAscending.getText());
			presenter.onSort(SortDirection.ASC);
		});
		sortDescending.addClickHandler(event -> {
			sortButton.setText(sortDescending.getText());
			presenter.onSort(SortDirection.DESC);
		});
	}

	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public Widget asWidget() {
		return w;
	}

	@Override
	public void setSynAlert(Widget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}

	@Override
	public void addNewSubscription(Widget topicRow) {
		subscriptionsContainer.add(topicRow);
	}

	public void clearSubscriptions() {
		subscriptionsContainer.clear();
	};

	@Override
	public void clearFilter() {
		projectFilter.setValue(true, false);
		projectFilter.setActive(true);
		sortButton.setText(sortAscending.getText());
	}

	@Override
	public void setNoItemsMessageVisible(boolean visible) {
		emptyUI.setVisible(visible);
	}

	@Override
	public void setPagination(Widget w) {
		paginationWidgetContainer.clear();
		paginationWidgetContainer.add(w);
	}

	@Override
	public void setLoadingVisible(boolean visible) {
		loadingUI.setVisible(visible);
	}
}
