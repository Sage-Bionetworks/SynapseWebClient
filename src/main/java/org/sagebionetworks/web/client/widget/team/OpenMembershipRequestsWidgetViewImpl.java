package org.sagebionetworks.web.client.widget.team;

import java.util.List;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.ColumnSize;
import org.gwtbootstrap3.client.ui.constants.Pull;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Italic;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.view.bootstrap.table.Table;
import org.sagebionetworks.web.client.view.bootstrap.table.TableData;
import org.sagebionetworks.web.client.view.bootstrap.table.TableRow;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.user.BadgeSize;
import org.sagebionetworks.web.client.widget.user.UserBadge;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class OpenMembershipRequestsWidgetViewImpl implements OpenMembershipRequestsWidgetView {

	public interface Binder extends UiBinder<Widget, OpenMembershipRequestsWidgetViewImpl> {}

	@UiField
	Div mainContainer;
	@UiField
	Div synAlertContainer;
	Widget widget;
	private Presenter presenter;
	private PortalGinInjector ginInjector;
	
	@Inject
	public OpenMembershipRequestsWidgetViewImpl(Binder binder, PortalGinInjector ginInjector) {
		widget = binder.createAndBindUi(this);
		this.ginInjector = ginInjector;
		mainContainer.addStyleName("highlight-box");
		mainContainer.getElement().setAttribute("highlight-box-title", DisplayConstants.PENDING_JOIN_REQUESTS);
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void clear() {
		mainContainer.clear();
	}
	
	@Override
	public void configure(List<UserProfile> profiles, List<String> requestMessages, List<String> createdOnDates, List<String> requestIds) {
		clear();
		mainContainer.setVisible(false);
		Table table = new Table();
		table.setWidth("100%");
		mainContainer.add(table);
		for (int i = 0; i < profiles.size(); i++) {
			TableRow tr = new TableRow();
			table.add(tr);
			if (i < profiles.size() - 1) {
				tr.addStyleName("border-bottom-1");	
			}
			
			final UserProfile profile = profiles.get(i);
			UserBadge renderer = ginInjector.getUserBadgeWidget();
			renderer.configure(profile);
			
			String requestMessage = requestMessages.get(i);
			Div requestMessageDiv = new Div();
			requestMessageDiv.add(new Text(requestMessage));
			
			Div createdOnDiv = new Div();
			createdOnDiv.add(new Italic(createdOnDates.get(i)));
			
			Button deleteButton = new Button("Remove");
			deleteButton.setSize(ButtonSize.LARGE);
			deleteButton.setType(ButtonType.DANGER);
			deleteButton.setMarginRight(5);
			final String requestId = requestIds.get(i);
			deleteButton.addClickHandler(event -> {
				presenter.deleteRequest(requestId);
			});
			Button joinButton = new Button(DisplayConstants.ACCEPT);
			joinButton.setSize(ButtonSize.LARGE);
			joinButton.setType(ButtonType.PRIMARY);
			joinButton.addClickHandler(event -> {
				presenter.acceptRequest(profile.getOwnerId());
			});
			
			TableData td = new TableData();
			td.addStyleName("padding-5");
			td.add(renderer);
			td.add(requestMessageDiv);
			td.add(createdOnDiv);
			tr.add(td);
			
			td = new TableData();
			td.add(deleteButton);
			td.setWidth("100px");
			tr.add(td);
			
			td = new TableData();
			td.add(joinButton);
			td.setWidth("100px");
			tr.add(td);
			
			mainContainer.setVisible(true);
		}
		mainContainer.add(table);
	}
	
	@Override
	public void setSynAlert(IsWidget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}
}
