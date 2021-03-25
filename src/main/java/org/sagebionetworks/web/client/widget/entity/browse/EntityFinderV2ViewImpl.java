package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.callback.OnSelectCallback;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.HelpWidget;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityFinderV2ViewImpl implements EntityFinderV2View {


	public interface EntityFinderV2ViewImplUiBinder extends UiBinder<Widget, EntityFinderV2ViewImpl> {
	}

	private static EntityFinderV2ViewImplUiBinder uiBinder = GWT.create(EntityFinderV2ViewImplUiBinder.class);

	private Presenter presenter;

	private AuthenticationController authController;
	private SynapseJSNIUtils jsniUtils;

	// the modal dialog
	private Modal modal;

	@UiField
	ReactComponentDiv entityFinderContainer;

	@UiField
	Heading modalTitle;
	@UiField
	Paragraph promptCopy;


	@UiField
	Button okButton;
	@UiField
	Button cancelButton;
	@UiField
	HelpWidget helpWidget;

	@UiField
	Div synAlertContainer;

	@Inject
	public EntityFinderV2ViewImpl(AuthenticationController authenticationController, SynapseJSNIUtils jsniUtils) {
		this.modal = (Modal) uiBinder.createAndBindUi(this);

		this.authController = authenticationController;
		this.jsniUtils = jsniUtils;

		okButton.addClickHandler(event -> {
			presenter.okClicked();
		});
		okButton.addDomHandler(DisplayUtils.getPreventTabHandler(okButton), KeyDownEvent.getType());
		cancelButton.addClickHandler(event -> {
			this.hide();
		});
	}


	@Override
	public void renderComponent(String initialContainerId, EntityFinderScope initialScope, boolean showVersions, boolean multiSelect, EntityFilter visibleEntityTypes, EntityFilter selectableEntityTypes, EntityFilter visibleTypesInTree, String selectedCopy) {
		entityFinderContainer.clear();
		_showEntityFinderReactComponent(
				entityFinderContainer.getElement(),
				authController.getCurrentUserSessionToken(),
				initialContainerId,
				initialScope.getValue(),
				toJsArray(visibleEntityTypes),
				toJsArray(selectableEntityTypes),
				toJsArray(visibleTypesInTree),
				showVersions,
				multiSelect,
				selectedCopy,
				result -> {
					List<Reference> selected = new ArrayList<>();
					for (int i = 0; i < result.length(); i++) {
						selected.add(convertFromJso(result.get(i)));
					}
					okButton.setEnabled(selected.size() > 0);
					if (multiSelect) {
						presenter.setSelectedEntities(selected);
					} else {
						if (selected.size() > 0) {
							presenter.setSelectedEntity(selected.get(0));
						} else {
							presenter.clearSelectedEntities();
						}
					}
				});
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
	public void clear() {
		entityFinderContainer.clear();
		presenter.clearSelectedEntities();
	}

	@Override
	public void show() {
		// show modal
		modal.show();
		presenter.renderComponent();
		helpWidget.focus();
	}

	@Override
	public void hide() {
		this.jsniUtils.unmountComponentAtNode(entityFinderContainer.getElement());
		modal.hide();
	}

	@Override
	public Widget asWidget() {
		return modal;
	}

	@Override
	public boolean isShowing() {
		return modal.isAttached() && modal.isVisible();
	}

	@Override
	public void setSynAlert(Widget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}

	@Override
	public void setModalTitle(String modalTitle) {
		this.modalTitle.setText(modalTitle);
	}

	@Override
	public void setPromptCopy(String promptCopy) {
		this.promptCopy.setText(promptCopy);
	}

	@Override
	public void setHelpMarkdown(String helpMarkdown) {
		this.helpWidget.setHelpMarkdown(helpMarkdown);
	}

	@Override
	public void setConfirmButtonCopy(String confirmButtonCopy) {
		this.okButton.setText(confirmButtonCopy);
	}

	public static class ReferenceJso extends JavaScriptObject {

		protected ReferenceJso() {}

		public final native String getTargetId() /*-{ return this.targetId }-*/;

		public final native int getTargetVersionNumber() /*-{ return this.targetVersionNumber || -1 }-*/;

	}

	private Reference convertFromJso(ReferenceJso jso) {
		Reference r = new Reference();
		r.setTargetId(jso.getTargetId());
		if (jso.getTargetVersionNumber() == -1) {
			r.setTargetVersionNumber(null);
		} else {
			r.setTargetVersionNumber(Long.valueOf(jso.getTargetVersionNumber()));
		}
		return r;
	}

	private static native void _showEntityFinderReactComponent(Element el, String sessionToken, String initialContainerId, String initialScope, JsArrayString visible, JsArrayString selectable, JsArrayString visibleTypesInTree, boolean showVersions, boolean multiSelect, String selectedCopy, OnSelectCallback onSelectedCallback) /*-{
		try {
			var callback = function(selected) {
				console.log(selected);
				onSelectedCallback.@org.sagebionetworks.web.client.callback.OnSelectCallback::onSelect(Lcom/google/gwt/core/client/JsArray;)(selected)
			};
			var props = {
				sessionToken: sessionToken,
				initialContainerId: initialContainerId,
				initialScope: initialScope,
				showTypes: visible,
				selectableTypes: selectable,
				visibleTypesInTree: visibleTypesInTree,
				selectMultiple: multiSelect,
				onSelectedChange: callback,
				showVersionSelection: showVersions,
			};
			$wnd.ReactDOM.render($wnd.React.createElement(
					$wnd.SRC.SynapseComponents.EntityFinder, props, null),
					el);
		} catch (err) {
			console.error(err);
		}
	}-*/;

	public static JsArrayString toJsArray(EntityFilter filter) {
		JsArrayString jsArrayString = JsArrayString.createArray().cast();
		for (EntityType t : filter.getEntityQueryValues()) {
			jsArrayString.push(t.toString());
		}
		return jsArrayString;
	}

}


