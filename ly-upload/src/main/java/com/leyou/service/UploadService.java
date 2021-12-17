package com.leyou.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.config.UploadProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;


@Service
@Slf4j
@EnableConfigurationProperties(UploadProperties.class)
public class UploadService {

    @Autowired
    private UploadProperties properties;

    @Autowired
    private FastFileStorageClient storageClient;

    //定义允许通过的文件类型
    //private static final List<String> ALLOW_TYPES = Arrays.asList("image/jpeg","image/png","image/bmp");

    public String uploadImage(MultipartFile file) {
        try {
            // 校验文件类型
            //获取上传文件类型
            String contentType = file.getContentType();
            if (!properties.getAllowTypes().contains(contentType)){
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }
            //校验文件内容
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null){
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }

           /* //准备目标路径
            File dest = new File("E:\\IdeaProjects\\java-workplace\\uploadImage",file.getOriginalFilename());
            //保存文件到本地，
            file.transferTo(dest);*/

            //上传到FastDFS
            //获得文件后缀名
            String extension = StringUtils.substringAfterLast(file.getOriginalFilename(), ".");
            //上传
            StorePath storePath = storageClient.uploadFile(file.getInputStream(), file.getSize(), extension, null);

            //返回路径
            return properties.getBaseUrl()+storePath.getFullPath();
        } catch (IOException e) {
           //上传失败
            log.error("上传文件失败！",e);
            throw new LyException(ExceptionEnum.UPLOAD_FILE_ERROR);
        }

    }
}
