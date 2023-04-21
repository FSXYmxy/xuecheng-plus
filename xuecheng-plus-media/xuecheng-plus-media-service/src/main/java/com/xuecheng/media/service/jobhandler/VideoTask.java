package com.xuecheng.media.service.jobhandler;

import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileProcessService;
import com.xuecheng.media.service.MediaFileService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author 31331
 * @version 1.0
 * @description TODO
 * @date 2023/3/21 10:32
 */

@Slf4j
@Component
public class VideoTask {

    @Autowired
    MediaFileService mediaFileService;

    @Autowired
    MediaFileProcessService mediaFileProcessService;

    @Value("${videoprocess.ffmpegpath}")
    String ffmpegpath;

    @XxlJob("videoJobHandler")
    public void videoJobHandler() throws Exception{

        //分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();

        List<MediaProcess> mediaProcessList = null;
        int size = 0;

        try {
            //取出可用处理器核心
            int processors = Runtime.getRuntime().availableProcessors();
            //一次处理数量不要超过处理器核心数量

            mediaProcessList = mediaFileProcessService.getMediaProcessList(shardTotal, shardIndex, processors);

            size = mediaProcessList.size();
            log.debug("取出待处理任务{}条", size);

            if (size<0) {
                return;
            }

        } catch (Exception e){
            e.printStackTrace();
            return;
        }

        //启动size个线程的线程池
        ExecutorService threadPool = Executors.newFixedThreadPool(size);
        //计数器
        CountDownLatch countDownLatch = new CountDownLatch(size);

        //将处理任务加入线程池
        mediaProcessList.forEach(mediaProcess -> {
            threadPool.execute(()->{
                try {
                    //获取任务id
                    Long taskId = mediaProcess.getId();
                    //抢占任务
                    boolean b = mediaFileProcessService.startTask(taskId);

                    if (!b){
                        return;
                    }

                    log.debug("开始执行任务{}", mediaProcess);

                    //下边是处理逻辑
                    String bucket = mediaProcess.getBucket();
                    String filePath = mediaProcess.getFilePath();
                    String fileId = mediaProcess.getFileId();
                    String filename = mediaProcess.getFilename();

                    //将要处理的文件下载到服务器上
                    File originalFile = mediaFileService.downloadFileFromMinIO(bucket, filePath);

                    if (originalFile == null) {
                        log.debug("下载待处理文件失败,originalFile:{}", mediaProcess.getBucket().concat(mediaProcess.getFilePath()));

                        mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), "3", fileId,null, "下载待处理文件失败");

                        return;
                    }

                    //处理结束的视频文件
                    File mp4File = null;

                    try {
                        mp4File = File.createTempFile("mp4", ".mp4");
                    }catch (Exception e){
                        log.error("创建临时mp4文件失败");

                        mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), "3", mediaProcess.getFilePath(), "null", "创建mp4临时文件失败");
                        return;
                    }

                    //视频处理结果
                    String result = "";

                    try {
                        //开始处理视频
                        Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpegpath, originalFile.getAbsolutePath(), originalFile.getName(), originalFile.getAbsolutePath());

                        //开始转换视频，成功则返回success
                        result = videoUtil.generateMp4();

                    } catch (Exception e){
                        e.printStackTrace();
                        log.error("处理视频文件{}出错{}", mediaProcess.getFilePath(), e.getMessage());
                    }

                    if (!result.equals("success")) {
                        log.error("处理视频失败,视频地址:{},错误信息:{}", bucket + filePath, result);
                        mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), "3", fileId, null, result);
                        return;
                    }

                    //将MP4上传至minio
                    //mp4在minio上的路径
                    String objectName = getFilePath(fileId, ".mp4");
                    //访问url
                    String url = "/" + bucket + "/" + objectName;
                    try {
                        boolean b1 = mediaFileService.addMediaFilesToMinIO(mp4File.getAbsolutePath(), "video/mp4", bucket, objectName);

                        if (!b1) {
                            log.error("上传mp4到minio失败");
                            mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), "3", fileId, null, "处理后视频上传或入库失败");
                        }

                        //将url存储至数据，并更新状态为成功，并将待处理视频记录删除存入地址
                        mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), "2", fileId, url, null);
                    } catch (Exception e){
                        log.error("上传视频失败或入库失败,视频地址:{},错误信息:{}", bucket + objectName, e.getMessage());
                        //最终还是失败
                        mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), "3", fileId, null, "处理后视频上传或入库失败");
                    }
                }finally {
                    countDownLatch.countDown();
                }
            });
        });
        //等待,给一个充裕的超时时间,防止无限等待，到达超时时间还没有处理完成则结束任务
        countDownLatch.await(30, TimeUnit.MINUTES);
    }

    //拼接文件路径
    private String getFilePath(String fileMd5,String fileExt){
        return   fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" +fileMd5 +fileExt;
    }


}
