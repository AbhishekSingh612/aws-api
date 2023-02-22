package com.example.aws.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AWSS3File {
    String key;
    String bucket;
    long size;
    String eTag;
    String downloadLink;
}
