package Server.FrontEndServerStuff.HttpResources.Authentication;

import Server.AccountManagement.UserEntry;
import Server.AccountManagement.UsernamePasswordPair;
import Server.DatabaseStuff.ClientDatabaseEditor;
import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AuthenticationManagerTest {

    static final String VALID_USERNAME = "user";
    static final String VALID_PASSWORD = "pass";
    static final String INVALID_USERNAME = "badUser";
    static final String INVALID_PASSWORD = "badPass";
    static final String EMAIL = "hello@gmail.com";
    static final String FIRST_NAME = "first";
    static final String LAST_NAME = "last";
    static final String CLIENT = "owner";
    static final long USER_ID = 1l;
    static final String VALID_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwidXNlcm5hbWUiOiJ1c2VyIiwiY2xpZW50Ijoib3duZXIifQ.KL9GcoPx0fKERF5rwiuvPasvwly_mZG9CgGnDhXv96I";
    static final String BAD_TOKEN = VALID_TOKEN.replace("a","b");

    private static AuthenticationManager underTest;
    private static ClientDatabaseEditor editor;


    @BeforeClass
    public static void generateDatabaseEditorAndUnderTest() {
        editor = generateMockDatabaseEditor();
        underTest = new AuthenticationManager(editor);
    }

    private static ClientDatabaseEditor generateMockDatabaseEditor() {
        ClientDatabaseEditor editor = mock(ClientDatabaseEditor.class);
        UserEntry user = mock(UserEntry.class);
        when(editor.getUserEntry(VALID_USERNAME)).thenReturn(user);
        when(editor.getUserEntry(INVALID_USERNAME)).thenReturn(null);
        when(user.validateCredentials(VALID_USERNAME, VALID_PASSWORD)).thenReturn(true);
        when(user.validateCredentials(VALID_USERNAME, INVALID_PASSWORD)).thenReturn(false);
        when(user.getClientName()).thenReturn(CLIENT);
        when(user.getId()).thenReturn(USER_ID);
        when(user.getEmail()).thenReturn(EMAIL);
        when(user.getFirstName()).thenReturn(FIRST_NAME);
        when(user.getLastName()).thenReturn(LAST_NAME);
        when(user.getUserName()).thenReturn(VALID_USERNAME);
        return editor;
    }

    @Test
    public void shouldReturnFalseIfUsernameCorrectButPasswordCredentialsInvalid() {
        assertFalse(underTest.validateUser(new UsernamePasswordPair(VALID_USERNAME, INVALID_PASSWORD)));
    }

    @Test
    public void shouldReturnFalseIfUsernameNotRecognised() {
        assertFalse(underTest.validateUser(new UsernamePasswordPair(INVALID_USERNAME,INVALID_PASSWORD)));
    }

    @Test
    public void shouldReturnTrueIfUserCredentialsValid() {
        assertTrue(underTest.validateUser(new UsernamePasswordPair(VALID_USERNAME,VALID_PASSWORD)));
    }

    @Test
    public void shouldReturnJWTContainingUsernameAndClientNameIfCredentialsValid() {
        assertEquals(VALID_TOKEN, underTest.getJWT(VALID_USERNAME));
    }

    @Test
    public void shouldThrowIllegalStateExceptionIfGetTokenCalledWithoutValidatingUser() {
        try {
            underTest.getJWT(INVALID_USERNAME);
            fail("Should not call this without checking whether user validates");
        } catch (IllegalStateException e) {
            assertEquals("This should not be called if user has not been validated!", e.getMessage());
        }
    }

    @Test
    public void shouldProduceResponseContainingAllFields() {
        UserJson response = underTest.generateAuthenticationResponse(VALID_USERNAME);
        assertEquals(VALID_TOKEN, response.token);
        assertEquals(EMAIL, response.email);
        assertEquals(FIRST_NAME, response.firstName);
        assertEquals(LAST_NAME, response.lastName);
        assertEquals(CLIENT, response.client);
        assertEquals(USER_ID, response.id);
        assertEquals(VALID_USERNAME, response.username);
    }

    @Test
    public void shouldAcceptValidToken() {
        assertTrue(underTest.isValidJWT(VALID_TOKEN));
    }

    @Test
    public void shouldRejectInvalidToken() {
        assertFalse(underTest.isValidJWT(BAD_TOKEN));
    }

    @Test
    public void shouldProduceUserJsonForValidTokens() {
        assertEquals(VALID_USERNAME, underTest.getUserEntryForJWT(VALID_TOKEN).getUserName());
    }

}