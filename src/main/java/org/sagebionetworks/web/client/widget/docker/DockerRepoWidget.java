package org.sagebionetworks.web.client.widget.docker;

import java.util.Map;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.EntityMetadata;
import org.sagebionetworks.web.client.widget.entity.ModifiedCreatedByWidget;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.entity.controller.PreflightController;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.file.BasicTitleBar;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
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
	private BasicTitleBar dockerTitleBar;

	@Inject
	public DockerRepoWidget(
			PreflightController preflightController,
			DockerRepoWidgetView view,
			SynapseAlert synAlert,
			WikiPageWidget wikiPageWidget,
			ProvenanceWidget provWidget,
			ActionMenuWidget actionMenu,
			BasicTitleBar dockerTitleBar,
			EntityMetadata metadata,
			ModifiedCreatedByWidget modifiedCreatedBy
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
		view.setPresenter(this);
		view.setSynapseAlert(synAlert.asWidget());
		view.setWikiPage(wikiPageWidget.asWidget());
		view.setProvenance(provWidget.asWidget());
		view.setTitlebar(dockerTitleBar.asWidget());
		view.setEntityMetadata(metadata.asWidget());
		view.setModifiedCreatedBy(modifiedCreatedBy);
	}

	public Widget asWidget() {
		return view.asWidget();
	}

	public void configure(EntityBundle bundle, final EntityUpdatedHandler handler) {
		Entity entity = bundle.getEntity();
		this.handler = handler;
		metadata.setEntityUpdatedHandler(handler);
		metadata.setEntityBundle(bundle, null);
		dockerTitleBar.configure(bundle);
		modifiedCreatedBy.configure(entity.getCreatedOn(), entity.getCreatedBy(), entity.getModifiedOn(), entity.getModifiedBy());
		configureWikiPage(bundle);
		configureProvenance(bundle.getEntity().getId());
		view.setDockerPullCommand(DOCKER_PULL_COMMAND + bundle.getEntity().getName());
	}

	private void configureProvenance(final String entityId) {
		Map<String, String> configMap = ProvenanceWidget.getDefaultWidgetDescriptor();
		configMap.put(WidgetConstants.PROV_WIDGET_ENTITY_LIST_KEY, DisplayUtils.createEntityVersionString(entityId, null));
		provWidget.configure(configMap);
	}

	private void configureWikiPage(EntityBundle bundle) {
		final String entityId = bundle.getEntity().getId();
		final boolean canEdit = bundle.getPermissions().getCanCertifiedUserEdit();
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
