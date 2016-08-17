package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.ProjectHeader;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.provenance.ProvViewUtil;
import org.sagebionetworks.web.shared.KeyValueDisplay;

import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProjectBadge implements ProjectBadgeView.Presenter, SynapseWidgetPresenter {
	
	private ProjectBadgeView view;
	private ProjectHeader header;
	private FavoriteWidget favoritesWidget;
	private UserProfile modifiedBy;
	private GWTWrapper gwt;
	
	@Inject
	public ProjectBadge(ProjectBadgeView view, 
			FavoriteWidget favoritesWidget,
			GWTWrapper gwt
			) {
		this.view = view;
		this.favoritesWidget = favoritesWidget;
		this.gwt = gwt;
		view.setPresenter(this);
		view.setFavoritesWidget(favoritesWidget.asWidget());
	}
	
	public void configure(ProjectHeader header, UserProfile modifiedBy) {
		this.header = header;
		this.modifiedBy = modifiedBy;
		view.setLastActivityVisible(false);
		if (header != null) {
			if (header.getLastActivity() != null) {
				try {
					String dateString = view.getSimpleDateString(header.getLastActivity());
					view.setLastActivityVisible(true);
					view.setLastActivityText(dateString);
				} catch(Exception e) {};
			}
			favoritesWidget.configure(header.getId());
			view.configure(header.getName(), DisplayUtils.getSynapseHistoryToken(header.getId()), getProjectTooltip());
		}
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}	
	
	@Override
	public String getProjectTooltip() {
		Map<String,String> map = new HashMap<String, String>();
		List<String> order = new ArrayList<String>();
		
		order.add("ID");
		map.put("ID", header.getId());

		if (modifiedBy != null) {
			order.add("Modified By");
			map.put("Modified By", DisplayUtils.getDisplayName(modifiedBy));
		}

		if (header.getModifiedOn() != null) {
			order.add("Modified On");
			map.put("Modified On", gwt.getDateTimeFormat(PredefinedFormat.DATE_TIME_MEDIUM).format(header.getModifiedOn()));		
		}
		
		return ProvViewUtil.createEntityPopoverHtml(new KeyValueDisplay<String>(map, order)).asString();
	}
	
	public void addStyleName(String style) {
		view.addStyleName(style);
	}
	
	public ProjectHeader getHeader() {
		return header;
	}
}
;