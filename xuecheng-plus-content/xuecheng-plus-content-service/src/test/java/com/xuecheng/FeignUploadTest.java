package com.xuecheng;

import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * @author 31331
 * @version 1.0
 * @description 测试远程调用媒资服务
 * @date 2023/4/6 13:26
 */

//@ComponentScan("com.xuecheng.content.feignclient")
@SpringBootTest
public class FeignUploadTest {

    @Autowired
    MediaServiceClient mediaServiceClient;

    @Test
    public void test() throws IOException {
        //将file转成MultipartFile
        File file = new File("D://develop//html//20.html");

        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);

        mediaServiceClient.upload(multipartFile, "course/10.html");

    }



}
