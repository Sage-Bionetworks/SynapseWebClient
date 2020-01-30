package org.sagebionetworks.web.client.widget.entity.editor;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.TextBox;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.SelectedHandler;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProvenanceConfigViewImpl implements ProvenanceConfigView {
	public interface ProvenanceConfigViewImplUiBinder extends UiBinder<Widget, ProvenanceConfigViewImpl> {
	}

	private Presenter presenter;

	EntityFinder entityFinder;

	@UiField
	TextBox entityListField;
	@UiField
	TextBox depthField;
	@UiField
	TextBox displayHeightField;
	@UiField
	CheckBox showExpandCheckbox;
	@UiField
	Button entityFinderButton;

	Widget widget;

	@Inject
	public ProvenanceConfigViewImpl(ProvenanceConfigViewImplUiBinder binder, EntityFinder entityFinder) {
		widget = binder.createAndBindUi(this);
		this.entityFinder = entityFinder;
		initClickHandlers();
	}

	private void initClickHandlers() {
		entityFinderButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				entityFinder.configure(true, new SelectedHandler<Reference>() {
					@Override
					public void onSelected(Reference selected) {
						if (selected.getTargetId() != null) {
							appendEntityListValue(selected);
							entityFinder.hide();
						} else {
							showErrorMessage(DisplayConstants.PLEASE_MAKE_SELECTION);
						}
					}
				});
				entityFinder.show();
			}
		});
	}

	@Override
	public void initView() {
		depthField.setValue("1");
		entityListField.setValue("");
		displayHeightField.setValue("");
	}

	@Override
	public void checkParams() throws IllegalArgumentException {}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void clear() {}

	@Override
	public String getDepth() {
		return depthField.getValue();
	}

	@Override
	public void setDepth(String depth) {
		depthField.setValue(depth);
	}

	@Override
	public String getEntityList() {
		if (entityListField.getValue() != null)
			return entityListField.getValue();
		else
			return null;
	}

	@Override
	public void setEntityList(String entityList) {
		entityListField.setValue(entityList);
	}

	@Override
	public boolean isExpanded() {
		return showExpandCheckbox.getValue();
	}

	@Override
	public void setIsExpanded(boolean b) {
		showExpandCheckbox.setValue(b);
	}

	@Override
	public void setProvDisplayHeight(String provDisplayHeight) {
		displayHeightField.setValue(provDisplayHeight);
	}

	@Override
	public String getProvDisplayHeight() {
		return displayHeightField.getValue();
	}


	/*
	 * Private Methods
	 */
	private void appendEntityListValue(Reference selected) {
		String str = entityListField.getValue();
		if (str == null)
			str = "";
		if (!str.equals(""))
			str += ",";
		str += DisplayUtils.createEntityVersionString(selected);
		entityListField.setValue(str);
	}

}
