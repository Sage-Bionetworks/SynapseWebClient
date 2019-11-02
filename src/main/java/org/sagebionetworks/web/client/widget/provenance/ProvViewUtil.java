package org.sagebionetworks.web.client.widget.provenance;

import java.util.Map;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.IconSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.util.ContentTypeUtils;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.EntityTypeUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.HelpWidget;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidgetView.Presenter;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.KeyValueDisplay;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.provenance.ActivityGraphNode;
import org.sagebionetworks.web.shared.provenance.ActivityType;
import org.sagebionetworks.web.shared.provenance.EntityGraphNode;
import org.sagebionetworks.web.shared.provenance.ExpandGraphNode;
import org.sagebionetworks.web.shared.provenance.ExternalGraphNode;
import org.sagebionetworks.web.shared.provenance.ProvGraphNode;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class ProvViewUtil {

	private static final String PROV_ENTTITY_NODE_STYLE = "provEntityNode";
	private static final String PROV_ACTIVITY_NODE_STYLE = "provActivityNode";
	private static final String PROV_EXPAND_NODE_STYLE = "provExpandNode";

	private static final String PROV_ACTIVITY_LABEL_STYLE = "provActivityLabel";
	private static final String PROV_ACTIVITY_TIME_STYLE = "provActivityLabelTime";
	private static final String PROV_VERSION_DISPLAY_STYLE = "provVersionDisplay";
	private static final String PROV_ACTIVITY_UNDEFINED_STYLE = "provUndefinedChange";

	private static final int ENTITY_LINE_NUMBER_CHARS = 17;
	private static final int MAX_TOOL_TIP_VALUE_CHAR = 300;
	private static final int MAX_ACT_CODE_NAME_CHAR = 30;
	private static String ACT_MARGIN_USER = "margin-bottom-1 margin-left-10";
	private static String ACT_MARGIN_TIME = "margin-bottom-3 margin-left-10";
	private static final String ACT_MARGIN_NAME = "margin-top-5 margin-right-4 margin-bottom-10 margin-left-10";

	public static ProvNodeContainer createActivityContainer(ActivityGraphNode node, IconsImageBundle iconsImageBundle, PortalGinInjector ginInjector) {
		ProvNodeContainer container = new ProvNodeContainer();
		container.getElement().setId(node.getId());
		container.addStyleName(PROV_ACTIVITY_NODE_STYLE);
		FlowPanel label = new FlowPanel();
		label.addStyleName(PROV_ACTIVITY_LABEL_STYLE);

		if (node.getType() == ActivityType.UNDEFINED) {
			container.addStyleName(PROV_ACTIVITY_UNDEFINED_STYLE);
			Div undefinedContainer = new Div();
			undefinedContainer.add(new Text(DisplayConstants.UNDEFINED));
			HelpWidget help = new HelpWidget();
			help.setHelpMarkdown(DisplayConstants.PROVENANCE_BASIC_HELP);
			help.setHref(WebConstants.PROVENANCE_API_URL);
			undefinedContainer.add(help);
			undefinedContainer.addStyleName(ACT_MARGIN_NAME);
			container.add(undefinedContainer);
		} else {
			// display user profile and name if defined
			if (node.getActivityName() != null) {
				label.add(new HTML(DisplayUtils.stubStrPartialWord(node.getActivityName(), MAX_ACT_CODE_NAME_CHAR)));
			}
			UserBadge badge = ginInjector.getUserBadgeWidget();
			badge.configure(node.getModifiedBy());
			HTML time = new HTML(ginInjector.getDateTimeUtils().getLongFriendlyDate(node.getModifiedOn()));
			time.addStyleName(PROV_ACTIVITY_TIME_STYLE);

			FlowPanel content = new FlowPanel();
			label.addStyleName(ACT_MARGIN_NAME);
			content.add(label);
			Widget userBadgeWidget = badge.asWidget();
			userBadgeWidget.addStyleName(ACT_MARGIN_USER);
			content.add(userBadgeWidget);
			time.addStyleName(ACT_MARGIN_TIME);
			content.add(time);
			container.addContent(content);
		}

		setPosition(node, container);
		return container;
	}

	public static ProvNodeContainer createEntityContainer(EntityGraphNode node, IconsImageBundle iconsImageBundle) {
		ProvNodeContainer container = createEntityContainer(node.getId(), node.getEntityId(), node.getName(), node.getVersionLabel(), node.getVersionNumber(), node.getEntityType(), iconsImageBundle);
		setPosition(node, container);
		return container;
	}

	public static ProvNodeContainer createExpandContainer(final ExpandGraphNode node, final Presenter presenter, final ProvenanceWidgetView view) {
		final Anchor link = new Anchor();
		link.addStyleName("textDecorationNone");
		Span sp = new Span("&#8230;"); // ellipsis
		sp.addStyleName("moveup-8");
		sp.setPaddingLeft(10); // expand clickable area
		sp.setPaddingRight(10);
		link.add(sp);
		link.addClickHandler(event -> {
			link.clear();
			link.add(DisplayUtils.getSmallLoadingWidget());
			presenter.expand(node);
		});

		ProvNodeContainer container = new ProvNodeContainer();
		container.getElement().setId(node.getId());
		container.setStyleName(PROV_ENTTITY_NODE_STYLE + " " + PROV_EXPAND_NODE_STYLE);
		container.add(link);
		setPosition(node, container);
		return container;
	}

	public static ProvNodeContainer createExternalUrlContainer(ExternalGraphNode node, IconsImageBundle iconsImageBundle) {
		if (node.getName() == null)
			node.setName("");
		if (node.getUrl() == null)
			node.setUrl("");

		SafeHtmlBuilder builder = new SafeHtmlBuilder();
		Anchor link = new Anchor();
		link.setHref(node.getUrl());
		link.setTarget("_blank");

		// icon
		ImageResource icon;
		if (node.getUrl().contains("github.com") || node.getUrl().contains("githubusercontent.com"))
			icon = iconsImageBundle.github16();
		else if (node.getUrl().contains("genomespace.org"))
			icon = iconsImageBundle.genomespace16();
		else
			icon = iconsImageBundle.documentExternal16();
		builder.appendHtmlConstant(AbstractImagePrototype.create(icon).getHTML());
		builder.appendHtmlConstant("<br/>");

		// name
		String stubName = DisplayUtils.stubStrPartialWord(node.getName(), ENTITY_LINE_NUMBER_CHARS);
		builder.appendEscaped(stubName);
		builder.appendHtmlConstant("<br/>");
		link.add(new HTML(builder.toSafeHtml()));

		ProvNodeContainer container = new ProvNodeContainer();
		if (node.getId() != null)
			container.getElement().setId(node.getId());
		container.setStyleName(PROV_ENTTITY_NODE_STYLE);
		container.addContent(link);
		setPosition(node, container);
		return container;
	}

	/*
	 * Private utils
	 */
	private static void setPosition(ProvGraphNode node, FlowPanel container) {
		container.getElement().getStyle().setTop(node.getyPos(), Unit.PX);
		container.getElement().getStyle().setLeft(node.getxPos(), Unit.PX);
	}

	private static ProvNodeContainer createEntityContainer(String id, String entityId, String name, String versionLabel, Long versionNumber, String entityType, IconsImageBundle iconsImageBundle) {
		FlowPanel container = new FlowPanel();
		SafeHtmlBuilder builder = new SafeHtmlBuilder();
		Anchor link = new Anchor();
		if (versionNumber != null) {
			link.setHref(DisplayUtils.getSynapseHistoryToken(entityId, versionNumber));
		} else {
			link.setHref(DisplayUtils.getSynapseHistoryToken(entityId));
		}

		// icon
		IconType iconType = EntityTypeUtils.getIconTypeForEntityClassName(entityType);
		if (FileEntity.class.getName().equals(entityType)) {
			if (ContentTypeUtils.isRecognizedCodeFileName(name) || org.sagebionetworks.web.client.ContentTypeUtils.isWebRecognizedCodeFileName(name)) {
				iconType = IconType.FILE_CODE_O;
			}
		}
		Icon icon = new Icon(iconType);
		icon.setSize(IconSize.TIMES2);
		container.add(new SimplePanel(icon));

		// name
		String stubName = DisplayUtils.stubStrPartialWord(name, ENTITY_LINE_NUMBER_CHARS);
		if (stubName != null)
			builder.appendEscaped(stubName);
		builder.appendHtmlConstant("<br/>");

		// version
		HTML versionHtml;
		if (versionNumber != null) {
			String versionNumberStr = "v." + versionNumber;
			String versionDisplay;
			if (versionLabel == null || versionNumber.toString().equals(versionLabel)) {
				versionDisplay = versionNumberStr;
			} else {
				versionDisplay = DisplayUtils.stubStrPartialWord(versionLabel + " (" + versionNumberStr + ")", ENTITY_LINE_NUMBER_CHARS - versionNumberStr.length());
			}
			versionHtml = new HTML(SafeHtmlUtils.fromString(DisplayUtils.stubStrPartialWord(versionDisplay, ENTITY_LINE_NUMBER_CHARS - versionNumberStr.length())));
		} else {
			versionHtml = new HTML("");
		}
		versionHtml.setStyleName(PROV_VERSION_DISPLAY_STYLE);
		builder.appendHtmlConstant(versionHtml.toString());
		link.add(new HTML(builder.toSafeHtml()));
		ProvNodeContainer node = new ProvNodeContainer();
		if (id != null)
			node.getElement().setId(id);
		node.setStyleName(PROV_ENTTITY_NODE_STYLE);
		container.add(link);
		node.addContent(container);
		return node;
	}

	public static SafeHtml createEntityPopoverHtml(KeyValueDisplay<String> kvDisplay) {
		SafeHtmlBuilder sb = new SafeHtmlBuilder();
		if (kvDisplay != null) {
			Map<String, String> map = kvDisplay.getMap();
			for (String key : kvDisplay.getKeyDisplayOrder()) {
				if (!DisplayConstants.DESCRIPTION.equalsIgnoreCase(key)) {
					String val = map.get(key);
					if (val == null)
						val = "";
					val = val.length() > MAX_TOOL_TIP_VALUE_CHAR ? val.substring(0, MAX_TOOL_TIP_VALUE_CHAR - 3) + "..." : val;
					sb.appendHtmlConstant("<span class=\"boldText\">").appendEscaped(key).appendHtmlConstant("</span>");
					if (val.length() > 0) {
						sb.appendEscaped(": ").appendEscaped(val);
					}
					sb.appendHtmlConstant("<br/>");
				}
			}
		}
		return sb.toSafeHtml();
	}
}
