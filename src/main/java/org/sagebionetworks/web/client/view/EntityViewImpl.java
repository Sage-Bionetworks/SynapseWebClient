package org.sagebionetworks.web.client.view;

import java.util.List;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.EntityPageTop;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.handlers.AreaChangeHandler;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.team.OpenTeamInvitationsWidget;
import org.sagebionetworks.web.shared.OpenUserInvitationBundle;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityViewImpl extends Composite implements EntityView {
	
	private SageImageBundle sageImageBundle;
	private Widget loadingPanel;

	public interface EntityViewImplUiBinder extends UiBinder<Widget, EntityViewImpl> {}

	@UiField
	SimplePanel header;
	@UiField
	SimplePanel footer;
	@UiField
	SimplePanel entityPageTopPanel;
	@UiField
	SimplePanel synAlertContainer;
	@UiField
	Image entityBackgroundImage;
	
	private Presenter presenter;
	private Header headerWidget;
	private EntityPageTop entityPageTop;
	private Footer footerWidget;
	private OpenTeamInvitationsWidget openTeamInvitesWidget;
	
	@Inject
	public EntityViewImpl(
			EntityViewImplUiBinder binder,
			Header headerWidget,
			Footer footerWidget,
			EntityPageTop entityPageTop,
			SageImageBundle sageImageBundle, 
			OpenTeamInvitationsWidget openTeamInvitesWidget) {		
		initWidget(binder.createAndBindUi(this));

		this.headerWidget = headerWidget;
		this.footerWidget = footerWidget;
		this.entityPageTop = entityPageTop;
		this.sageImageBundle = sageImageBundle;
		this.openTeamInvitesWidget = openTeamInvitesWidget;
		
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());
		// TODO : need to dynamically set the header widget
		//headerWidget.setMenuItemActive(MenuItems.PROJECTS);
	}

	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
		entityPageTop.setEntityUpdatedHandler(new EntityUpdatedHandler() {			
			@Override
			public void onPersistSuccess(EntityUpdatedEvent event) {
				presenter.refresh();
			}
		});
		entityPageTop.setAreaChangeHandler(new AreaChangeHandler() {			
			@Override
			public void areaChanged(EntityArea area, String areaToken) {
				presenter.updateArea(area, areaToken);
			}

			@Override
			public void replaceArea(EntityArea area, String areaToken) {
				presenter.replaceArea(area, areaToken);
			}
		});
		
		header.clear();
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.clear();
		footer.add(footerWidget.asWidget());
		headerWidget.refresh();

		Window.scrollTo(0, 0); // scroll user to top of page
	}

	@Override
	public void setEntityBundle(EntityBundle bundle, Long versionNumber, EntityHeader projectHeader, Synapse.EntityArea area, String areaToken) {
		entityPageTop.clearState();
		entityPageTop.configure(bundle, versionNumber, projectHeader, area, areaToken);
		entityPageTopPanel.setWidget(entityPageTop.asWidget());
		entityPageTop.refresh();
	}
		
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {
		if (loadingPanel == null)
			loadingPanel = DisplayUtils.createFullWidthLoadingPanel(sageImageBundle);
		entityPageTopPanel.setWidget(loadingPanel);
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}
	
	@Override
	public void setSynAlertWidget(Widget synAlert) {
		synAlertContainer.setWidget(synAlert);
	}
	

	@Override
	public void clear() {
		entityPageTop.clearState();
		entityPageTopPanel.clear();
	}

	@Override
	public void show404() {
		entityPageTop.clearState();
		entityPageTopPanel.setWidget(new HTML(DisplayUtils.get404Html()));
	}

	@Override
	public void show403() {
		entityPageTop.clearState();
		FlowPanel panel = new FlowPanel();
		panel.add(new HTML(DisplayUtils.get403Html()));
		final SimplePanel invitesPanel = new SimplePanel();
		panel.add(invitesPanel);
		//also add the open team invitations widget (accepting may gain access to this project)
		Callback callback = new Callback() {
			@Override
			public void invoke() {
				//when team is updated, refresh to see if we can now access
				presenter.refresh();
			}
		};
		CallbackP<List<OpenUserInvitationBundle>> teamInvitationsCallback = new CallbackP<List<OpenUserInvitationBundle>>() {
			
			@Override
			public void invoke(List<OpenUserInvitationBundle> invites) {
				//if there are any, then also add the title text to the panel
				if (invites != null && invites.size() > 0) {
					HTML message = new HTML("<h4>"+DisplayConstants.ACCESS_DEPENDENT_ON_TEAM+"</h4>");
					message.addStyleName("margin-top-100 margin-left-15");
					invitesPanel.setWidget(message);
				}
			}
		};
		openTeamInvitesWidget.configure(callback, teamInvitationsCallback);
		Widget openTeamInvites = openTeamInvitesWidget.asWidget();
		openTeamInvites.addStyleName("margin-left-10 margin-bottom-10 margin-right-10");
		panel.add(openTeamInvites);
		entityPageTopPanel.setWidget(panel);
	}
	
	@Override
	public void setBackgroundImageVisible(boolean isVisible) {
		entityBackgroundImage.setVisible(isVisible);
	}

	@Override
	public void setBackgroundImageUrl(String url) {
		entityBackgroundImage.setUrl(url);
	}
}
