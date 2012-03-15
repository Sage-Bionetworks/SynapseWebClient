package org.sagebionetworks.web.client.widget.entity.row;

import com.extjs.gxt.ui.client.widget.ComponentHelper;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.google.gwt.user.client.Element;

/**
 * A field with a button next to it.
 * 
 * @author jmhill
 *
 * @param <D>
 */
public class ButtonField<D> extends Field<D> {

	protected Field<?> field;
	protected LayoutContainer lc;
	protected Button button;
	
	public ButtonField(Field<?> field, Button button){
		this.field = field;
		this.button = button;
	}

	@Override
	protected void onRender(Element target, int index) {
		lc = new HorizontalPanel();
		lc.add(field);
		lc.add(button);
		lc.render(target, index);
		ComponentHelper.setParent(this, lc);
		setElement(lc.getElement());
	}

}
