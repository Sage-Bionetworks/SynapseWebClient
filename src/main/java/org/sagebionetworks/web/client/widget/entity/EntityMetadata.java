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
	private DoiWidget doiWidget;
	private FileHistoryWidget fileHistoryWidget;
	private RestrictionWidget restrictionWidget;
	
	@Inject
	public EntityMetadata(EntityMetadataView view, 
			DoiWidget doiWidget,
			AnnotationsRendererWidget annotationsWidget,
			RestrictionWidget restrictionWidget,
			FileHistoryWidget fileHistoryWidget) {
		this.view = view;
		this.doiWidget = doiWidget;
		this.annotationsWidget = annotationsWidget;
		this.fileHistoryWidget = fileHistoryWidget;
		this.restrictionWidget = restrictionWidget;
		this.view.setDoiWidget(doiWidget);
		this.view.setAnnotationsRendererWidget(annotationsWidget);
		this.view.setFileHistoryWidget(fileHistoryWidget);
		this.view.setRestrictionWidget(restrictionWidget);
	}
	
	public Widget asWidget() {
		return view.asWidget();
	}
	
	public void setEntityBundle(EntityBundle bundle, Long versionNumber) {
		clear();
		Entity en = bundle.getEntity();
		view.setEntityId(en.getId());
		boolean canEdit = bundle.getPermissions().getCanCertifiedUserEdit();
		boolean showDetailedMetadata = true;
		if (bundle.getEntity() instanceof FileEntity) {
			fileHistoryWidget.setEntityBundle(bundle, versionNumber);
			fileHistoryWidget.setEntityUpdatedHandler(entityUpdatedHandler);
			view.setFileHistoryWidget(fileHistoryWidget);
			view.setRestrictionPanelVisible(true);
		} else {
			view.setRestrictionPanelVisible(en instanceof TableEntity
					|| en instanceof Folder);
		}
		restrictionWidget.configure(bundle, true, false, true, new Callback() {
			@Override
			public void invoke() {
				fireEntityUpdatedEvent();
			}
		});
		doiWidget.configure(bundle.getDoi(), en.getId());
		annotationsWidget.configure(bundle, canEdit);
		view.setDetailedMetadataVisible(showDetailedMetadata);
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
