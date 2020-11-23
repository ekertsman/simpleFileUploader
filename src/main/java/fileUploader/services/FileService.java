package fileUploader.services;

import fileUploader.dao.IFileInfoDAO;
import fileUploader.dto.FileInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.List;

@Service
@Slf4j
public class FileService implements IFileService {
    @Autowired
    private IFileInfoDAO fileInfoDAO;

    @Override
    public List<FileInfo> getAllFiles(){
        return fileInfoDAO.getAllFiles();
    }

    @Override
    public List<String> getAllFileNames(){
        return fileInfoDAO.getAllFileNames();
    }

    @Override
    public void uploadFile(MultipartFile file) throws IOException {
        fileInfoDAO.uploadFile(file);
    }

}
