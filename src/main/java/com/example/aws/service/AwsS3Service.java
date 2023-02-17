package com.example.aws.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.example.aws.dto.AWSS3File;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class AwsS3Service {

    @Autowired
    private AmazonS3 s3client;


    public List<AWSS3File> getBucketFiles(String bucketName, Date lastModifiedFilter) {

        List<AWSS3File> awss3Files = new ArrayList<>();

        // Get List of objects in the given bucket
        ObjectListing objectListing = s3client.listObjects(bucketName); // TODO Handle truncated results

        // Filter the objects
        for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
            Date lastModified = objectSummary.getLastModified();
            log.debug("Object Summary : {}",objectSummary);
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

        return awss3Files;
    }

    public S3Object getS3Object(String key, String bucketName) {
        S3Object s3object = s3client.getObject(bucketName, key);
        return s3object;
    }
}
