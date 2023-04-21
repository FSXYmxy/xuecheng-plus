package com.xuecheng.content.jobhandler;

import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.feignclient.CourseIndex;
import com.xuecheng.content.feignclient.SearchServiceClient;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @author 31331
 * @version 1.0
 * @description 课程发布任务
 * @date 2023/4/2 10:33
 */


@Slf4j
@Component
public class CoursePublishTask extends MessageProcessAbstract {

    @Autowired
    CoursePublishService coursePublishService;
    @Autowired
    CoursePublishMapper coursePublishMapper;
    @Autowired
    SearchServiceClient searchServiceClient;

    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler() throws Exception{
        //分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();

        //调用抽象类的方法执行任务
        process(shardIndex, shardTotal, "course_publish", 30, 60);

    }

    //执行课程发布任务的逻辑
    @Override
    public boolean execute(MqMessage mqMessage) {

        //从mqMessage拿课程id
        long courseId = Long.parseLong(mqMessage.getBusinessKey1());

        //课程静态化，并上传到minio
        generateCourseHtml(mqMessage, courseId);

        //向elasticsearch写索引
        saveCourseIndex(mqMessage, courseId);

        //向redis写缓存



        //任务完成
        return true;
    }

    //生成课程静态化页面并上传到文件系统
    private void generateCourseHtml(MqMessage mqMessage, Long courseId){

        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();

        //做任务幂等性处理
        //查询数据库取出该阶段执行状态
        int stageOne = mqMessageService.getStageOne(taskId);
        if (stageOne>0) {
            log.debug("课程静态化已完成，无需处理");
            return;
        }

        //开始课程静态化
        File file = coursePublishService.generateCourseHtml(courseId);
        if (file == null) {
            XueChengPlusException.cast(CommonError.OBJECT_NULL);
        }

        //上传页面到minio
        coursePublishService.uploadCourseHtml(courseId, file);

        //任务处理完成，记录状态
        mqMessageService.completedStageOne(courseId);

    }

    //保存课程索引信息
    private void saveCourseIndex(MqMessage mqMessage, long courseId) {

        //任务id
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();

        //做任务幂等性处理
        //查询数据库取出该阶段执行状态
        int stageTwo = mqMessageService.getStageTwo(taskId);
        if (stageTwo>0) {
            log.debug("索引已生成，无需处理");
            return;
        }

        //查询课程信息，调用搜索服务生成索引
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        CourseIndex courseIndex = new CourseIndex();

        BeanUtils.copyProperties(coursePublish, courseIndex);

        Boolean add = searchServiceClient.add(courseIndex);
        if (!add) {
            XueChengPlusException.cast("远程调用搜索服务添加课程索引失败");
        }

        //任务处理完成
        mqMessageService.completedStageTwo(courseId);

    }
}
