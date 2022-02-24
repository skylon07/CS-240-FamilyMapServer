package services;

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
        // TODO Auto-generated method stub
        return null;
    }
}
