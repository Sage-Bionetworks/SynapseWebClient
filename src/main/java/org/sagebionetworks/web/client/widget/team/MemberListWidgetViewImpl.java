package org.sagebionetworks.web.client.widget.team;

import java.util.List;

import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.select.client.ui.Select;
import org.sagebionetworks.repo.model.TeamMember;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.ButtonType;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.UnorderedListPanel;
import org.sagebionetworks.web.client.widget.search.PaginationEntry;
import org.sagebionetworks.web.client.widget.user.BigUserBadge;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class MemberListWidgetViewImpl extends FlowPanel implements	MemberListWidgetView {
	public static final String MEMBER_ACCESS = "Member";
	public static final String ADMIN_ACCESS = "Team Manager";
	
	private static final int MAX_PAGES_IN_PAGINATION = 10;
	private Presenter presenter;
	private SageImageBundle sageImageBundle;
	private PortalGinInjector portalGinInjector;
	private TextBox searchField;
	private SimplePanel memberSearchContainer;
	private FlowPanel mainContainer;
	@Inject
	public MemberListWidgetViewImpl(SageImageBundle sageImageBundle,
			PortalGinInjector portalGinInjector) {
		this.sageImageBundle = sageImageBundle;
		this.portalGinInjector = portalGinInjector;
		memberSearchContainer = new SimplePanel();
		mainContainer = new FlowPanel();
		mainContainer.addStyleName("highlight-box margin-bottom-10");
		mainContainer.getElement().setAttribute("highlight-box-title", DisplayConstants.MEMBERS);
	}
	
	@Override
	public void showLoading() {
		clear();
		add(DisplayUtils.getLoadingWidget(sageImageBundle));
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
		configureSearchBox();
	}

	private void configureSearchBox() {
		memberSearchContainer.clear();
		SimplePanel container;
		Row horizontalTable = new Row();
		
	    searchField = new TextBox();
	    searchField.setWidth("300px");
	    searchField.addStyleName("form-control");
	    searchField.addKeyDownHandler(new KeyDownHandler() {				
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
					presenter.search(searchField.getValue());
	            }					
			}
		});
		Button searchButton = DisplayUtils.createButton("Member Search");
		searchButton.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				presenter.search(searchField.getValue());
			}
		});
		
		// add to table and page
		container = new SimplePanel(searchButton);
		container.addStyleName("right width-150 padding-left-5");
		horizontalTable.add(container);
		container = new SimplePanel(searchField);
		container.addStyleName("right");
		horizontalTable.add(container);
		memberSearchContainer.add(horizontalTable);
	}
	
	
	@Override
	public void configure(List<TeamMember> members, String searchTerm, boolean isAdmin) {
		clear();
		mainContainer.clear();
		if (searchTerm == null)
			searchTerm = "";
		searchField.setValue(searchTerm);
		FlowPanel singleRow = DisplayUtils.createRowContainerFlowPanel();
		add(memberSearchContainer);
		
		for (TeamMember teamMember : members) {
			FlowPanel mediaContainer = new FlowPanel();
			mediaContainer.addStyleName("col-xs-12 col-md-6");
			mediaContainer.setHeight("120px");
			FlowPanel left = new FlowPanel();
			left.addStyleName("col-xs-9 col-sm-10 col-md-11");
			FlowPanel right = new FlowPanel();
			right.addStyleName("col-xs-3 col-sm-2 col-md-1");
			mediaContainer.add(left);
			mediaContainer.add(right);
			final UserGroupHeader member = teamMember.getMember();
			String rowPrincipalId = member.getOwnerId();
			BigUserBadge userBadge = portalGinInjector.getBigUserBadgeWidget();
			userBadge.configure(rowPrincipalId);
			Widget userBadgeView = userBadge.asWidget();
			left.add(userBadgeView);
			if (isAdmin) {
				//add simple combo
				Select combo = getAccessCombo(member.getOwnerId(), teamMember.getIsAdmin());
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
			} else if (teamMember.getIsAdmin()) {
				//otherwise, indicate that this row user is an admin (via label)
				left.add(new HTML("<span class=\"margin-left-15\">"+ADMIN_ACCESS+"</span>"));
			}
			
			singleRow.add(mediaContainer);
		}
		
		mainContainer.add(singleRow);
		add(mainContainer);
		createPagination();
	}
	

	private void createPagination() {
		FlowPanel lc = new FlowPanel();
		UnorderedListPanel ul = new UnorderedListPanel();
		ul.setStyleName("pagination pagination-lg");

		List<PaginationEntry> entries = presenter.getPaginationEntries(MemberListWidget.MEMBER_LIMIT, MAX_PAGES_IN_PAGINATION);
		if(entries != null && entries.size() > 1) {
			for(PaginationEntry pe : entries) {
				if(pe.isCurrent())
					ul.add(createPaginationAnchor(pe.getLabel(), pe.getStart()), "active");
				else
					ul.add(createPaginationAnchor(pe.getLabel(), pe.getStart()));
			}
		}
		
		lc.add(ul);
		if (entries.size() > 1)
			mainContainer.add(lc);
	}
	
	private Select getAccessCombo(final String ownerId, boolean isAdmin) {
		final Select accessCombo = new Select();
		Option memberOption = new Option();
		memberOption.setText(MEMBER_ACCESS);
		accessCombo.add(memberOption);
		
		Option adminOption = new Option();
		adminOption.setText(ADMIN_ACCESS);
		accessCombo.add(adminOption);
		accessCombo.setWidth("140px");
		accessCombo.addChangeHandler(new ChangeHandler() {
			boolean initializing = true;
			@Override
			public void onChange(ChangeEvent event) {
				if (initializing) {
					initializing = false;
					return;
				}
				boolean isAdmin = ADMIN_ACCESS.equals(accessCombo.getValue());
				presenter.setIsAdmin(ownerId, isAdmin);
			}
		});
		accessCombo.setValue(isAdmin ? adminOption : memberOption);
		
		return accessCombo;
	}
	
	private Anchor createPaginationAnchor(String anchorName, final int newStart) {
		Anchor a = new Anchor();
		a.setHTML(anchorName);
		a.addClickHandler(new ClickHandler() {				
			@Override
			public void onClick(ClickEvent event) {
				presenter.jumpToOffset(newStart);
			}
		});
		
		return a;
	}	

}
