package fileUploader.dao;

import fileUploader.dto.FileInfo;
import fileUploader.utils.ConcurrentHashMapLocker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// this class mimics DAO layer interacting with DB
@Slf4j
@Component
public class FileInfoDAOImpl implements IFileInfoDAO {
    private static final String STORAGE_FOLDER_NAME = "filestorage";

    private Path getStoragePath(String user) {
        Path currentPath = Paths.get(System.getProperty("user.dir"));
        Path filePath = Paths.get(currentPath.toString(), STORAGE_FOLDER_NAME);
        createIfNotExists(filePath);
        filePath = Paths.get(filePath.toString(), user);
        createIfNotExists(filePath);
        log.info("Storage path is: " + filePath);
        return filePath;
    }

    private void createIfNotExists(Path filePath) {
        File f = new File(filePath.toString());
        if (!f.exists()){
            log.info("Folder " + STORAGE_FOLDER_NAME + " does not exist. Folder created.");
            f.mkdir();
        }
    }

    private Optional<FileInfo> mapPathToFileInfoDto(Path path) {
        try {
            return Optional.of(FileInfo.builder()
                    .name(path.getFileName().toString())
                    .path(path.toString())
                    .size(Files.size(path))
                    .build());
        } catch (IOException e) {
            log.error("Can not read file info for path " + path.toString(), e);
            return Optional.empty();
        }
    }

    private <T> List<T> fetchFiles(String actionName, String user, Function<Path, T> func){
        log.info(String.format("Fetching %s starts...", actionName));
        Path storagePath = getStoragePath(user);
        List<T> result = new ArrayList<>();
        try(Stream<Path> walk = Files.walk(storagePath)){
            result = walk.filter(Files::isRegularFile)
                    .map(func)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Failed " + actionName, e);
        }
        log.info("Successfully " + actionName);
        return result;
    }

    @Override
    public List<FileInfo> getAllFiles(String user) {
        return fetchFiles("all files info", user, path -> mapPathToFileInfoDto(path).orElse(null));
    }

    @Override
    public List<String> getAllFileNames(String user) {
        return fetchFiles("all file names", user, path -> path.getFileName().toString());
    }

    @Override
    public void uploadFile(MultipartFile file, String user) throws IOException {
        String filename = file.getOriginalFilename();
        log.info("Uploading file " + filename);
        InputStream is = file.getInputStream();
        Path currentPath = getStoragePath(user);

        Path filePath = Paths.get(currentPath.toString(), filename);

        // sync on file name as system is used directly, so files with the same name are placed one by one
        // though it causes rewrite, that's ok in this scenario
        Object locker = ConcurrentHashMapLocker.getLockerString(filename);
        synchronized (locker) {
            Files.copy(is, filePath, StandardCopyOption.REPLACE_EXISTING);
        }
        log.info("File " + filename + " successfully uploaded");
    }

    @Override
    public FileInfo getFile(String user, String name) {
        log.info(String.format("Start fetching file %s for user %s", name, user));
        FileInfo fileInfo = null;
        try {
            if (StringUtils.isEmpty(user)) {
                throw new IllegalArgumentException("User can not be empty");
            }
            if (StringUtils.isEmpty(name)) {
                throw new IllegalArgumentException("File name can not be empty");
            }
            Path filePath = Paths.get(getStoragePath(user).toString(), name);
            fileInfo = mapPathToFileInfoDto(filePath).orElse(null);
        } catch (Exception e){
            log.error("Failed fetching", e);
            throw e;
        }
        return fileInfo;
    }
}
