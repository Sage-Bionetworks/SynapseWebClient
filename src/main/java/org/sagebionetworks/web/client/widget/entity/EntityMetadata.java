package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.EntityMetadataView.Presenter;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationsRendererWidget;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityMetadata implements Presenter {

	private EntityMetadataView view;
	private EntityUpdatedHandler entityUpdatedHandler;
	private AnnotationsRendererWidget annotationsWidget;
	private FavoriteWidget favoriteWidget;
	private DoiWidget doiWidget;
	private FileHistoryWidget fileHistoryWidget;
	private RestrictionWidget restrictionWidget;
	
	@Inject
	public EntityMetadata(EntityMetadataView view, 
			FavoriteWidget favoriteWidget,
			DoiWidget doiWidget,
			AnnotationsRendererWidget annotationsWidget,
			RestrictionWidget restrictionWidget,
			FileHistoryWidget fileHistoryWidget) {
		this.view = view;
		this.favoriteWidget = favoriteWidget;
		this.doiWidget = doiWidget;
		this.annotationsWidget = annotationsWidget;
		this.fileHistoryWidget = fileHistoryWidget;
		this.restrictionWidget = restrictionWidget;
		this.view.setFavoriteWidget(favoriteWidget);
		this.view.setDoiWidget(doiWidget);
		this.view.setAnnotationsRendererWidget(annotationsWidget);
		this.view.setFileHistoryWidget(fileHistoryWidget);
	}
	
	public Widget asWidget() {
		return view.asWidget();
	}
	
	public void setEntityBundle(EntityBundle bundle, Long versionNumber) {
		clear();
		Entity en = bundle.getEntity();
		boolean canEdit = bundle.getPermissions().getCanCertifiedUserEdit();
		boolean showDetailedMetadata = true;
		boolean showEntityName = true;
		view.setEntityId(en.getId());
		view.setEntityName(en.getName());
		view.getAndSetEntityIcon(en);
		if (bundle.getEntity() instanceof FileEntity) {
			showEntityName = false;
			fileHistoryWidget.setEntityBundle(bundle, versionNumber);
			fileHistoryWidget.setEntityUpdatedHandler(entityUpdatedHandler);
			view.setFileHistoryWidget(fileHistoryWidget);
			view.setFileHistoryVisible(versionNumber != null);
			view.setRestrictionPanelVisible(true);
		} else {
			view.setFileHistoryVisible(false);
			view.setRestrictionPanelVisible(en instanceof TableEntity
					|| en instanceof Folder);
		}
		restrictionWidget.configure(bundle, true, false, true, new Callback() {
			@Override
			public void invoke() {
				fireEntityUpdatedEvent();
			}
		});
		this.view.setRestrictionWidget(restrictionWidget);
		favoriteWidget.configure(bundle.getEntity().getId());
		doiWidget.configure(bundle.getEntity().getId(), bundle.getPermissions().getCanCertifiedUserEdit(), versionNumber);
		annotationsWidget.configure(bundle, canEdit);
		view.setDetailedMetadataVisible(showDetailedMetadata);
		view.setEntityNameVisible(showEntityName);		
	}	

	@Override
	public void fireEntityUpdatedEvent() {
		if (entityUpdatedHandler != null)
			entityUpdatedHandler.onPersistSuccess(new EntityUpdatedEvent());
	}
	
	public void setEntityUpdatedHandler(EntityUpdatedHandler handler) {
		this.entityUpdatedHandler = handler;
		this.annotationsWidget.setEntityUpdatedHandler(entityUpdatedHandler);
	}

	public void setAnnotationsVisible(boolean visible) {
		view.setAnnotationsVisible(visible);
	}
	
	public void setFileHistoryVisible(boolean visible) {
		view.setFileHistoryVisible(visible);
	}
	
	public void clear() {
		doiWidget.clear();
		view.clear();
	}
	
}
