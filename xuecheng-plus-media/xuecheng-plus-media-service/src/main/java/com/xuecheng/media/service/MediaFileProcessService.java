package com.xuecheng.media.service;

import com.xuecheng.media.model.po.MediaProcess;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MediaFileProcessService {

    /**
     * @description 获取待处理任务
     * @param shardIndex 分片序号
     * @param shardTotal 分片总数
     * @param count 获取记录数
     * @return java.util.List<com.xuecheng.media.model.po.MediaProcess>
     * @author mengxiangyue
     * @date 2022/9/14 14:49
    */
    List<MediaProcess> getMediaProcessList(int shardTotal, int shardIndex, int count);


    /**
    * @description 开启任务
    * @param id 任务id
    * @return true开启任务成功， false失败
    * @author 31331
    * @date 2023/3/21 9:31
    */
    boolean startTask(@Param("id") Long id);

    /**
     * @param taskId   任务id
     * @param status   任务状态
     * @param fileId   文件id
     * @param url      url
     * @param errorMsg 错误信息
     * @return void
     * @return void
     * @description TODO
     * @author 31331
     * @date 2023/3/21 10:02
     */
    void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg);


}
