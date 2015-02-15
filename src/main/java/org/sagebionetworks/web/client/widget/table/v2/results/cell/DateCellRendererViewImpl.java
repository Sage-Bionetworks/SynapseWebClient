package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import java.util.Date;

import org.gwtbootstrap3.client.ui.html.Text;

import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
/**
 * View implementation with zero business logic.
 * 
 * @author jhill
 *
 */
public class DateCellRendererViewImpl implements DateCellRendererView {
	
	public interface Binder extends UiBinder<Widget, DateCellRendererViewImpl> {}
	
	@UiField
	Text text;

	Widget widget;
	
	DateTimeFormat format;
	
	@Inject
	public DateCellRendererViewImpl(Binder binder){
		widget = binder.createAndBindUi(this);
	}
	
	@Override
	public Date getValue() {
		return format.parse(text.getText());
	}

	@Override
	public void setValue(Date value) {
		text.setText(format.format(value));
	}

	@Override
	public void setFormat(String format) {
		this.format = DateTimeFormat.getFormat(format);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void clear() {
		text.setText("");
	}

}
