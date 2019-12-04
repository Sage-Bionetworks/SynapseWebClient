package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.List;
import java.util.Map;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.SelectedHandler;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFilter;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SynapseFormConfigEditor implements SynapseFormConfigView.Presenter, WidgetEditorPresenter {

	private SynapseFormConfigView view;
	private Map<String, String> descriptor;
	EntityFinder entityFinder;

	@Inject
	public SynapseFormConfigEditor(SynapseFormConfigView view, EntityFinder entityFinder) {
		this.view = view;
		this.entityFinder = entityFinder;
		view.setPresenter(this);
		view.initView();

		configureEntityFinder();
	}

	private void configureEntityFinder() {
		entityFinder.configure(EntityFilter.PROJECT_OR_TABLE, true, new SelectedHandler<Reference>() {
			@Override
			public void onSelected(Reference selected) {
				view.setEntityId(selected.getTargetId());
				entityFinder.hide();
			}
		});
	}

	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, DialogCallback dialogCallback) {
		descriptor = widgetDescriptor;

		if (descriptor.get(WidgetConstants.TABLE_ID_KEY) != null) {
			view.setEntityId(descriptor.get(WidgetConstants.TABLE_ID_KEY));
		}
	}

	public void clearState() {
		view.clear();
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void updateDescriptorFromView() {
		// update widget descriptor from the view
		String entityId = view.getEntityId();
		if (!DisplayUtils.isDefined(entityId))
			throw new IllegalArgumentException(DisplayConstants.INVALID_SELECTION);
		descriptor.clear();
		descriptor.put(WidgetConstants.TABLE_ID_KEY, entityId);
	}

	@Override
	public void onEntityFinderButtonClicked() {
		entityFinder.show();
	}

	@Override
	public String getTextToInsert() {
		return null;
	}

	@Override
	public List<String> getNewFileHandleIds() {
		return null;
	}

	@Override
	public List<String> getDeletedFileHandleIds() {
		return null;
	}
}
