package org.sagebionetworks.web.client.widget.entity.annotation;

import java.util.Map;
import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.repo.model.annotation.v2.AnnotationsValue;
import org.sagebionetworks.web.client.view.bootstrap.table.TBody;
import org.sagebionetworks.web.client.view.bootstrap.table.TableData;
import org.sagebionetworks.web.client.view.bootstrap.table.TableRow;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * A widget that renders entity annotations.
 *
 */
public class AnnotationsRendererWidgetViewImpl implements AnnotationsRendererWidgetView {
	@UiField
	Button editAnnotationsButton;
	@UiField
	TBody tableBody;
	@UiField
	Alert noAnnotationsFoundAlert;
	@UiField
	Span clickEditText;

	@UiField
	FlowPanel modalContainer;

	public interface Binder extends UiBinder<Widget, AnnotationsRendererWidgetViewImpl> {
	}

	private Presenter presenter;
	private AnnotationTransformer transformer;
	private Widget widget;

	@Inject
	public AnnotationsRendererWidgetViewImpl(final Binder uiBinder, AnnotationTransformer transformer) {
		widget = uiBinder.createAndBindUi(this);
		this.transformer = transformer;
		editAnnotationsButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onEdit();
			}
		});
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public Widget asWidget() {
		return widget;
	}


	public void configure(Map<String, AnnotationsValue> annotationsMap) {
		// add a row for each annotation
		noAnnotationsFoundAlert.setVisible(false);
		tableBody.clear();
		tableBody.setVisible(true);
		for (final String label : annotationsMap.keySet()) {
			AnnotationsValue annotationsValue = annotationsMap.get(label);
			TableRow tableRow = new TableRow();

			TableData labelCell = new TableData();
			labelCell.add(new Text(label));
			tableRow.add(labelCell);

			TableData valueCell = new TableData();

			String value = SafeHtmlUtils.htmlEscapeAllowEntities(transformer.getFriendlyValues(annotationsValue));
			valueCell.add(new Text(value));
			tableRow.add(valueCell);

			tableBody.add(tableRow);
		}
	}

	@Override
	public void setEditUIVisible(boolean isVisible) {
		editAnnotationsButton.setVisible(isVisible);
		clickEditText.setVisible(isVisible);
	}

	@Override
	public void showNoAnnotations() {
		tableBody.setVisible(false);
		noAnnotationsFoundAlert.setVisible(true);
	}

	@Override
	public void addEditorToPage(Widget editorWidget) {
		modalContainer.add(editorWidget);
	}
}
