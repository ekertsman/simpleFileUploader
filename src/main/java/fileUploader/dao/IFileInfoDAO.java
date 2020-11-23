package fileUploader.dao;

import fileUploader.dto.FileInfo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IFileInfoDAO {
    List<FileInfo> getAllFiles();
    List<String> getAllFileNames();
    void uploadFile(MultipartFile file) throws IOException;
}
