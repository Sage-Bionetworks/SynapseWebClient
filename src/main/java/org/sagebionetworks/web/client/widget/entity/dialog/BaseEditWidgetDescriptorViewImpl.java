package org.sagebionetworks.web.client.widget.entity.dialog;

import java.util.List;
import java.util.Map;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class BaseEditWidgetDescriptorViewImpl implements BaseEditWidgetDescriptorView {
	public interface Binder extends UiBinder<Widget, BaseEditWidgetDescriptorViewImpl> {}
	
	//the modal
	Modal modal;

	@UiField
	SimplePanel paramsPanel;
	@UiField
	Button okButton;
	
	private Presenter presenter;
	private WidgetEditorPresenter widgetDescriptorPresenter;
	private WidgetRegistrar widgetRegistrar;
	private DialogCallback dialogCallback;
	
	@Inject
	public BaseEditWidgetDescriptorViewImpl(Binder binder, WidgetRegistrar widgetRegistrar) {
		this.modal = (Modal)binder.createAndBindUi(this);
		this.widgetRegistrar = widgetRegistrar;
		okButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.apply();
			}
		});
		
		dialogCallback = new DialogCallback() {
			@Override
			public void setPrimaryEnabled(boolean enable) {
				okButton.setEnabled(enable);
			}
		};
	}
	
	@Override
	public void show() {
		if (widgetDescriptorPresenter != null) {
			modal.show();
		} else {
			//widget editor presenter not found for this content type
			DisplayUtils.showErrorMessage("No editor was found for the selected widget.");
		}
	}

	@Override
	public void hide() {
		modal.hide();
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
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}
	
	@Override
	public void setWidgetDescriptor(WikiPageKey wikiKey, String contentTypeKey, Map<String, String> widgetDescriptor, boolean isWiki) {
		//clear out params panel.  Get the right params editor based on the descriptor (it's concrete class, and configure based on the parameters inside of it).
		okButton.setEnabled(true);
		paramsPanel.clear();
		widgetDescriptorPresenter = widgetRegistrar.getWidgetEditorForWidgetDescriptor(wikiKey, contentTypeKey, widgetDescriptor, isWiki, dialogCallback);
		if (widgetDescriptorPresenter != null) {
			Widget w = widgetDescriptorPresenter.asWidget();
			paramsPanel.add(w);
			//finish setting up the main dialog
			String friendlyName = widgetRegistrar.getFriendlyTypeName(contentTypeKey);
			modal.setTitle(friendlyName);
		} else {
			showErrorMessage("No editor found for the content type: " + contentTypeKey);
		}
	}
	
	@Override
	public String getTextToInsert() {
		return widgetDescriptorPresenter.getTextToInsert();
	}

	@Override
	public void updateDescriptorFromView() {
		widgetDescriptorPresenter.updateDescriptorFromView();
	}
	
	@Override
	public List<String> getNewFileHandleIds() {
		return widgetDescriptorPresenter.getNewFileHandleIds();
	}
	
	@Override
	public void clear() {
	}
}
