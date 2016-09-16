package org.sagebionetworks.web.client.widget.docker;

import java.util.Map;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.docker.DockerRepository;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.EntityMetadata;
import org.sagebionetworks.web.client.widget.entity.ModifiedCreatedByWidget;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.entity.controller.EntityActionController;
import org.sagebionetworks.web.client.widget.entity.controller.PreflightController;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.file.DockerTitleBar;
import org.sagebionetworks.web.client.widget.entity.menu.v2.Action;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget.ActionListener;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidget;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DockerRepoWidget implements DockerRepoWidgetView.Presenter{
	public static final String DOCKER_PULL_COMMAND = "docker pull ";

	private PreflightController preflightController;
	private DockerRepoWidgetView view;
	private SynapseAlert synAlert;
	private WikiPageWidget wikiPageWidget;
	private ProvenanceWidget provWidget;
	private EntityUpdatedHandler handler;
	private ActionMenuWidget actionMenu;
	private EntityMetadata metadata;
	private ModifiedCreatedByWidget modifiedCreatedBy;
	private DockerTitleBar dockerTitleBar;
	private EntityActionController controller;
	private DockerCommitListWidget dockerCommitListWidget;
	private CookieProvider cookies;
	private boolean isAnnotationsShown;
	private boolean canEdit;
	private DockerRepository entity;

	@Inject
	public DockerRepoWidget(
			PreflightController preflightController,
			DockerRepoWidgetView view,
			SynapseAlert synAlert,
			WikiPageWidget wikiPageWidget,
			ProvenanceWidget provWidget,
			ActionMenuWidget actionMenu,
			DockerTitleBar dockerTitleBar,
			EntityMetadata metadata,
			ModifiedCreatedByWidget modifiedCreatedBy,
			EntityActionController controller,
			DockerCommitListWidget dockerCommitListWidget,
			CookieProvider cookies
			) {
		this.preflightController = preflightController;
		this.view = view;
		this.synAlert = synAlert;
		this.wikiPageWidget = wikiPageWidget;
		this.provWidget = provWidget;
		this.actionMenu = actionMenu;
		this.dockerTitleBar = dockerTitleBar;
		this.metadata = metadata;
		this.modifiedCreatedBy = modifiedCreatedBy;
		this.controller = controller;
		this.dockerCommitListWidget = dockerCommitListWidget;
		this.cookies = cookies;
		actionMenu.addControllerWidget(controller.asWidget());
		view.setPresenter(this);
		view.setSynapseAlert(synAlert.asWidget());
		view.setWikiPage(wikiPageWidget.asWidget());
		view.setProvenance(provWidget.asWidget());
		view.setTitlebar(dockerTitleBar.asWidget());
		view.setEntityMetadata(metadata.asWidget());
		view.setModifiedCreatedBy(modifiedCreatedBy);
		view.setActionMenu(actionMenu.asWidget());
		view.setDockerCommitListWidget(dockerCommitListWidget.asWidget());
	}

	public Widget asWidget() {
		return view.asWidget();
	}

	public void configure(EntityBundle bundle, final EntityUpdatedHandler handler) {
		this.entity = (DockerRepository)bundle.getEntity();
		this.handler = handler;
		this.canEdit = bundle.getPermissions().getCanCertifiedUserEdit();
		metadata.setEntityUpdatedHandler(handler);
		metadata.setEntityBundle(bundle, null);
		dockerTitleBar.configure(entity);
		modifiedCreatedBy.configure(entity.getCreatedOn(), entity.getCreatedBy(), entity.getModifiedOn(), entity.getModifiedBy());
		configureWikiPage(bundle);
		configureProvenance(entity.getId());
		configureActionMenu(bundle);
		view.setDockerPullCommand(DOCKER_PULL_COMMAND + entity.getRepositoryName());
		dockerCommitListWidget.configure(entity.getId(), false);
		view.setProvenanceWidgetVisible(DisplayUtils.isInTestWebsite(cookies));
	}

	private void configureActionMenu(EntityBundle bundle) {
		isAnnotationsShown = false;
		actionMenu.addActionListener(Action.TOGGLE_ANNOTATIONS, new ActionListener() {
			@Override
			public void onAction(Action action) {
				isAnnotationsShown = !isAnnotationsShown;
				controller.onAnnotationsToggled(isAnnotationsShown);
				DockerRepoWidget.this.metadata.setAnnotationsVisible(isAnnotationsShown);
			}
		});
		controller.configure(actionMenu, bundle, true, bundle.getRootWikiId(), handler);
		boolean isEditableDockerRepo = entity instanceof DockerRepository && canEdit;
		this.actionMenu.setActionVisible(Action.ADD_COMMIT, isEditableDockerRepo);
		this.actionMenu.setActionVisible(Action.EDIT_WIKI_PAGE, isEditableDockerRepo);
		this.actionMenu.setActionVisible(Action.EDIT_PROVENANCE, isEditableDockerRepo);
		this.actionMenu.setActionVisible(Action.CHANGE_ENTITY_NAME, false);
		this.actionMenu.setActionVisible(Action.MOVE_ENTITY, false);
		this.actionMenu.setActionVisible(Action.DELETE_ENTITY, false);
		this.actionMenu.setActionListener(Action.ADD_COMMIT, new ActionListener() {
			@Override
			public void onAction(Action action) {
				onAddCommit();
			}
		});
	}

	private void onAddCommit() {
		// TODO Auto-generated method stub
		
	}

	private void configureProvenance(final String entityId) {
		Map<String, String> configMap = ProvenanceWidget.getDefaultWidgetDescriptor();
		configMap.put(WidgetConstants.PROV_WIDGET_ENTITY_LIST_KEY, DisplayUtils.createEntityVersionString(entityId, null));
		provWidget.configure(configMap);
	}

	private void configureWikiPage(EntityBundle bundle) {
		final String entityId = bundle.getEntity().getId();
		final WikiPageWidget.Callback wikiCallback = new WikiPageWidget.Callback() {
			@Override
			public void pageUpdated() {
				handler.onPersistSuccess(new EntityUpdatedEvent());
			}
			@Override
			public void noWikiFound() {
			}
		};
		wikiPageWidget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), bundle.getRootWikiId(), null), canEdit, wikiCallback, false);
		CallbackP<String> wikiReloadHandler = new CallbackP<String>(){
			@Override
			public void invoke(String wikiPageId) {
				wikiPageWidget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), wikiPageId, null), canEdit, wikiCallback, false);
			}
		};
		wikiPageWidget.setWikiReloadHandler(wikiReloadHandler);
	}
}
