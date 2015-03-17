package org.sagebionetworks.web.client.widget.entity.row;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.EntitySchemaCache;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.widget.entity.dialog.Annotation;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Render entity annotations
 *
 */
public class AnnotationsRendererWidget implements AnnotationsRendererWidgetView.Presenter, IsWidget {
	
	private Annotations annotations;
	private AnnotationsRendererWidgetView view;
	EntityUpdatedHandler entityUpdatedHandler;
	/**
	 * 
	 * @param factory
	 * @param cache
	 * @param propertyView
	 */
	@Inject
	public AnnotationsRendererWidget(AnnotationsRendererWidgetView propertyView) {
		super();
		this.view = propertyView;
		this.view.setPresenter(this);
	}
	
	@Override
	public void configure(Annotations annotations, boolean canEdit) {
		this.annotations = annotations;
		view.configure(getAnnotations(annotations));
		view.setEditButtonVisible(canEdit);
	}
	
	public static List<Annotation> getAnnotations(Annotations annotations) {
		annotations.getDoubleAnnotations()
	}
	
	public boolean isEmpty() {
		return annotations.keySet().isEmpty();
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	public void setEntityUpdatedHandler(EntityUpdatedHandler handler) {
		this.entityUpdatedHandler = handler;
	}

	@Override
	public void onEdit() {
		//TODO
	}
}
