package com.oscarhanke.s3uploaddownloadexample.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {

    private final AmazonS3 s3Client;

    @Value("${application.bucket.name}")
    private String bucketName;

    public String uploadFile(MultipartFile file){
        File convertedFile = convertMultiPartFileToFile(file);
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        s3Client.putObject(new PutObjectRequest(bucketName, fileName, convertedFile));
        convertedFile.delete();
        return "File uploaded: " + fileName;
    }

    public byte[] downloadFile(String fileName){
        S3Object s3Object = s3Client.getObject(bucketName, fileName);
        S3ObjectInputStream inputStream = s3Object.getObjectContent();
        try {
            byte[] content = IOUtils.toByteArray(inputStream);
            return content;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String deleteFileFromBucket(String fileName){
        s3Client.deleteObject(bucketName, fileName);
        return fileName + " removed...";
    }

    private File convertMultiPartFileToFile(MultipartFile file){
        if (file == null){
            throw new IllegalStateException("File cannot be null");
        }
        File convertFile = new File(file.getOriginalFilename());
        try (FileOutputStream fileOutputStream = new FileOutputStream(convertFile)) {
            fileOutputStream.write(file.getBytes());
        } catch (IOException exception) {
            log.error("Error converting multipartFile to file", exception);
        }
        return convertFile;
    }
}
