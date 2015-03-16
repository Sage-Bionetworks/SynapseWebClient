package org.sagebionetworks.web.client.widget.entity.row;

import java.util.List;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.web.client.view.bootstrap.table.TBody;
import org.sagebionetworks.web.client.view.bootstrap.table.TableData;
import org.sagebionetworks.web.client.view.bootstrap.table.TableRow;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
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
	
	public interface Binder extends UiBinder<Widget, AnnotationsRendererWidgetViewImpl> {	}
	
	private Presenter presenter;
	private Widget widget;
	@Inject
	public AnnotationsRendererWidgetViewImpl(final Binder uiBinder){
		widget = uiBinder.createAndBindUi(this);
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
	
	/**
	 * The rows of data to render.
	 * 
	 * @param rows
	 */
	@Override
	public void configure(List<EntityRow<?>> rows) {
		//now add a row for each annotation
		tableBody.clear();
		for (final EntityRow<?> row : rows) {
			TableRow tableRow = new TableRow();
			
			TableData labelCell = new TableData();
			String label = row.getLabel();
			labelCell.add(new Text(label));
			tableRow.add(labelCell);
			
			TableData valueCell = new TableData();
			String value = SafeHtmlUtils.htmlEscapeAllowEntities(row.getDislplayValue());
			valueCell.add(new Text(value));
			tableRow.add(valueCell);
			
			tableBody.add(tableRow);
		}
	}
	
	@Override
	public void setEditButtonVisible(boolean isVisible) {
		editAnnotationsButton.setVisible(isVisible);
	}
}
