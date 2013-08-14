package org.sagebionetworks.web.unitclient.widget.entity.editor;

import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.message.ObjectType;
import org.sagebionetworks.web.client.widget.entity.editor.MathJaxConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.MathJaxConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.ProvenanceConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.ProvenanceConfigView;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

public class MathJaxConfigEditorTest {
		
	MathJaxConfigEditor editor;
	MathJaxConfigView mockView;
	WikiPageKey wikiKey = new WikiPageKey("", ObjectType.ENTITY.toString(), null);
	@Before
	public void setup(){
		mockView = mock(MathJaxConfigView.class);
		editor = new MathJaxConfigEditor(mockView);
	}
	
	@Test
	public void testAsWidget() {
		editor.asWidget();
		verify(mockView).asWidget();
	}
	@Test
	public void testConfigure() {
		Map<String, String> descriptor = new HashMap<String, String>();
		
		String equation = "\\left( \\sum_{k=1}^n a_k b_k \\right)^2 \\leq \\left( \\sum_{k=1}^n a_k^2 \\right) \\left( \\sum_{k=1}^n b_k^2 \\right)";
		descriptor.put(WidgetConstants.MATHJAX_WIDGET_EQUATION_KEY, equation);
		editor.configure(wikiKey, descriptor);
		
		verify(mockView).setEquation(eq(equation));
		when(mockView.getEquation()).thenReturn(equation);
		editor.updateDescriptorFromView();
		verify(mockView).checkParams();
		verify(mockView).getEquation();
		
		//also verify the prefix and suffix
		String updatedEquation = descriptor.get(WidgetConstants.MATHJAX_WIDGET_EQUATION_KEY);
		assertTrue(updatedEquation.startsWith(WebConstants.MATHJAX_PREFIX));
		assertTrue(updatedEquation.endsWith(WebConstants.MATHJAX_SUFFIX));
		
		
		//test when it already starts with the required prefix
		String equationWithPrefixSuffixAlready = WebConstants.MATHJAX_PREFIX + equation + WebConstants.MATHJAX_SUFFIX;
		when(mockView.getEquation()).thenReturn(equationWithPrefixSuffixAlready);
		editor.updateDescriptorFromView();
		updatedEquation = descriptor.get(WidgetConstants.MATHJAX_WIDGET_EQUATION_KEY);
		assertEquals(equationWithPrefixSuffixAlready, updatedEquation);
	}
}
