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
        if (filePath == "web/") {
            filePath = "web/index.html";
        }
        File fileToSend = new File(filePath);

        FileResponse response = new FileResponse();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileToSend));) {
            StringBuilder str = new StringBuilder();
            char[] buffer = new char[1024];
            while ((reader.read(buffer) != -1)) {
                str.append(buffer);
            }
            response.success = true;
            response.data = str.toString();
        } catch (FileNotFoundException err) {
            response.success = false;
            response.data = "File does not exist";
        } catch (IOException err) {
            response.success = false;
            response.data = "File reading failed";
        }
        return response;
    }
}
