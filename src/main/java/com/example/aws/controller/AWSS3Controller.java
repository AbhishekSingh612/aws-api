package com.example.aws.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.example.aws.dto.AWSS3File;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/s3")
public class AWSS3Controller {

    @Autowired
    private AmazonS3 s3client;

    @GetMapping("/buckets/{bucketName}")
    public ResponseEntity<List<AWSS3File>> getS3Files(@RequestParam("lastModified")
                                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                          Date lastModifiedFilter,
                                                     @PathVariable String bucketName){
        log.debug("Started getS3Files in bucket :{}, lastModified filter : {}",bucketName, lastModifiedFilter);
        List<AWSS3File> awss3Files = new ArrayList<>();

        if(StringUtils.isBlank(bucketName)){
            log.error("Bucket name is blank. Please provide a valid bucket name");
            return new ResponseEntity<>(awss3Files, HttpStatus.BAD_REQUEST) ;
        }

        // Get List of objects in the given bucket
        ObjectListing objectListing = s3client.listObjects(bucketName); // TODO Handle truncated results


        // Filter the objects
        for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
            Date lastModified = objectSummary.getLastModified();

            if (lastModifiedFilter==null || (lastModifiedFilter.equals(lastModified))) {

                AWSS3File file = AWSS3File.builder()
                        .key(objectSummary.getKey())
                        .bucket(objectSummary.getBucketName())
                        .size(objectSummary.getSize())
                        .eTag(objectSummary.getETag())
                        .build();
                 awss3Files.add(file);
            }
        }

        log.debug("Ended getS3Files and Found {} Files",awss3Files.size());
        return new ResponseEntity<>(awss3Files, HttpStatus.OK);
    }



    @GetMapping("/download/{bucketName}")
    public ResponseEntity<Resource> downloadS3File(@RequestParam String key,
                                                   @PathVariable String bucketName){

        S3Object s3object = s3client.getObject(bucketName, key);
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
