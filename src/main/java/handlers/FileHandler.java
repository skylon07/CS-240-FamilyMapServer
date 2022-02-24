package handlers;

import services.FileService;
import services.requests.FileRequest;
import services.responses.FileResponse;

public class FileHandler extends GenericHandler<FileRequest, FileResponse, FileService> {
    @Override
    protected Class<FileRequest> getBoundRequestClass() {
        return FileRequest.class;
    }

    @Override
    protected FileService createBoundService() {
        return new FileService();
    }
}
