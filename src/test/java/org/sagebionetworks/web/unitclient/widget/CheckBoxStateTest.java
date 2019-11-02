package org.sagebionetworks.web.unitclient.widget;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.sagebionetworks.web.client.widget.CheckBoxState;

public class CheckBoxStateTest {
	@Test
	public void test() {
		// standard cases
		assertEquals(CheckBoxState.DESELECTED, CheckBoxState.getStateFromCount(0, 10)); // 0 selected
		assertEquals(CheckBoxState.INDETERMINATE, CheckBoxState.getStateFromCount(3, 10)); // subset selected
		assertEquals(CheckBoxState.SELECTED, CheckBoxState.getStateFromCount(10, 10)); // all selected

		assertEquals(CheckBoxState.DESELECTED, CheckBoxState.getStateFromCount(-10, 100));
	}
}
