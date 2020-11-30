package fileUploader.services;

import fileUploader.dto.FileInfo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IFileService {
    List<FileInfo> getUserFiles(String user);

    FileInfo getFile(String user, String name);

    List<String> getAllFileNames(String user);

    void uploadFile(MultipartFile file, String user) throws IOException;
}
