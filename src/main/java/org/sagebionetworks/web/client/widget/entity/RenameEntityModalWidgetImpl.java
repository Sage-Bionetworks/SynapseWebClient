package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityTypeUtils;
import org.sagebionetworks.web.client.StringUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.Callback;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * A simple modal dialog for renaming an entity.
 * 
 * @author John
 *
 */
public class RenameEntityModalWidgetImpl implements RenameEntityModalWidget {

	public static final String BUTTON_TEXT = "Rename";
	public static final String LABLE_SUFFIX = " name";
	public static final String TITLE_PREFIX = "Rename ";

	public static final String NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER = "Name must include at least one character.";

	PromptForValuesModalView view;
	SynapseJavascriptClient jsClient;
	String parentId;
	Entity toRename;
	String startingName;
	Callback handler;

	@Inject
	public RenameEntityModalWidgetImpl(PromptForValuesModalView view, SynapseJavascriptClient jsClient) {
		super();
		this.view = view;
		this.jsClient = jsClient;
	}


	/**
	 * Update entity with a new name.
	 * 
	 * @param name
	 */
	private void updateEntity(final String name) {
		view.setLoading(true);
		toRename.setName(name);
		jsClient.updateEntity(toRename, null, null, new AsyncCallback<Entity>() {
			@Override
			public void onSuccess(Entity result) {
				view.hide();
				handler.invoke();
			}

			@Override
			public void onFailure(Throwable caught) {
				// put the name back.
				toRename.setName(startingName);
				view.showError(caught.getMessage());
				view.setLoading(false);
			}
		});
	}

	/**
	 * Should be Called when the rename button is clicked on the dialog.
	 */
	public void onRename(String newName) {
		String name = StringUtils.emptyAsNull(newName);
		if (name == null) {
			view.showError(NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER);
		} else if (this.startingName.equals(name)) {
			// just hide the view if the name has not changed.
			view.hide();
		} else {
			// Create the table
			updateEntity(name);
		}
	}


	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void onRename(Entity toRename, Callback handler) {
		this.handler = handler;
		String typeName = EntityTypeUtils.getDisplayName(EntityTypeUtils.getEntityTypeForClass(toRename.getClass()));
		this.toRename = toRename;
		this.startingName = toRename.getName();
		this.view.clear();
		this.view.configureAndShow(TITLE_PREFIX + typeName, typeName + LABLE_SUFFIX, toRename.getName(), newName -> {
			onRename(newName);
		});
	}


}
