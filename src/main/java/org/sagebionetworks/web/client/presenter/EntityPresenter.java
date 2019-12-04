package org.sagebionetworks.web.client.presenter;


import java.util.List;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Link;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.view.EntityView;
import org.sagebionetworks.web.client.widget.entity.EntityPageTop;
import org.sagebionetworks.web.client.widget.entity.controller.StuAlert;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.team.OpenTeamInvitationsWidget;
import org.sagebionetworks.web.shared.OpenUserInvitationBundle;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.binder.EventHandler;

public class EntityPresenter extends AbstractActivity implements EntityView.Presenter, Presenter<Synapse>, IsWidget {

	private EntityView view;
	private AuthenticationController authenticationController;
	private StuAlert synAlert;
	private String entityId;
	private Long versionNumber;
	private Synapse.EntityArea area;
	private String areaToken;
	private Header headerWidget;
	private EntityPageTop entityPageTop;
	private OpenTeamInvitationsWidget openTeamInvitesWidget;
	private GlobalApplicationState globalAppState;
	private GWTWrapper gwt;
	private SynapseJavascriptClient jsClient;

	@Inject
	public EntityPresenter(EntityView view, EntityPresenterEventBinder entityPresenterEventBinder, GlobalApplicationState globalAppState, AuthenticationController authenticationController, SynapseJavascriptClient jsClient, StuAlert synAlert, EntityPageTop entityPageTop, Header headerWidget, OpenTeamInvitationsWidget openTeamInvitesWidget, GWTWrapper gwt, EventBus eventBus) {
		this.headerWidget = headerWidget;
		this.entityPageTop = entityPageTop;
		this.globalAppState = globalAppState;
		this.openTeamInvitesWidget = openTeamInvitesWidget;
		this.view = view;
		this.synAlert = synAlert;
		this.authenticationController = authenticationController;
		this.jsClient = jsClient;
		this.gwt = gwt;
		clear();
		entityPresenterEventBinder.getEventBinder().bindEventHandlers(this, eventBus);
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		clear();
		// Install the view
		panel.setWidget(view);
		view.setLoadingVisible(true);
	}

	@Override
	public void setPlace(Synapse place) {
		this.entityId = place.getEntityId();
		this.versionNumber = place.getVersionNumber();
		this.area = place.getArea();
		this.areaToken = place.getAreaToken();
		refresh();
	}

	public static boolean isValidEntityId(String entityId) {
		if (entityId == null || entityId.length() == 0 || !entityId.toLowerCase().startsWith("syn")) {
			return false;
		}

		// try to parse the actual syn id
		try {
			Long.parseLong(entityId.substring("syn".length()).trim());
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	@Override
	public void clear() {
		entityPageTop.clearState();
		synAlert.clear();
		openTeamInvitesWidget.clear();
		view.clear();
		view.setAccessDependentMessageVisible(false);
	}

	@Override
	public String mayStop() {
		view.clear();
		return null;
	}

	@Override
	public void refresh() {
		clear();
		headerWidget.refresh();
		// place widgets and configure
		view.setEntityPageTopWidget(entityPageTop);
		view.setOpenTeamInvitesWidget(openTeamInvitesWidget);
		view.setSynAlertWidget(synAlert.asWidget());
		// Hide the view panel contents until async callback completes
		view.setLoadingVisible(true);
		checkEntityIdAndVersion();
	}

	/**
	 * First step of loading the page.  Check for a valid entity ID, and check it's version.
	 * If this is a File and the version number specified is the latest,
	 * then clear the version to allow user to operate on the current FileEntity.
	 */
	public void checkEntityIdAndVersion() {
		// before anything else, figure out the latest version to determine if it should be nulled out here (so user can operate on the latest version of the entity)
		if (isValidEntityId(entityId)) {
			if (versionNumber == null) {
				// version is already null, continue loading...
				getEntityBundleAndLoadPageTop();
			} else {
				jsClient.getEntity(entityId, new AsyncCallback<Entity>() {
					@Override
					public void onFailure(Throwable caught) {
						onError(caught);
					}
					@Override
					public void onSuccess(Entity entity) {
						if (entity instanceof FileEntity) {
							if (versionNumber.equals(((FileEntity)entity).getVersionNumber())) {
								// we've been asked to load the current file version
								versionNumber = null;	
							}
						}
						// continue loading...
						getEntityBundleAndLoadPageTop();
					}
				});
				
			}
		} else {
			// invalid entity detected, indicate that the page was not found
			gwt.scheduleDeferred(new Callback() {
				@Override
				public void invoke() {
					onError(new NotFoundException());
				}
			});
		}
	}
	
	public void getEntityBundleAndLoadPageTop() {
		final AsyncCallback<EntityBundle> callback = new AsyncCallback<EntityBundle>() {
			@Override
			public void onSuccess(EntityBundle bundle) {
				synAlert.clear();
				view.setLoadingVisible(false);
				// Redirect if Entity is a Link
				if (bundle.getEntity() instanceof Link) {
					Reference ref = ((Link) bundle.getEntity()).getLinksTo();
					entityId = null;
					if (ref != null) {
						// redefine where the page is and refresh
						entityId = ref.getTargetId();
						versionNumber = ref.getTargetVersionNumber();
						refresh();
						return;
					} else {
						// show error and then allow entity bundle to go to view
						view.showErrorMessage(DisplayConstants.ERROR_NO_LINK_DEFINED);
					}
				}
				EntityHeader projectHeader = DisplayUtils.getProjectHeader(bundle.getPath());
				if (projectHeader == null) {
					synAlert.showError(DisplayConstants.ERROR_GENERIC_RELOAD);
				} else {
					entityPageTop.configure(bundle, versionNumber, projectHeader, area, areaToken);
					view.setEntityPageTopWidget(entityPageTop);
					view.setEntityPageTopVisible(true);
					headerWidget.configure(projectHeader);
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				onError(caught);
			}
		};

		if (versionNumber == null) {
			jsClient.getEntityBundle(entityId, EntityPageTop.ALL_PARTS_REQUEST, callback);
		} else {
			jsClient.getEntityBundleForVersion(entityId, versionNumber, EntityPageTop.ALL_PARTS_REQUEST, callback);
		}
	}
	
	public void onError(Throwable caught) {
		view.setLoadingVisible(false);
		headerWidget.configure();
		if (caught instanceof NotFoundException) {
			show404();
		} else if (caught instanceof ForbiddenException && authenticationController.isLoggedIn()) {
			show403();
		} else {
			view.clear();
			synAlert.handleException(caught);
		}
	}
	
	public void show403() {
		if (entityId != null) {
			synAlert.show403(entityId);
		}
		view.setLoadingVisible(false);
		view.setEntityPageTopVisible(false);
		// also add the open team invitations widget (accepting may gain access to this project)
		openTeamInvitesWidget.configure(new Callback() {
			@Override
			public void invoke() {
				// when team is updated, refresh to see if we can now access
				refresh();
			}

		}, new CallbackP<List<OpenUserInvitationBundle>>() {

			@Override
			public void invoke(List<OpenUserInvitationBundle> invites) {
				// if there are any, then also add the title text to the panel
				if (invites != null && invites.size() > 0) {
					view.setAccessDependentMessageVisible(true);
				}
			}

		});
		view.setOpenTeamInvitesVisible(true);
	}

	public void show404() {
		synAlert.show404();
		view.setLoadingVisible(false);
		view.setEntityPageTopVisible(false);
		view.setOpenTeamInvitesVisible(false);
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@EventHandler
	public void onEntityUpdatedEvent(EntityUpdatedEvent event) {
		globalAppState.refreshPage();
	}

	// for testing only

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}
}
