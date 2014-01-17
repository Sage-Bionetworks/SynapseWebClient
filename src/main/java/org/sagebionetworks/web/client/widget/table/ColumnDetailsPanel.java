package org.sagebionetworks.web.client.widget.table;

import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.web.client.DisplayConstants;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

public class ColumnDetailsPanel extends FlowPanel {
	
	Anchor moveUp = new Anchor(SafeHtmlUtils.fromSafeConstant("<span class=\"glyphicon glyphicon-arrow-up margin-right-5\"></span>"));
	Anchor moveDown = new Anchor(SafeHtmlUtils.fromSafeConstant("<span class=\"glyphicon glyphicon-arrow-down margin-right-5\"></span>"));
	Anchor delete = new Anchor(SafeHtmlUtils.fromSafeConstant("<span class=\"glyphicon glyphicon-remove\"></span>"));
	ColumnModel col;
	
	public ColumnDetailsPanel(String accordionId, ColumnModel col, String colContentId) {
		this.col = col;
		this.addStyleName("panel panel-default");					
		
		FlowPanel columnEntry = new FlowPanel();
		columnEntry.addStyleName("panel-heading row");			
		String expandLinkStyleOpen = "<a data-toggle=\"collapse\" data-parent=\"#" + accordionId + "\" href=\"#" + colContentId + "\" class=\"link\">";
		
		FlowPanel left = new FlowPanel();			
		left.addStyleName("col-xs-7 col-sm-9 col-md-10");
		left.add(new HTML("<h4>" + expandLinkStyleOpen + SafeHtmlUtils.fromString(col.getName()).asString() + "</a></h4>"));
		FlowPanel right = new FlowPanel();
		right.addStyleName("col-xs-5 col-sm-3 col-md-2 text-align-right largeIconButton");
		right.add(moveUp);
		right.add(moveDown);		
		right.add(delete);
		
		columnEntry.add(left);
		columnEntry.add(right);
		this.add(columnEntry);
		
		FlowPanel columnContent = new FlowPanel();
		columnContent.addStyleName("panel-collapse collapse");
		columnContent.getElement().setId(colContentId);
		FlowPanel columnContentBody = new FlowPanel();
		columnContentBody.addStyleName("panel-body");		
		columnContentBody.add(createColumnView(col));
		columnContent.add(columnContentBody);
		this.add(columnContent);
		
	}
	
	public Anchor getMoveUp() {
		return moveUp;
	}

	public Anchor getMoveDown() {
		return moveDown;
	}
	
	public Anchor getDelete() {
		return delete;
	}

	public ColumnModel getCol() {
		return col;
	}

	/**
	 * Create a view widget for a column
	 * @param col
	 * @return
	 */
	private Widget createColumnView(org.sagebionetworks.repo.model.table.ColumnModel col) {
		SafeHtmlBuilder shb = new SafeHtmlBuilder();
		shb.appendHtmlConstant("<span class=\"boldText\">" + DisplayConstants.NAME + "</span>: ").appendEscaped(col.getName()).appendHtmlConstant("<br/>")
		.appendHtmlConstant("<span class=\"boldText\">" + DisplayConstants.TYPE + "</span>: ").appendEscaped(ColumnUtils.getColumnDisplayName(col.getColumnType())).appendHtmlConstant("<br/>");
		if(col.getDefaultValue() != null) 
			shb.appendHtmlConstant("<span class=\"boldText\">" + DisplayConstants.DEFAULT_VALUE + "</span>: ").appendEscaped(col.getDefaultValue()).appendHtmlConstant("<br/>");
		if(col.getEnumValues() != null && col.getEnumValues().size() > 0) {
			shb.appendHtmlConstant("<span class=\"boldText\">" + DisplayConstants.RESTRICTED_VALUES + "</span>: ");
			String values = "";
			for(String val : col.getEnumValues()) values += val + ", ";
			values = values.substring(0, values.length()-2); // chop last comma
			shb.appendEscaped(values).appendHtmlConstant("<br/>");
		}		
		return new HTML(shb.toSafeHtml());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((delete == null) ? 0 : delete.hashCode());
		result = prime * result
				+ ((moveDown == null) ? 0 : moveDown.hashCode());
		result = prime * result + ((moveUp == null) ? 0 : moveUp.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ColumnDetailsPanel other = (ColumnDetailsPanel) obj;
		if (delete == null) {
			if (other.delete != null)
				return false;
		} else if (!delete.equals(other.delete))
			return false;
		if (moveDown == null) {
			if (other.moveDown != null)
				return false;
		} else if (!moveDown.equals(other.moveDown))
			return false;
		if (moveUp == null) {
			if (other.moveUp != null)
				return false;
		} else if (!moveUp.equals(other.moveUp))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ColumnDetailsPanel [moveUp=" + moveUp + ", moveDown="
				+ moveDown + ", delete=" + delete + "]";
	}

	
}
