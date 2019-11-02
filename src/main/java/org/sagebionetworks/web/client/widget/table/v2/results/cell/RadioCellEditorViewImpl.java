package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import java.util.ArrayList;
import java.util.List;
import org.gwtbootstrap3.client.ui.Radio;
import org.gwtbootstrap3.client.ui.html.Div;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Radio editor view
 * 
 * @author Jay
 *
 */
public class RadioCellEditorViewImpl implements RadioCellEditorView {

	public interface Binder extends UiBinder<Widget, RadioCellEditorViewImpl> {
	}

	@UiField
	Div container;

	Widget widget;

	List<Radio> radioButtons;
	Integer selectedIndex;

	@Inject
	public RadioCellEditorViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
		radioButtons = new ArrayList<Radio>();
		selectedIndex = null;

	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setValue(Integer value) {
		selectedIndex = value;
		radioButtons.get(value).setValue(true, true);
	}

	@Override
	public Integer getValue() {
		return selectedIndex;
	}

	@Override
	public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
		return null;
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {}

	@Override
	public int getTabIndex() {
		return 0;
	}

	@Override
	public void setAccessKey(char key) {}

	@Override
	public void setFocus(boolean focused) {}

	@Override
	public void setTabIndex(int index) {}

	@Override
	public void configure(List<String> items) {
		container.clear();
		radioButtons.clear();
		String uniqueId = HTMLPanel.createUniqueId();
		for (String item : items) {
			final Radio radio = new Radio(uniqueId, item);
			radio.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					selectedIndex = radioButtons.indexOf(radio);
				}
			});
			radioButtons.add(radio);
			container.add(radio);
		}
	}

}
