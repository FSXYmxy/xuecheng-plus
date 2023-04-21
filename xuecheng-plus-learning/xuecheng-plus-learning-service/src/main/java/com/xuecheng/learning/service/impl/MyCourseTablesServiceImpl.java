package com.xuecheng.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.learning.mapper.XcChooseCourseMapper;
import com.xuecheng.learning.mapper.XcCourseTablesMapper;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcChooseCourse;
import com.xuecheng.learning.model.po.XcCourseTables;
import com.xuecheng.learning.service.MyCourseTablesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author 31331
 * @version 1.0
 * @description TODO
 * @date 2023/4/17 18:10
 */


@Slf4j
@Service
public class MyCourseTablesServiceImpl implements MyCourseTablesService {

    @Autowired
    XcChooseCourseMapper chooseCourseMapper;
    @Autowired
    XcCourseTablesMapper courseTablesMapper;
    @Autowired
    ContentServiceClient contentServiceClient;

    @Transactional
    @Override
    public XcChooseCourseDto addChooseCourse(String userId, Long courseId) {

        //查询课程收费规则
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);
        if (coursepublish == null) {
            XueChengPlusException.cast("课程不存在");
        }

        XcChooseCourse chooseCourse = null;

        String charge = coursepublish.getCharge();
        if ("201000".equals(charge)) {

            //免费，向选课记录表，我的课程表添加记录
            chooseCourse = addFreeCoruse(userId, coursepublish);
            XcCourseTables xcCourseTables = addCourseTabls(chooseCourse);


        } else {
            //收费，向选课记录表添加记录
            chooseCourse = addChargeCoruse(userId, coursepublish);

        }

        //判断学生学习资格
        XcCourseTablesDto learningStatus = getLearningStatus(userId, courseId);

        XcChooseCourseDto xcChooseCourseDto = new XcChooseCourseDto();
        BeanUtils.copyProperties(chooseCourse, xcChooseCourseDto);

        //设置学习资格
        xcChooseCourseDto.setLearnStatus(learningStatus.getLearnStatus());

        return xcChooseCourseDto;

    }


    //添加免费课程,免费课程加入选课记录表、我的课程表
    public XcChooseCourse addFreeCoruse(String userId, CoursePublish coursepublish) {

        //无则增加，有则返回
        LambdaQueryWrapper<XcChooseCourse> queryWrapper = new LambdaQueryWrapper<XcChooseCourse>()
                .eq(XcChooseCourse::getUserId, userId)
                .eq(XcChooseCourse::getCourseId, coursepublish.getId())
                .eq(XcChooseCourse::getOrderType, "700001")//免费课程
                .eq(XcChooseCourse::getStatus, "701001");//选课成功

        List<XcChooseCourse> courseList = chooseCourseMapper.selectList(queryWrapper);
        if (courseList.size()>0) {
            return courseList.get(0);
        }

        //向选课记录表记录数据
        XcChooseCourse chooseCourse = new XcChooseCourse();

        chooseCourse.setCourseId(coursepublish.getId());
        chooseCourse.setCourseName(coursepublish.getName());
        chooseCourse.setUserId(userId);
        chooseCourse.setCompanyId(coursepublish.getCompanyId());
        chooseCourse.setOrderType("701001");
        chooseCourse.setCreateDate(LocalDateTime.now());
        chooseCourse.setCoursePrice(coursepublish.getPrice());
        chooseCourse.setValidDays(365);
        chooseCourse.setStatus("701001");//选课成功
        chooseCourse.setValidtimeStart(LocalDateTime.now());
        chooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));//有效时间

        int insert = chooseCourseMapper.insert(chooseCourse);

        if (insert<0) {
            XueChengPlusException.cast("添加选课记录失败");
        }

        return chooseCourse;
    }

    //添加收费课程
    public XcChooseCourse addChargeCoruse(String userId,CoursePublish coursepublish){

        //如果存在收费的选课记录且状态为待支付，则直接返回
        LambdaQueryWrapper<XcChooseCourse> queryWrapper = new LambdaQueryWrapper<XcChooseCourse>()
                .eq(XcChooseCourse::getUserId, userId)
                .eq(XcChooseCourse::getCourseId, coursepublish.getId())
                .eq(XcChooseCourse::getOrderType, "700002")//免费课程
                .eq(XcChooseCourse::getStatus, "701002");//选课成功

        List<XcChooseCourse> courseList = chooseCourseMapper.selectList(queryWrapper);
        if (courseList.size()>0) {
            return courseList.get(0);
        }

        //向选课记录表记录数据
        XcChooseCourse chooseCourse = new XcChooseCourse();

        chooseCourse.setCourseId(coursepublish.getId());
        chooseCourse.setCourseName(coursepublish.getName());
        chooseCourse.setUserId(userId);
        chooseCourse.setCompanyId(coursepublish.getCompanyId());
        chooseCourse.setOrderType("701002");
        chooseCourse.setCreateDate(LocalDateTime.now());
        chooseCourse.setCoursePrice(coursepublish.getPrice());
        chooseCourse.setValidDays(365);
        chooseCourse.setStatus("701002");//选课成功
        chooseCourse.setValidtimeStart(LocalDateTime.now());
        chooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));//有效时间

        int insert = chooseCourseMapper.insert(chooseCourse);

        if (insert<0) {
            XueChengPlusException.cast("添加选课记录失败");
        }

        return chooseCourse;
    }
    //添加到我的课程表
    public XcCourseTables addCourseTabls(XcChooseCourse xcChooseCourse){

        //选课成功方可添加
        String status = xcChooseCourse.getStatus();
        if (!"701001".equals(status)) {
            XueChengPlusException.cast("选课没有成功，无法添加到课程表");
        }

        XcCourseTables courseTables = getXcCourseTables(xcChooseCourse.getUserId(), xcChooseCourse.getCourseId());
        if (courseTables != null) {
            return courseTables;
        }

        XcCourseTables xcCourseTables = new XcCourseTables();
        BeanUtils.copyProperties(xcChooseCourse, xcCourseTables);
        xcCourseTables.setChooseCourseId(xcChooseCourse.getId());
        xcCourseTables.setCourseType(xcChooseCourse.getOrderType());//选课类型
        xcCourseTables.setUpdateDate(LocalDateTime.now());

        int insert = courseTablesMapper.insert(xcCourseTables);
        if (insert < 0) {
            XueChengPlusException.cast("添加我的课程表失败");
        }

        return xcCourseTables;
    }

    /**
     * @description 根据课程和用户查询我的课程表中某一门课程
     * @param userId
     * @param courseId
     * @return com.xuecheng.learning.model.po.XcCourseTables
     * @author Mr.M
     * @date 2022/10/2 17:07
     */
    public XcCourseTables getXcCourseTables(String userId,Long courseId){
        XcCourseTables xcCourseTables = courseTablesMapper.selectOne(new LambdaQueryWrapper<XcCourseTables>().eq(XcCourseTables::getUserId, userId).eq(XcCourseTables::getCourseId, courseId));

        return xcCourseTables;

    }

    /**
    * @description 获取学习资格
    * @param userId
    * @param courseId
    * @return com.xuecheng.learning.model.dto.XcCourseTablesDto
    * @author 31331
    * @date 2023/4/17 20:23
    */
    @Override
    public XcCourseTablesDto getLearningStatus(String userId, Long courseId) {
        //准备返回的结果
        XcCourseTablesDto courseTablesDto = new XcCourseTablesDto();

        //查询我的课程表，如果查不到
        XcCourseTables courseTables = getXcCourseTables(userId, courseId);

        if (courseTables == null) {
            courseTablesDto.setLearnStatus("702002");

            return courseTablesDto;
        }

        //如果过期
        if (courseTables.getValidtimeEnd().isBefore(LocalDateTime.now())) {
            BeanUtils.copyProperties(courseTables, courseTablesDto);
            courseTablesDto.setLearnStatus("702003");

            return courseTablesDto;
        } else {
            BeanUtils.copyProperties(courseTables, courseTablesDto);
            courseTablesDto.setLearnStatus("702001");

            return courseTablesDto;
        }

    }
}
