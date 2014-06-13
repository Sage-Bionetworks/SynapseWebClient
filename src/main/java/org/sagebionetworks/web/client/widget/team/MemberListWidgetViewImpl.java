package org.sagebionetworks.web.client.widget.team;

import java.util.List;

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

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
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
	public static final String ADMIN_ACCESS = "Admin";
	
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
		mainContainer.addStyleName("highlight-box");
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
		LayoutContainer horizontalTable = new LayoutContainer();
		horizontalTable.addStyleName("row");
		
	    searchField = new TextBox();
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
		container.addStyleName("padding-right-5 col-md-3 right");
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
		
		add(memberSearchContainer);
		for (TeamMember teamMember : members) {
			LayoutContainer rowPanel = DisplayUtils.createRowContainer();
			rowPanel.addStyleName("col-md-12");
			LayoutContainer left = new LayoutContainer();
			left.addStyleName("col-xs-9 col-sm-10 col-md-11");
			LayoutContainer right = new LayoutContainer();
			right.addStyleName("col-xs-3 col-sm-2 col-md-1");
			rowPanel.add(left);
			rowPanel.add(right);
			final UserGroupHeader member = teamMember.getMember();
			String rowPrincipalId = member.getOwnerId();
			BigUserBadge userBadge = portalGinInjector.getBigUserBadgeWidget();
			userBadge.configure(rowPrincipalId);
			Widget userBadgeView = userBadge.asWidget();
			left.add(userBadgeView);
			if (isAdmin) {
				//add simple combo
				SimpleComboBox combo = getAccessCombo(member.getOwnerId(), teamMember.getIsAdmin());
				SimplePanel wrap = new SimplePanel();
				combo.setWidth(70);
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
				left.add(new HTML("<span class=\"margin-left-15\">Admin</span>"));
			}
			
			mainContainer.add(rowPanel);
		}
		
		add(mainContainer);
		createPagination();
	}
	

	private void createPagination() {
		LayoutContainer lc = new LayoutContainer();
		UnorderedListPanel ul = new UnorderedListPanel();
		ul.setStyleName("pagination pagination-lg");

		List<PaginationEntry> entries = presenter.getPaginationEntries(MemberListWidget.MEMBER_LIMIT, MAX_PAGES_IN_PAGINATION);
		if(entries != null && entries.size() > 1) {
			for(PaginationEntry pe : entries) {
				if(pe.isCurrent())
					ul.add(createPaginationAnchor(pe.getLabel(), pe.getStart()), "current");
				else
					ul.add(createPaginationAnchor(pe.getLabel(), pe.getStart()));
			}
		}
		
		lc.add(ul);
		if (entries.size() > 1)
			mainContainer.add(lc);
	}
	
	private SimpleComboBox getAccessCombo(final String ownerId, boolean isAdmin) {
		final SimpleComboBox accessCombo = new SimpleComboBox<String>();
		accessCombo.setTypeAhead(false);
		accessCombo.setEditable(false);
		accessCombo.setForceSelection(true);
		accessCombo.setTriggerAction(TriggerAction.ALL);
		accessCombo.add(MEMBER_ACCESS);
		accessCombo.add(ADMIN_ACCESS);
		String currentValue = isAdmin ? ADMIN_ACCESS : MEMBER_ACCESS;
		accessCombo.setSimpleValue(currentValue);
		
		accessCombo.addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<String>>() {				
			@Override
			public void selectionChanged(SelectionChangedEvent<SimpleComboValue<String>> se) {
				boolean isAdmin = ADMIN_ACCESS.equals(accessCombo.getSimpleValue());
				presenter.setIsAdmin(ownerId, isAdmin);
			}
		});
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
