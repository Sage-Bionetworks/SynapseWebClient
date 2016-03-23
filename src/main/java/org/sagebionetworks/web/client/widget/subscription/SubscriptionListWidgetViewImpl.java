package org.sagebionetworks.web.client.widget.subscription;

import org.gwtbootstrap3.client.ui.RadioButton;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.repo.model.subscription.SubscriptionObjectType;
import org.sagebionetworks.web.client.view.bootstrap.table.Table;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SubscriptionListWidgetViewImpl implements SubscriptionListWidgetView{

	@UiField
	RadioButton noFilter;
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
	Span loadingUI;
	@UiField
	Table subscriptionsContainer;
	public interface Binder extends UiBinder<Widget, SubscriptionListWidgetViewImpl> {
	}
	
	Widget w;
	Presenter presenter;
	
	@Inject
	public SubscriptionListWidgetViewImpl(Binder binder){
		this.w = binder.createAndBindUi(this);
		noFilter.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onFilter(null);
			}
		});
		projectFilter.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onFilter(SubscriptionObjectType.FORUM);
			}
		});
		threadFilter.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onFilter(SubscriptionObjectType.DISCUSSION_THREAD);
			}
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
