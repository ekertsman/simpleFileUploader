package fileUploader.controllers;

import fileUploader.dto.FileInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import fileUploader.services.IFileService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@Slf4j
public class SimpleFileUploaderController {
    @Autowired
    private IFileService fileService;

    private static final String template = "Hello! Please do '/login' to authorize";

    @GetMapping("/public/greeting")
    public String greeting(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String currentUserName = authentication.getName();
            model.addAttribute("userName",  currentUserName);
            model.addAttribute("files", fileService.getAllFileNames());
        }
        return "greeting";
    }

    @GetMapping("/files")
    @ResponseBody
    public List<FileInfo> getAllFiles(Model model){
        return fileService.getAllFiles();
    }

    @PostMapping("/public/file")
    @ResponseBody
    public ResponseEntity<String> uploadFile(@RequestParam(name = "file") MultipartFile file){
        final String filename = file.getOriginalFilename();
        if (file == null || file.isEmpty()){
            final String msg = "File " + filename + " is emtpy";
            log.error("Upload failed: " + msg);
            return new ResponseEntity<>(msg, HttpStatus.BAD_REQUEST);
        }
        try{
            fileService.uploadFile(file);
            return new ResponseEntity<>("Successfully uploaded file " + filename, HttpStatus.OK);
        } catch (IOException e) {
            final String msg = "Failed file " + filename + " uploading";
            log.error(msg, e);
            return new ResponseEntity<>(msg, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/file/{name}")
    @ResponseBody
    public String getFileInfo(@PathVariable(name = "name") String name){
        System.out.println("show file " + name + " info");
        return "file " + name;
    }


}