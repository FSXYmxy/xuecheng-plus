package com.xuecheng.media;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.UploadObjectArgs;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterInputStream;

/**
* @description
* @author 31331
* @date 2023/3/9 10:01
*/
public class MinIOTest {

    static MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://192.168.101.65:9000")
                    .credentials("minioadmin", "minioadmin")
                    .build();

    @Test
    public void upload() {
        try {
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket("test")
                            .object("1.jfif")
                            .filename("D:\\DevApps\\minio\\source\\1.jfif")
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void upload2() {
        try {
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket("test")
                            .object("1/2/3/test/1.jfif")
                            .filename("D:\\DevApps\\minio\\source\\1.jfif")
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void remove() {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket("test")
                            .object("test/1.jfif")
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    public void download() throws Exception {
        FilterInputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                .bucket("test")
                .object("1.jfif")
                .build());

        String filePath = "D:\\DevApps\\minio\\sourceCopy\\";
        if (! new File(filePath).exists()) {
            boolean mkdir = new File(filePath).mkdirs();
        }
        FileOutputStream fileOutputStream = new FileOutputStream(filePath + "1.jfif");

        if (inputStream != null) {
            IOUtils.copy(inputStream, fileOutputStream);
        }



    }
}