package org.sagebionetworks.web.client.widget.docker;

import java.util.Map;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.docker.DockerRepository;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.EntityMetadata;
import org.sagebionetworks.web.client.widget.entity.ModifiedCreatedByWidget;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.entity.file.DockerTitleBar;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidget;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DockerRepoWidget {
	public static final String DOCKER_PULL_COMMAND = "docker pull ";

	private DockerRepoWidgetView view;
	private WikiPageWidget wikiPageWidget;
	private ProvenanceWidget provWidget;
	private EntityMetadata metadata;
	private ModifiedCreatedByWidget modifiedCreatedBy;
	private DockerTitleBar dockerTitleBar;
	private DockerCommitListWidget dockerCommitListWidget;
	private CookieProvider cookies;
	private boolean canEdit;
	private DockerRepository entity;
	private EventBus eventBus;

	@Inject
	public DockerRepoWidget(DockerRepoWidgetView view, WikiPageWidget wikiPageWidget, ProvenanceWidget provWidget, DockerTitleBar dockerTitleBar, EntityMetadata metadata, ModifiedCreatedByWidget modifiedCreatedBy, DockerCommitListWidget dockerCommitListWidget, CookieProvider cookies, EventBus eventBus) {
		this.view = view;
		this.wikiPageWidget = wikiPageWidget;
		this.provWidget = provWidget;
		this.dockerTitleBar = dockerTitleBar;
		this.metadata = metadata;
		this.modifiedCreatedBy = modifiedCreatedBy;
		this.dockerCommitListWidget = dockerCommitListWidget;
		this.cookies = cookies;
		this.eventBus = eventBus;
		view.setWikiPage(wikiPageWidget.asWidget());
		view.setProvenance(provWidget.asWidget());
		view.setTitlebar(dockerTitleBar.asWidget());
		view.setEntityMetadata(metadata.asWidget());
		view.setModifiedCreatedBy(modifiedCreatedBy);
		view.setDockerCommitListWidget(dockerCommitListWidget.asWidget());
	}

	public Widget asWidget() {
		return view.asWidget();
	}

	public void configure(EntityBundle bundle, ActionMenuWidget actionMenu) {
		this.entity = (DockerRepository) bundle.getEntity();
		this.canEdit = bundle.getPermissions().getCanCertifiedUserEdit();
		metadata.configure(bundle, null, actionMenu);
		dockerTitleBar.configure(entity);
		modifiedCreatedBy.configure(entity.getCreatedOn(), entity.getCreatedBy(), entity.getModifiedOn(), entity.getModifiedBy());
		configureWikiPage(bundle);
		configureProvenance(entity.getId());
		view.setDockerPullCommand(DOCKER_PULL_COMMAND + entity.getRepositoryName());
		dockerCommitListWidget.configure(entity.getId(), false);
		view.setProvenanceWidgetVisible(DisplayUtils.isInTestWebsite(cookies));
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
				eventBus.fireEvent(new EntityUpdatedEvent());
			}

			@Override
			public void noWikiFound() {}
		};
		wikiPageWidget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), bundle.getRootWikiId(), null), canEdit, wikiCallback);
		CallbackP<String> wikiReloadHandler = new CallbackP<String>() {
			@Override
			public void invoke(String wikiPageId) {
				wikiPageWidget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), wikiPageId, null), canEdit, wikiCallback);
			}
		};
		wikiPageWidget.setWikiReloadHandler(wikiReloadHandler);
	}
}
