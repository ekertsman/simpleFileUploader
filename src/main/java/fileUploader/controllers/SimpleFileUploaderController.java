package fileUploader.controllers;

import fileUploader.dto.FileInfo;
import fileUploader.services.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import fileUploader.services.IFileService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Controller
@Slf4j
public class SimpleFileUploaderController {
    @Autowired
    private IFileService fileService;
    @Autowired
    private IUserService userService;

    @GetMapping("/public/greeting")
    public String greeting(Model model) {
        Optional<String> userNameOptional = userService.getCurrentUserName();
        if (userNameOptional.isPresent()) {
            final String userName = userNameOptional.get();
            model.addAttribute("userName", userName);
            model.addAttribute("files", fileService.getAllFileNames(userName));
        }
        return "greeting";
    }

    @GetMapping("/files")
    @ResponseBody
    public List<FileInfo> getUserFiles(Model model){
        String currentUser = userService.getCurrentUserName()
                .orElseThrow(() -> new IllegalAccessError("No permission "));
        log.info("Current user is " + currentUser);
        return fileService.getUserFiles(currentUser);
    }

    @PostMapping("/file")
    @ResponseBody
    public ResponseEntity<String> uploadFile(@RequestParam(name = "file") MultipartFile file){
        String currentUser = userService.getCurrentUserName()
                .orElseThrow(() -> new IllegalAccessError("No permission "));
        log.info("Current user is " + currentUser);
        final String filename = file.getOriginalFilename();
        if (file == null || file.isEmpty()){
            final String msg = "File " + filename + " is emtpy";
            log.error("Upload failed: " + msg);
            return new ResponseEntity<>(msg, HttpStatus.BAD_REQUEST);
        }
        try{
            fileService.uploadFile(file, currentUser);
            return new ResponseEntity<>("Successfully uploaded file " + filename, HttpStatus.OK);
        } catch (IOException e) {
            final String msg = "Failed file " + filename + " uploading";
            log.error(msg, e);
            return new ResponseEntity<>(msg, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/file/{name}")
    @ResponseBody
    public ResponseEntity<FileInfo> getFileInfo(@PathVariable(name = "name") String name){
        String currentUser = userService.getCurrentUserName()
                .orElseThrow(() -> new IllegalAccessError("No permission "));
        log.info("Current user is " + currentUser);
        return new ResponseEntity<>(fileService.getFile(currentUser, name), HttpStatus.OK);
    }


}