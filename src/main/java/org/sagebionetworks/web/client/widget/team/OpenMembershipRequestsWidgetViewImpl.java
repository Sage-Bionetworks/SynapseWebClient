package org.sagebionetworks.web.client.widget.team;

import java.util.List;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Italic;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.view.bootstrap.table.Table;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class OpenMembershipRequestsWidgetViewImpl implements OpenMembershipRequestsWidgetView {

	public interface Binder extends UiBinder<Widget, OpenMembershipRequestsWidgetViewImpl> {
	}

	@UiField
	Heading title;
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
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void clear() {
		mainContainer.clear();
		title.setVisible(false);
	}

	@Override
	public void configure(List<UserProfile> profiles, List<String> requestMessages, List<String> createdOnDates, List<String> requestIds) {
		clear();
		title.setVisible(false);
		mainContainer.setVisible(false);

		Table table = new Table();
		table.setWidth("100%");
		mainContainer.add(table);
		for (int i = 0; i < profiles.size(); i++) {
			OpenMembershipRequestWidget openMembershipRequestWidget = ginInjector.getOpenMembershipRequestWidget();
			UserProfile profile = profiles.get(i);
			UserBadge renderer = ginInjector.getUserBadgeWidget();
			renderer.configure(profile);
			openMembershipRequestWidget.badgeTableData.add(renderer);


			String requestMessage = requestMessages.get(i);
			openMembershipRequestWidget.messageTableData.add(new Text(requestMessage));
			openMembershipRequestWidget.createdOnTableData.add(new Italic(createdOnDates.get(i)));

			final String requestId = requestIds.get(i);
			openMembershipRequestWidget.denyButton.addClickHandler(event -> {
				presenter.deleteRequest(requestId);
			});
			openMembershipRequestWidget.acceptButton.addClickHandler(event -> {
				presenter.acceptRequest(profile.getOwnerId());
			});

			table.add(openMembershipRequestWidget);

			mainContainer.setVisible(true);
			title.setVisible(true);
		}
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
