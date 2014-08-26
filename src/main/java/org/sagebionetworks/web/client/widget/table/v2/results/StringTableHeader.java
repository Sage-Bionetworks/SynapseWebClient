package org.sagebionetworks.web.client.widget.table.v2.results;

import java.util.List;

import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.web.client.view.bootstrap.table.THead;
import org.sagebionetworks.web.client.view.bootstrap.table.TableHeader;
import org.sagebionetworks.web.client.view.bootstrap.table.TableRow;

/**
 * A boostrap table header to render a list of strings..
 * @author jmhill
 *
 */
public class StringTableHeader extends THead {
	
	/**
	 * 
	 * @param columns
	 */
	public StringTableHeader(List<String> columnNames){
		super();
		TableRow tr = new TableRow();
		this.add(tr);
		// Add the Columns
		for(String name: columnNames){
			TableHeader th = new TableHeader();
			tr.add(th);
			Text text = new Text(name);
			th.add(text);
		}
	}

}
