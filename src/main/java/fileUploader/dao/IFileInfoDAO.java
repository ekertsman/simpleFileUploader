package fileUploader.dao;

import fileUploader.dto.FileInfo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IFileInfoDAO {
    List<FileInfo> getAllFiles(String user);
    List<String> getAllFileNames(String user);
    void uploadFile(MultipartFile file, String user) throws IOException;
    FileInfo getFile(String user, String name);
}
