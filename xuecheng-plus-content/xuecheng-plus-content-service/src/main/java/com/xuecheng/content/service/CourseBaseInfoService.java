package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;

/**
 * @author 31331
 * @version 1.0
 * @description 课程管理service
 * @date 2023/2/8 22:32
 */

public interface CourseBaseInfoService {


    /**
     * @param params               分页参数
     * @param queryCourseParamsDto 查询条件
     * @return com.xuecheng.base.model.PageResult<com.xuecheng.content.model.po.CourseBase>
     * @description 课程查询
     * @author 31331
     * @date 2023/2/8 22:45
     */
    public PageResult<CourseBase> queryCourseBaseList(PageParams params, QueryCourseParamsDto queryCourseParamsDto);


    /**
    * @description 根据id查询课程
    * @param courseId 课程id
    * @return com.xuecheng.content.model.dto.CourseBaseInfoDto
    * @author 31331
    * @date 2023/3/1 11:01
    */
    public CourseBaseInfoDto getCourseBaseById(Long courseId);


    /**
    * @description 新增课程
    * @param companyId 培训机构id
     * @param addCourseDto 新增课程的信息
    * @return com.xuecheng.content.model.dto.CourseBaseInfoDto
    * @author 31331
    * @date 2023/2/14 14:17
    */
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto);


    /**
    * @description 修改课程
    * @param companyId 机构id，只能修改本机构id
    * @param dto
    * @return com.xuecheng.content.model.dto.CourseBaseInfoDto
    * @author 31331
    * @date 2023/3/1 11:05
    */
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto dto);
}
