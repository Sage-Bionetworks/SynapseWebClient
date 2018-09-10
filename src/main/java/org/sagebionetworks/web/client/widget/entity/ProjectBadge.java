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
import org.sagebionetworks.web.client.widget.asynch.UserProfileAsyncHandler;
import org.sagebionetworks.web.client.widget.provenance.ProvViewUtil;
import org.sagebionetworks.web.shared.KeyValueDisplay;

import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProjectBadge implements SynapseWidgetPresenter {
	
	private ProjectBadgeView view;
	private ProjectHeader header;
	private FavoriteWidget favoritesWidget;
	private GWTWrapper gwt;
	private UserProfileAsyncHandler userProfileAsyncHandler;
	@Inject
	public ProjectBadge(ProjectBadgeView view, 
			FavoriteWidget favoritesWidget,
			GWTWrapper gwt,
			UserProfileAsyncHandler userProfileAsyncHandler
			) {
		this.view = view;
		this.favoritesWidget = favoritesWidget;
		this.gwt = gwt;
		this.userProfileAsyncHandler = userProfileAsyncHandler;
		view.setFavoritesWidget(favoritesWidget.asWidget());
		favoritesWidget.setLoadingSize(10);
	}
	
	public void configure(ProjectHeader header) {
		this.header = header;
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
			view.configure(header.getName(), header.getId());
			updateTooltip();
		}
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}	
	
	public void updateTooltip() {
		if (header.getModifiedBy() != null) {
			userProfileAsyncHandler.getUserProfile(header.getModifiedBy().toString(), new AsyncCallback<UserProfile>() {
				@Override
				public void onFailure(Throwable caught) {
					updateTooltip(null);
				}
				public void onSuccess(UserProfile profile) {
					updateTooltip(profile);
				};
			});
		} else {
			updateTooltip(null);
		}
	}
	
	public void updateTooltip(UserProfile modifiedBy) {
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
		
		view.setTooltip(ProvViewUtil.createEntityPopoverHtml(new KeyValueDisplay<String>(map, order)).asString());
	}
	
	public void addStyleName(String style) {
		view.addStyleName(style);
	}
	
	public ProjectHeader getHeader() {
		return header;
	}
}
;