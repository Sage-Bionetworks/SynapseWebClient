package org.sagebionetworks.web.client.widget.editpanels.phenotype;

import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PhenotypeMatrix implements PhenotypeMatrixView.Presenter, SynapseWidgetPresenter {
	private PhenotypeMatrixView view;
	
	@Inject
	public PhenotypeMatrix(PhenotypeMatrixView view) {
        this.view = view;
        view.setPresenter(this);		
	}
	
	public void setResources() {
		this.view.createWidget();
	}	
	
	public void disable() {
		this.view.disable();
	}
	
	public void enable() {
		this.view.enable();
	}
	
    @Override
	public Widget asWidget() {
   		view.setPresenter(this);
        return view.asWidget();
    }
	
	public void setHeight(int height) {
		view.setHeight(height);
	}
	
	public void setWidth(int width) {
		view.setWidth(width);
	}


}
