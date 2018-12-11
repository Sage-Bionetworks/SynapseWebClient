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

public class PreviewConfigViewImpl implements PreviewConfigView {
	public interface PreviewConfigViewImplUiBinder extends UiBinder<Widget, PreviewConfigViewImpl> {}
	private Presenter presenter;
	
	@UiField
	TextBox entityIdField;
	@UiField
	TextBox versionField;
	@UiField
	Button entityFinderButton;
	
	Widget widget;
	
	@Inject
	public PreviewConfigViewImpl(PreviewConfigViewImplUiBinder binder) {
		widget = binder.createAndBindUi(this);
		initClickHandlers();
	}
	
	private void initClickHandlers() {	
		entityFinderButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onEntityFinderButtonClicked();
			}
		});
	}
	
	@Override
	public void initView() {	
		entityIdField.setValue("");
		versionField.setValue("");
	}

	@Override
	public void checkParams() throws IllegalArgumentException {
	}

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
	public void showLoading() {
	}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void clear() {
	}

	
	@Override
	public String getEntityId() {
		return entityIdField.getValue();
	}
	
	@Override
	public void setEntityId(String entityId) {
		entityIdField.setValue(entityId);
	}
	@Override
	public String getVersion() {
		return versionField.getValue();
	}
	@Override
	public void setVersion(String version) {
		versionField.setValue(version);
	}

}
