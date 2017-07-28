package org.sagebionetworks.web.client.widget.team;

import java.util.List;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.Pull;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Italic;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.repo.model.MembershipInvtnSubmission;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.UnorderedListPanel;
import org.sagebionetworks.web.client.view.bootstrap.table.Table;
import org.sagebionetworks.web.client.view.bootstrap.table.TableData;
import org.sagebionetworks.web.client.view.bootstrap.table.TableRow;
import org.sagebionetworks.web.client.widget.user.UserBadge;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class OpenUserInvitationsWidgetViewImpl implements OpenUserInvitationsWidgetView {
	public interface Binder extends UiBinder<Widget, OpenUserInvitationsWidgetViewImpl> {}

	@UiField
	Div mainContainer;
	@UiField
	Div synAlertContainer;
	Widget widget;
	
	private Presenter presenter;
	private PortalGinInjector ginInjector;
	private UnorderedListPanel ulPanel;
	
	@Inject
	public OpenUserInvitationsWidgetViewImpl(
			Binder binder, 
			PortalGinInjector ginInjector) {
		widget = binder.createAndBindUi(this);
		this.ginInjector = ginInjector;
		mainContainer.addStyleName("highlight-box");
		mainContainer.getElement().setAttribute("highlight-box-title", DisplayConstants.PENDING_INVITATIONS);
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void configure(List<UserProfile> profiles, List<MembershipInvtnSubmission> invitations, List<String> createdOnDates) {
		clear();
		Table table = new Table();
		table.setWidth("100%");
		
		mainContainer.setVisible(false);
		mainContainer.add(table);
		for (int i = 0; i < profiles.size(); i++) {
			TableRow tr = new TableRow();
			table.add(tr);
			if (i < profiles.size() - 1) {
				tr.addStyleName("border-bottom-1");	
			}
			
			final UserProfile profile = profiles.get(i);
			UserBadge renderer = ginInjector.getUserBadgeWidget();
			MembershipInvtnSubmission invite = invitations.get(i);
			final String inviteId = invite.getId();
			String inviteMessage = invite.getMessage() != null ? invite.getMessage() : "";
			String createdOn = createdOnDates.get(i);
			
			renderer.configure(profile);
			
			Div inviteDiv = new Div();
			inviteDiv.add(new Text(inviteMessage));
			
			Div createdOnDiv = new Div();
			createdOnDiv.add(new Italic(createdOn));
			
			//Remove invitation button
			Button leaveButton = new Button("Remove");
			leaveButton.setType(ButtonType.DANGER);
			leaveButton.setSize(ButtonSize.EXTRA_SMALL);
			leaveButton.setPull(Pull.RIGHT);
			leaveButton.addClickHandler(new ClickHandler() {			
				@Override
				public void onClick(ClickEvent event) {
					presenter.removeInvitation(inviteId);
				}
			});
			
			TableData td = new TableData();
			td.addStyleName("padding-5");
			td.add(renderer);
			td.add(inviteDiv);
			td.add(createdOnDiv);
			tr.add(td);
			
			td = new TableData();
			td.add(leaveButton);
			tr.add(td);
			
			mainContainer.setVisible(true);
		}
		ulPanel = new UnorderedListPanel();
		if (profiles.size() > 0) {
			ulPanel.addStyleName("pager");
			Anchor moreLink = new Anchor("More");
			moreLink.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					presenter.getNextBatch();
				}
			});
			ulPanel.add(moreLink);
			ulPanel.setVisible(false);
			mainContainer.add(ulPanel);
		}
	}
	
	@Override
	public void setMoreResultsVisible(boolean isVisible) {
		if (ulPanel != null)
			ulPanel.setVisible(isVisible);
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
	@Override
	public void clear() {
		mainContainer.clear();
	}
}
