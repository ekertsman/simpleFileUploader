package fileUploader.dao;

import fileUploader.dto.FileInfo;
import fileUploader.utils.ConcurrentHashMapLocker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

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
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// this class mimics DAO layer interacting with DB
@Slf4j
@Component
public class FileInfoDAOImpl implements IFileInfoDAO {
    private static final String STORAGE_FOLDER_NAME = "filestorage";

    private Path getStoragePath() {
        Path currentPath = Paths.get(System.getProperty("user.dir"));
        Path filePath = Paths.get(currentPath.toString(), STORAGE_FOLDER_NAME);
        File f = new File(filePath.toString());
        if (!f.exists()){
            log.info("Folder " + STORAGE_FOLDER_NAME + " does not exist. Folder created.");
            f.mkdir();
        }
        log.info("Storage path is: " + filePath);
        return filePath;
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

    private <T> List<T> fetchFiles(String actionName, Function<Path, T> func){
        log.info(String.format("Fetching %s starts...", actionName));
        Path storagePath = getStoragePath();
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
    public List<FileInfo> getAllFiles() {
        return fetchFiles("all files info", path -> mapPathToFileInfoDto(path).orElse(null));
    }

    @Override
    public List<String> getAllFileNames() {
        return fetchFiles("all file names", path -> path.getFileName().toString());
    }

    @Override
    public void uploadFile(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        log.info("Uploading file " + filename);
        InputStream is = file.getInputStream();
        Path currentPath = getStoragePath();

        Path filePath = Paths.get(currentPath.toString(), filename);

        // sync on file name as system is used directly, so files with the same name are placed one by one
        // though it causes rewrite, that's ok in this scenario
        Object locker = ConcurrentHashMapLocker.getLockerString(filename);
        synchronized (locker) {
            Files.copy(is, filePath, StandardCopyOption.REPLACE_EXISTING);
        }
        log.info("File " + filename + " successfully uploaded");
    }
}
