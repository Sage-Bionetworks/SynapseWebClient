package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.web.client.ClientLogger;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.widget.entity.row.EntityRowList;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.DomEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.KeyNav;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.TriggerField;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Element;

/**
 * As list editor.
 * 
 * @author John
 * 
 * @param <T>
 */
public class ListFieldEditor extends TriggerField<List<String>> {

	private ListMenu menu;
	EntityRowList<String> rowList;
	IconsImageBundle iconBundle;
	ClientLogger log;

	public ListFieldEditor(EntityRowList<String> rowList, IconsImageBundle iconBundle, ClientLogger log) {
		setTriggerStyle("x-form-date-trigger");
		this.rowList = rowList;
		this.iconBundle = iconBundle;
		this.log = log;
	}

	/**
	 * Returns the field's date picker.
	 * 
	 * @return the date picker
	 */
	public ListEditorGrid<String> getListPicker() {
		if (menu == null) {
			menu = new ListMenu(this.iconBundle, log);
			// When we hide the menu, apply the change.
			menu.addListener(Events.Hide, new Listener<ComponentEvent>() {
				public void handleEvent(ComponentEvent be) {
					applyListFromPicker();
					focus();
				}
			});
		}
		return menu.getListPicker();
	}
	
	// Apply the list from the picker
	public void applyListFromPicker() {
		List<String> value = menu.getList();
		setValue(value);
		rowList.setValue(value);
	}

	
	protected void expand() {
		ListEditorGrid<String> picker = getListPicker();

		List<String> value = getList();
		picker.setList(value, new TextField<String>());

		// handle case when down arrow is opening menu
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				menu.show(el().dom, "tl-bl?");
				menu.getListPicker().focus();
			}
		});
	}

	@Override
	protected void onKeyDown(FieldEvent fe) {
		super.onKeyDown(fe);
		if (fe.getKeyCode() == KeyCodes.KEY_DOWN) {
			fe.stopEvent();
			if (menu == null || !menu.isAttached()) {
				expand();
			}
		}
	}

	@Override
	protected void onRender(Element target, int index) {
		super.onRender(target, index);

		new KeyNav<FieldEvent>(this) {

			@Override
			public void onEsc(FieldEvent fe) {
				if (menu != null && menu.isAttached()) {
					menu.hide();
				}
			}
		};

		if (GXT.isAriaEnabled()) {
			getInputEl().dom.setAttribute("title", "Aria text");
		}
	}

	@Override
	protected void onTriggerClick(ComponentEvent ce) {
		super.onTriggerClick(ce);
		expand();
	}

	@Override
	protected boolean validateBlur(DomEvent e, Element target) {
		return menu == null || (menu != null && !menu.isVisible());
	}
	
	public void setList(List<String> list){
		this.rowList.setValue(list);
	}
	
	public List<String> getList(){
		return this.rowList.getValue();
	}

}
