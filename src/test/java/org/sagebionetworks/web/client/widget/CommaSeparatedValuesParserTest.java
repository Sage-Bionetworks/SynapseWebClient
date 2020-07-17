package org.sagebionetworks.web.client.widget;


import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.web.client.widget.csv.PapaCSVParser;
import org.sagebionetworks.web.client.widget.csv.PapaParseResult;

@RunWith(MockitoJUnitRunner.class)
public class CommaSeparatedValuesParserTest extends TestCase {

	@Mock
	PapaCSVParser mockJavascriptParser;

	@Mock
	CommaSeparatedValuesParserView mockView;

	@InjectMocks
	CommaSeparatedValuesParser parser;

	@Test
	public void testParseToStringList(){
		String text = "asdf,asdf\nqwer,zxcv";
		when(mockView.getText()).thenReturn(text);
		PapaParseResult parseResult = new PapaParseResult();
		parseResult.data = new String[][]{{"asdf","asdf"},{"qwer","zxcv"}};
		when(mockJavascriptParser.parse(text)).thenReturn(parseResult);


		List<String> result = parser.parseToStringList();

		assertEquals(4, result.size());
		assertEquals(Arrays.asList("asdf","asdf","qwer","zxcv"), result);
	}

}