package services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import dataAccess.Database;
import dataAccess.DatabaseException;
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
    public FileResponse onGet(FileRequest request, Database database) throws InvalidHTTPMethodException, DatabaseException {
        if (request.path == null) {
            return this.createIncompleteResponse("path");
        }
        
        String filePath = "web";
        if (request.path.charAt(0) != '/') {
            filePath += '/';
        }
        filePath += request.path;
        if (filePath.equals("web/")) {
            filePath = "web/index.html";
        }
        File fileToSend = new File(filePath);

        try {
            String str = this.readServerFile(fileToSend);
            return this.createSuccessfulResponse(str);
        } catch (FileNotFoundException err) {
            return this.createFileNotFoundResponse();
        } catch (IOException err) {
            return this.createFailedResponse("File reading failed");
        }
    }

    /**
     * Returns text (as a String) from a file on the server
     * 
     * @param fileToRead is the File to read from
     * @return the text as a String
     * @throws IOException when the file cannot be read from
     */
    private String readServerFile(File fileToRead) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileToRead));) {
            StringBuilder str = new StringBuilder();
            while (reader.ready()) {
                str.append((char) reader.read());
            }
            return str.toString();
        }
    }

    /**
     * Creates a successful FileResponse with required parameters
     * 
     * @param fileData is the data read from the requested file to send back
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
     * 
     * @return the failed FileResponse
     */
    private FileResponse createFileNotFoundResponse() {
        FileResponse response = new FileResponse();
        response.success = false;
        response.message = "File not found";
        try {
            // response.data = this.readServerFile(new File("web/HTML/404.html"));
            response.data = this.readServerFile(new File("web/HTML/404.html"));
        } catch (IOException err) {
            return this.createFailedResponse("File not found");
        }
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

    @Override
    protected FileResponse createSpecificErrorResponse(String errMsg) {
        return new FileResponse();
    }
}
