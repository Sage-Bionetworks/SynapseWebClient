package org.sagebionetworks.web.client.widget.subscription;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.RadioButton;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.repo.model.subscription.SubscriptionObjectType;
import org.sagebionetworks.web.client.view.bootstrap.table.*;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SubscriptionListWidgetViewImpl implements SubscriptionListWidgetView{

	@UiField
	Button moreButton;
	
	@UiField
	RadioButton noFilter;
	@UiField
	RadioButton projectFilter;
	@UiField
	RadioButton threadFilter;
	
	@UiField
	Div synAlertContainer;
	@UiField
	Table subscriptionsContainer;
	public interface Binder extends UiBinder<Widget, SubscriptionListWidgetViewImpl> {
	}
	
	Widget w;
	Presenter presenter;
	
	@Inject
	public SubscriptionListWidgetViewImpl(Binder binder){
		this.w = binder.createAndBindUi(this);
		moreButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onMore();
			}
		});
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
	public void addNewSubscription(Widget subscribeButton, Widget topicWidget) {
		TableRow tr = new TableRow();
		tr.setHeight("45px");
		TableData td = new TableData();
		td.add(subscribeButton);
		td.setWidth("95px");
		tr.add(td);
		
		td = new TableData();
		td.add(topicWidget);
		tr.add(td);
		subscriptionsContainer.add(tr);
	}
	public void clearSubscriptions() {
		subscriptionsContainer.clear();
	};
	@Override
	public void clearFilter() {
		noFilter.setValue(true, false);
		noFilter.setActive(true);
	}
	public void setMoreButtonVisible(boolean visible) {
		moreButton.setVisible(visible);
	};
	
}
