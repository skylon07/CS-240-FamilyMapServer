package services;

import org.junit.jupiter.api.*;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

import services.requests.FileRequest;
import services.responses.FileResponse;

/**
 * Contains test cases to ensure the ClearService works correctly
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FileServiceTest {
    /**
     * Runs fail(), but shows a traceback to the call of this method, instead
     * of where the error was thrown
     * 
     * @param err is the error that was thrown
     */
    @SuppressWarnings("unused")
    private void failNoTraceback(Exception err) {
        fail(err.getMessage());
    }

    /**
     * Ensures the service can get files and return the correct data
     */
    @Test
    @DisplayName("Existing files can be returned")
    public void testGetExistingFile() {
        FileService service = new FileService();
        FileRequest request = new FileRequest();
        request.path = "test.txt";
        FileResponse response = service.process("GET", request);

        assertNotNull(response);
        assertTrue(response.success);
        assertEquals(response.data, "This is for FileServiceTest.java");
    }

    /**
     * Ensures the service fails when getting nonexisting files
     */
    @Test
    @DisplayName("Existing files can be returned")
    public void testGetNonExistingFile() {
        FileService service = new FileService();
        FileRequest request = new FileRequest();
        request.path = "thisDoesNotExistAndShouldNotExist.txt";
        FileResponse response = service.process("GET", request);

        assertNotNull(response);
        assertFalse(response.success);
        assertTrue(response.message.matches(".* not found.*"));
    }

    /**
     * Ensures using POST fails
     */
    @Test
    @DisplayName("POST fails")
    public void testPostMethod() {
        FileService service = new FileService();
        FileRequest request = new FileRequest();
        FileResponse response = service.process("POST", request);

        assertNotNull(response);
        assertFalse(response.success);
        assertTrue(response.message.matches("(I|.*i)nvalid (http |HTTP |Http )?method.*"));
    }
}