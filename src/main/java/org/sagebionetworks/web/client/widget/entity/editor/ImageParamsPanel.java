package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.web.shared.WidgetConstants;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SliderEvent;
import com.extjs.gxt.ui.client.widget.Slider;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.SliderField;
import com.extjs.gxt.ui.client.widget.layout.FormData;

public class ImageParamsPanel extends FormPanel{
	private SimpleComboBox<String> alignmentCombo;
	private Slider scaleSlider;
	
	public ImageParamsPanel() {
		init();
	}
	
	public void init() {
		setHeaderVisible(false);
		setFrame(false);
		setBorders(false);
		setShadow(false);
		setLabelAlign(LabelAlign.LEFT);
		setBodyBorder(false);
		FormData basicFormData = new FormData("-50");
		setFieldWidth(40);
		//and add scale and alignment
		scaleSlider = new Slider();
	    scaleSlider.setMinValue(1);
	    scaleSlider.setMaxValue(200);
	    scaleSlider.setValue(100);
	    scaleSlider.setIncrement(1);
	    final SliderField sf = new SliderField(scaleSlider);
	    sf.setFieldLabel("Scale (100%)");
	    //bug in gxt slider where the message popup is shown far from the slider, and can't seem to hide it
	    scaleSlider.setMessage("{0}%");
	    //update the field label as a workaround
	    scaleSlider.addListener(Events.Change, new Listener<SliderEvent>() {
	    	@Override
	    	public void handleEvent(SliderEvent be) {
	    		sf.setFieldLabel("Scale (" + be.getNewValue() + "%)");
	    	}
		});

	    add(sf, basicFormData);
	    
	    alignmentCombo = new SimpleComboBox<String>();
		alignmentCombo.add(WidgetConstants.FLOAT_NONE);
		alignmentCombo.add(WidgetConstants.FLOAT_LEFT);
		alignmentCombo.add(WidgetConstants.FLOAT_RIGHT);
		alignmentCombo.add(WidgetConstants.FLOAT_CENTER);
		alignmentCombo.setSimpleValue(WidgetConstants.FLOAT_NONE);
		alignmentCombo.setTypeAhead(false);
		alignmentCombo.setEditable(false);
		alignmentCombo.setForceSelection(true);
		alignmentCombo.setTriggerAction(TriggerAction.ALL);
		alignmentCombo.setFieldLabel("Alignment");
		
		add(alignmentCombo, basicFormData);
	}
	
	public String getAlignment() {
		if(alignmentCombo != null && alignmentCombo.getValue() != null)
			return alignmentCombo.getValue().getValue();			
		return null;
	}

	public String getScale() {
		if (scaleSlider != null)
			return Integer.toString(scaleSlider.getValue());
		return null;
	}
	
	public void setAlignment(String alignmentValue) {
		if(alignmentCombo != null && alignmentCombo.getValue() != null)
			alignmentCombo.getValue().setValue(alignmentValue);			
	}

	public void setScale(String scale) {
		if (scaleSlider != null)
			scaleSlider.setValue(Integer.parseInt(scale));
	}
}

