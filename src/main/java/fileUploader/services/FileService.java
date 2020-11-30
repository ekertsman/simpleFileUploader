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
    public List<FileInfo> getUserFiles(String user){
        return fileInfoDAO.getAllFiles(user);
    }

    @Override
    public FileInfo getFile(String user, String name){
        return fileInfoDAO.getFile(user, name);
    }

    @Override
    public List<String> getAllFileNames(String user){
        return fileInfoDAO.getAllFileNames(user);
    }

    @Override
    public void uploadFile(MultipartFile file, String user) throws IOException {
        fileInfoDAO.uploadFile(file, user);
    }

}
