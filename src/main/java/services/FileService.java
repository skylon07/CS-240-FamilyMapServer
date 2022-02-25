package services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import dataAccess.Database;
import services.requests.FileRequest;
import services.responses.FileResponse;

/**
 * This service provides functionality for getting files off the server
 */
public class FileService extends GenericService<FileRequest, FileResponse> {
    /**
     * Creates a new FileService by calling GenericService with this
     * service's name
     */
    public FileService() {
        super("FileService");
    }

    @Override
    public FileResponse onGet(FileRequest request, Database database) throws InvalidHTTPMethodException {
        assert request.path != null : "FileService expected a path";
        String filePath = "web" + request.path;
        if (filePath.equals("web/")) {
            filePath = "web/index.html";
        }
        File fileToSend = new File(filePath);

        try (BufferedReader reader = new BufferedReader(new FileReader(fileToSend));) {
            StringBuilder str = new StringBuilder();
            char[] buffer = new char[1024];
            while ((reader.read(buffer) != -1)) {
                str.append(buffer);
            }
            return this.createSuccessfulResponse(str.toString());
        } catch (FileNotFoundException err) {
            return this.createFailedResponse("File does not exist");
        } catch (IOException err) {
            return this.createFailedResponse("File reading failed");
        }
    }

    /**
     * Creates a successful FileResponse with required parameters
     * 
     * @param authToken is the newly created auth token
     * @param username is the username of the now-logged-in User
     * @param personID is the personID of that User
     * @return the successful FileResponse
     */
    private FileResponse createSuccessfulResponse(String fileData) {
        FileResponse response = new FileResponse();
        response.success = true;
        response.data = fileData;
        return response;
    }

    /**
     * Creates a failed FileResponse with an error message
     * @param errMsg is the message to send back in the response
     * @return the failed FileResponse
     */
    private FileResponse createFailedResponse(String errMsg) {
        FileResponse response = new FileResponse();
        response.success = false;
        response.message = errMsg;
        return response;
    }
}
