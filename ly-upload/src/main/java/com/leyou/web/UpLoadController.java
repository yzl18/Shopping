package com.leyou.web;

import com.leyou.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload")
public class UpLoadController {

    @Autowired
    private UploadService uploadService;
    /*图片上传*/
    @PostMapping("/image")
    public ResponseEntity<String> uploadImage(@RequestParam("file")MultipartFile file){
        String url = uploadService.uploadImage(file);
        return ResponseEntity.ok(url);
    }
}