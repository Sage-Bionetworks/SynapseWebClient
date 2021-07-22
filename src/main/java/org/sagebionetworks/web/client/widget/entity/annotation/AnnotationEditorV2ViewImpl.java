package org.sagebionetworks.web.client.widget.entity.annotation;

import org.gwtbootstrap3.client.ui.Modal;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.context.SynapseContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactDOM;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.jsinterop.SchemaDrivenAnnotationEditorProps;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AnnotationEditorV2ViewImpl implements AnnotationEditorV2View {
	public interface Binder extends UiBinder<Widget, AnnotationEditorV2ViewImpl> {
	}

	private Presenter presenter;
	private SynapseContextPropsProvider contextPropsProvider;
	private Widget widget;
	@UiField
	ReactComponentDiv reactComponentDiv;
	@UiField
	Modal editModal;

	@Inject
	public AnnotationEditorV2ViewImpl(Binder uiBinder, SynapseContextPropsProvider propsProvider) {
		widget = uiBinder.createAndBindUi(this);
		contextPropsProvider = propsProvider;

	}
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void configure(String entityId) {
		SchemaDrivenAnnotationEditorProps props =
				SchemaDrivenAnnotationEditorProps.create(
						entityId,
						() -> DisplayUtils.showSuccess("Annotations successfully updated.")
				);

		ReactDOM.render(
				React.createElementWithSynapseContext(
						SRC.SynapseComponents.SchemaDrivenAnnotationEditor,
						props,
						contextPropsProvider.getJsInteropContextProps()
				),
				reactComponentDiv.getElement(),
				() -> editModal.show()
		);
	}
}
