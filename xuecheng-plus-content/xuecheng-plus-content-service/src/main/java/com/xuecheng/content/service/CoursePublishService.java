package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.po.CoursePublish;

import java.io.File;

/**
 * @author 31331
 * @version 1.0
 * @description TODO
 * @date 2023/3/26 12:21
 */

public interface CoursePublishService {

    CoursePreviewDto getCoursePreviewInfo(Long courseId);

    /**
     * @description 提交审核
     * @param courseId  课程id
     * @return void
     * @author Mr.M
     * @date 2022/9/18 10:31
     */
    public void commitAudit(Long companyId,Long courseId);

    /**
    * @description 课程发布
    * @param companyId 
     * @param courseId 
    * @return void
    * @author 31331
    * @date 2023/4/1 11:37
    */
    void publish(Long companyId, Long courseId);

    /**
    * @description 生成课程静态化页面
    * @param courseId
    * @return java.io.File
    * @author 31331
    * @date 2023/4/8 11:23
    */
    public File generateCourseHtml(Long courseId);

    /**
    * @description 上传课程静态化页面
    * @param courseId
     * @param file
    * @return void
    * @author 31331
    * @date 2023/4/8 11:23
    */
    public void  uploadCourseHtml(Long courseId,File file);


    /**
    * @description 获取课程发布记录
    * @param courseId
    * @return com.xuecheng.content.model.po.CoursePublish
    * @author 31331
    * @date 2023/4/17 17:33
    */
    CoursePublish getCoursePublish(Long courseId);
}
