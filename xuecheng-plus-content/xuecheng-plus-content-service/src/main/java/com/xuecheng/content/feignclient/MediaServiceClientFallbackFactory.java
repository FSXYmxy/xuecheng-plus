package com.xuecheng.content.feignclient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author 31331
 * @version 1.0
 * @description TODO
 * @date 2023/4/8 10:48
 */
@Slf4j
@Component
public class MediaServiceClientFallbackFactory implements FallbackFactory<MediaServiceClient> {
    @Override
    public MediaServiceClient create(Throwable throwable) {
        //使用工厂模式可拿到熔断的异常信息
        return new MediaServiceClient() {
            //发生熔断时，上传服务利用此方法执行降级逻辑
            @Override
            public String upload(MultipartFile filedata, String objectName) throws IOException {
                log.debug("远程调用上传文件的接口发生熔断{}", throwable.toString(), throwable);

                return null;
            }
        };
    }
}
