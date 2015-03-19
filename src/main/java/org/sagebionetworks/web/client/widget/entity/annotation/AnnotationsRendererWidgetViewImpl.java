package org.sagebionetworks.web.client.widget.entity.annotation;

import java.util.List;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.web.client.view.bootstrap.table.TBody;
import org.sagebionetworks.web.client.view.bootstrap.table.TableData;
import org.sagebionetworks.web.client.view.bootstrap.table.TableRow;
import org.sagebionetworks.web.client.widget.entity.dialog.Annotation;

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
	FlowPanel modalContainer;
	
	public interface Binder extends UiBinder<Widget, AnnotationsRendererWidgetViewImpl> {	}
	
	private Presenter presenter;
	private AnnotationTransformer transformer;
	private Widget widget;
	@Inject
	public AnnotationsRendererWidgetViewImpl(final Binder uiBinder, AnnotationTransformer transformer){
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
	

	public void configure(List<Annotation> annotations) {
		//add a row for each annotation
		noAnnotationsFoundAlert.setVisible(false);
		tableBody.clear();
		tableBody.setVisible(true);
		for (final Annotation row : annotations) {
			TableRow tableRow = new TableRow();
			
			TableData labelCell = new TableData();
			String label = row.getKey();
			labelCell.add(new Text(label));
			tableRow.add(labelCell);
			
			TableData valueCell = new TableData();
			
			String value = SafeHtmlUtils.htmlEscapeAllowEntities(transformer.getFriendlyValues(row));
			valueCell.add(new Text(value));
			tableRow.add(valueCell);
			
			tableBody.add(tableRow);
		}
	}
	
	@Override
	public void setEditButtonVisible(boolean isVisible) {
		editAnnotationsButton.setVisible(isVisible);
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
