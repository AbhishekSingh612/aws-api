package com.example.aws.controller;

import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.example.aws.dto.AWSS3File;
import com.example.aws.service.AwsS3Service;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/s3")
public class AWSS3Controller {

    @Autowired
    private AwsS3Service s3Service;

    @GetMapping("/buckets/{bucketName}")
    public ResponseEntity<List<AWSS3File>> getS3Files(@RequestParam(value = "lastModified", required = false)
                                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                          Date lastModifiedFilter,
                                                     @PathVariable String bucketName){
        log.debug("Started getS3Files in bucket :{}, lastModified filter : {}",bucketName, lastModifiedFilter);

        if(StringUtils.isBlank(bucketName)){
            log.error("Bucket name is blank. Please provide a valid bucket name");
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.BAD_REQUEST) ;
        }

        List<AWSS3File> awss3Files =  s3Service.getBucketFiles(bucketName, lastModifiedFilter);

        log.debug("Ended getS3Files and Found {} Files",awss3Files.size());
        return new ResponseEntity<>(awss3Files, HttpStatus.OK);
    }



    @GetMapping("/download/{bucketName}")
    public ResponseEntity<Resource> downloadS3File(@RequestParam String key,
                                                   @PathVariable String bucketName){

        S3Object s3object = s3Service.getS3Object(key, bucketName);
        S3ObjectInputStream inputStream = s3object.getObjectContent();
        InputStreamResource resource = new InputStreamResource(inputStream);

        String[] s = key.split("/");
        String fileName = s[s.length-1];

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentLength(s3object.getObjectMetadata().getContentLength())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }


}
