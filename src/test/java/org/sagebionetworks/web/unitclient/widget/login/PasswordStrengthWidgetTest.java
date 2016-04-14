package org.sagebionetworks.web.unitclient.widget.login;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.widget.login.PasswordStrengthWidget;
import org.sagebionetworks.web.client.widget.login.PasswordStrengthWidgetView;
import org.sagebionetworks.web.client.widget.login.ZxcvbnWrapper;

public class PasswordStrengthWidgetTest {
	
	@Mock
	ZxcvbnWrapper mockZxcvbn;
	@Mock
	PasswordStrengthWidgetView mockView;
	
	PasswordStrengthWidget widget;
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		widget = new PasswordStrengthWidget(mockView, mockZxcvbn);
	}
	
	@Test
	public void testWeakPasswordNull() {
		widget.scorePassword(null);
		verify(mockView).setVisible(false);
		verify(mockZxcvbn, never()).scorePassword(anyString());
	}
	@Test
	public void testWeakPasswordEmpty() {
		widget.scorePassword("");
		verify(mockView).setVisible(false);
		verify(mockZxcvbn, never()).scorePassword(anyString());
	}
	@Test
	public void testWeakPasswordTooShort() {
		widget.scorePassword("A");
		verify(mockView).showWeakPasswordUI(PasswordStrengthWidget.TOO_SHORT_MESSAGE);
	}
	
	@Test
	public void testWeakPasswordBasedOnScore() {
		String passwordToTest = "abcabcabc";
		String feedback = "abcabcabc is a bad password";
		when(mockZxcvbn.getScore()).thenReturn(0);
		when(mockZxcvbn.getFeedback()).thenReturn(feedback);
		widget.scorePassword(passwordToTest);
		verify(mockZxcvbn).scorePassword(passwordToTest);
		verify(mockView).showWeakPasswordUI(feedback);
	}
	
	@Test
	public void testFairPasswordBasedOnScore() {
		String passwordToTest = "A fair password";
		String feedback = "ok, that's fair";
		when(mockZxcvbn.getScore()).thenReturn(2);
		when(mockZxcvbn.getFeedback()).thenReturn(feedback);
		widget.scorePassword(passwordToTest);
		verify(mockZxcvbn).scorePassword(passwordToTest);
		verify(mockView).showFairPasswordUI(feedback);
	}
	
	@Test
	public void testGoodPasswordBasedOnScore() {
		String passwordToTest = "A good password";
		when(mockZxcvbn.getScore()).thenReturn(3);
		widget.scorePassword(passwordToTest);
		verify(mockZxcvbn).scorePassword(passwordToTest);
		verify(mockView).showGoodPasswordUI();
	}
	
	@Test
	public void testStrongPasswordBasedOnScore() {
		String passwordToTest = "Wow, this is a strong password!";
		when(mockZxcvbn.getScore()).thenReturn(4);
		widget.scorePassword(passwordToTest);
		verify(mockZxcvbn).scorePassword(passwordToTest);
		verify(mockView).showStrongPasswordUI();
	}
}
