package org.sagebionetworks.web.client.widget.team;

import java.util.List;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.ButtonType;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.user.BadgeSize;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.TeamMemberBundle;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class MemberListWidgetViewImpl extends FlowPanel implements	MemberListWidgetView {
	public static final String MEMBER_ACCESS = "Member";
	public static final String ADMIN_ACCESS = "Team Manager";
	
	private Presenter presenter;
	private PortalGinInjector portalGinInjector;
	private SimplePanel loadMoreWidgetContainer;
	private LoadMoreWidgetContainer loadMoreWidget;
	
	@Inject
	public MemberListWidgetViewImpl(SageImageBundle sageImageBundle,
			PortalGinInjector portalGinInjector) {
		this.portalGinInjector = portalGinInjector;
		loadMoreWidgetContainer = new SimplePanel();
		add(loadMoreWidgetContainer);
	}
	@Override
	public void setMembersContainer(LoadMoreWidgetContainer loadMoreWidget) {
		this.loadMoreWidget = loadMoreWidget;
		loadMoreWidgetContainer.clear();
		loadMoreWidgetContainer.add(loadMoreWidget);
	}
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public void addMembers(List<TeamMemberBundle> members, boolean isAdmin) {
		FlowPanel singleRow = DisplayUtils.createRowContainerFlowPanel();
		
		for (TeamMemberBundle teamMember : members) {
			FlowPanel mediaContainer = new FlowPanel();
			mediaContainer.addStyleName("col-xs-12 col-md-6");
			mediaContainer.setHeight("120px");
			FlowPanel left = new FlowPanel();
			left.addStyleName("col-xs-9 col-sm-10 col-md-11");
			FlowPanel right = new FlowPanel();
			right.addStyleName("col-xs-3 col-sm-2 col-md-1");
			mediaContainer.add(left);
			mediaContainer.add(right);
			final UserProfile member = teamMember.getUserProfile();
			UserBadge userBadge = portalGinInjector.getUserBadgeWidget();
			userBadge.configure(member);
			userBadge.setSize(BadgeSize.LARGER);
			Widget userBadgeView = userBadge.asWidget();
			left.add(userBadgeView);
			
			if (isAdmin) {
				//add simple combo
				ListBox combo = getAccessCombo(member.getOwnerId(), teamMember.getIsTeamAdmin());
				SimplePanel wrap = new SimplePanel();
				wrap.addStyleName("margin-top-5");
				wrap.add(combo);
				left.add(wrap);
				
				//add delete member button
				Button leaveButton = DisplayUtils.createButton("Remove", ButtonType.DANGER);
				leaveButton.addStyleName("pull-right margin-left-5");
				leaveButton.addClickHandler(new ClickHandler() {			
					@Override
					public void onClick(ClickEvent event) {
						DisplayUtils.showConfirmDialog("Remove Member?", DisplayUtils.getDisplayName(member) + DisplayConstants.PROMPT_SURE_REMOVE_MEMBER, 
								new Callback() {
									@Override
									public void invoke() {
										presenter.removeMember(member.getOwnerId());
									}
								});
					}
				});
				right.add(leaveButton);
			} else if (teamMember.getIsTeamAdmin()) {
				//otherwise, indicate that this row user is an admin (via label)
				left.add(new HTML("<span>"+ADMIN_ACCESS+"</span>"));
			}
			
			singleRow.add(mediaContainer);
		}
		
		loadMoreWidget.add(singleRow);
		// SWC-4280: after attaching to the DOM, reference the element offset height in an attempt to force layout calculation (reflow):
		singleRow.getOffsetHeight();
	}
	
	@Override
	public void clearMembers() {
		loadMoreWidget.clear();
	}
	
	private ListBox getAccessCombo(final String ownerId, boolean isAdmin) {
		final ListBox accessCombo = new ListBox();
		accessCombo.setStyleName("form-control");
		accessCombo.addItem(MEMBER_ACCESS);
		accessCombo.addItem(ADMIN_ACCESS);
		accessCombo.setWidth("150px");
		accessCombo.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				boolean isAdmin = accessCombo.getSelectedIndex() == 1;
				presenter.setIsAdmin(ownerId, isAdmin);
			}
		});
		accessCombo.setSelectedIndex(isAdmin ? 1 : 0);
		
		return accessCombo;
	}
	
	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}
	
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
}
