package org.sagebionetworks.web.client.widget.team;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SageImageBundle;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.inject.Inject;

public class WizardProgressWidgetViewImpl extends FlowPanel implements WizardProgressWidgetView {
	
	private SageImageBundle sageImageBundle;
	private Presenter presenter;
	
	@Inject
	public WizardProgressWidgetViewImpl(SageImageBundle sageImageBundle) {
		this.sageImageBundle = sageImageBundle;
		addStyleName("whiteBackground center border-bottom-1");
	}
	
	
	@Override
	public void configure(int current, int total) {
		clear();
		//only show progress if there's more than one page
		if (total > 1) {
			DisplayUtils.show(this);
			for (int i = 0; i < total; i++) {
				if (i == current) {
					//current page
					add(new InlineHTML("<span class=\"badge margin-top-10\" style=\"color: white; background-color: #0d78b6; padding: 3px 5px; box-shadow: 0 0 0 1px #fff, 0 0 0 3px #58585a;\">"+(i+1) +"</span>"));
				} else if (i < current) {
					//page complete
					add(new InlineHTML("<span class=\"badge margin-top-10 movedown-2\" style=\"color: white; background-color: #06944e; padding: 3px 3px; box-shadow: 0 0 0 1px #fff, 0 0 0 3px #58585a;\">"+DisplayUtils.getIcon("glyphicon-ok moveup-2") +"</span>"));
				} else {
					//page incomplete
					add(new InlineHTML("<span class=\"badge margin-top-10\" style=\"color: #58585a; background-color: white; padding: 3px 5px; box-shadow: 0 0 0 1px #fff, 0 0 0 3px #58585a;\">"+(i+1) +"</span>"));
				}
				
				if (i < total-1) {
					Image greyArrow = new Image(sageImageBundle.greyArrow());
					greyArrow.addStyleName("margin-10 moveup-2");
					add(greyArrow);
				}
			}
		} else {
			DisplayUtils.hide(this);
		}
	}
	
	
	@Override
	public void showLoading() {
		clear();
		add(DisplayUtils.getLoadingWidget(sageImageBundle));
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
}
