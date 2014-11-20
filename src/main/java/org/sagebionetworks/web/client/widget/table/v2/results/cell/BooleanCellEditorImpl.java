package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import java.util.Arrays;
import java.util.List;

import com.google.inject.Inject;
/**
 * The boolean cell editor extends the enum editor configured with 'true' & 'false'
 * @author John
 *
 */
public class BooleanCellEditorImpl extends EnumCellEditorImpl implements BooleanCellEditor {

	private static final List<String> booleanOptions = Arrays.asList("true", "false");
	
	@Inject
	public BooleanCellEditorImpl(EnumCellEditorView view) {
		super(view);
		configure(booleanOptions);
	}

}
