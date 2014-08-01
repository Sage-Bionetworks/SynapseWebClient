package org.sagebionetworks.web.client.widget.table;

import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.view.client.Range;

/**
 * Fixes issues around last page in pager
 * From http://stackoverflow.com/questions/6057141/simplepager-row-count-is-working-incorrectly
 *
 */
public class MySimplePager extends SimplePager {

	public MySimplePager() {
		this.setRangeLimited(true);
	}

	public MySimplePager(TextLocation location, Resources resources,
			boolean showFastForwardButton, int fastForwardRows,
			boolean showLastPageButton) {
		super(location, resources, showFastForwardButton, fastForwardRows,
				showLastPageButton);
		this.setRangeLimited(true);
	}

	public void setPageStart(int index) {

		if (this.getDisplay() != null) {
			Range range = getDisplay().getVisibleRange();
			int pageSize = range.getLength();
			if (!isRangeLimited() && getDisplay().isRowCountExact()) {
				index = Math.min(index, getDisplay().getRowCount() - pageSize);
			}
			index = Math.max(0, index);
			if (index != range.getStart()) {
				getDisplay().setVisibleRange(index, pageSize);
			}
		}
	}
}