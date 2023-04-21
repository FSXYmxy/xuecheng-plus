package com.xuecheng.content.api;

import com.xuecheng.base.exception.ValidationGroups;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.utils.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * @author 31331
 * @version 1.0
 * @description 课程信息编辑接口
 * @date 2023/2/5 15:56
 */

@Api(value = "课程信息编辑接口",tags = "课程信息编辑接口")
@RestController
public class CourseBaseInfoController {

    @Autowired
    CourseBaseInfoService courseBaseInfoService;

    @ApiOperation("课程查询接口")
    @PreAuthorize("hasAuthority('xc_teachmanager_course_list')")//指定权限标识符~
    @PostMapping("/course/list")
    public PageResult<CourseBase> list(PageParams params, @RequestBody QueryCourseParamsDto queryCourseParamsDto){

        //获取用户所属机构id
        long companyId = Long.parseLong(Objects.requireNonNull(SecurityUtil.getUser()).getCompanyId());
        PageResult<CourseBase> pageResult = courseBaseInfoService.queryCourseBaseList(params, queryCourseParamsDto);

        return pageResult;
    }

    @ApiOperation("按id查询课程")
    @GetMapping("/course/{courseId}")
    public CourseBaseInfoDto getCourseBaseById(@PathVariable Long courseId){

        //获取当前用户身份
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        System.out.println(user.getUsername());
//        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        System.out.println(principal);

        return courseBaseInfoService.getCourseBaseById(courseId);
    }

    @ApiOperation("新增课程接口")
    @PostMapping("/course")
    public CourseBaseInfoDto createCourseBase( @RequestBody @Validated(ValidationGroups.Inster.class) AddCourseDto addCourseDto){

        //获取当前用户所属培训机构
        Long companyId = 22l;

        //调用service
        CourseBaseInfoDto courseBase = courseBaseInfoService.createCourseBase(companyId, addCourseDto);
        return courseBase;
    }

//    @ApiOperation("新增课程接口")
//    @PostMapping("/course2")
//    public CourseBaseInfoDto createCourseBase2( @RequestBody @Validated(ValidationGroups.Update.class) AddCourseDto addCourseDto){
//
//        //获取当前用户所属培训机构
//        Long companyId = 22l;
//
//        //调用service
//        CourseBaseInfoDto courseBase = courseBaseInfoService.createCourseBase(companyId, addCourseDto);
//        return courseBase;
//    }

    @ApiOperation("修改课程基础信息")
    @PutMapping("/course")
    public CourseBaseInfoDto modifyCourseBase(@RequestBody @Validated EditCourseDto dto){
        //机构id，由于认证系统没有上线暂时硬编码
        Long companyId = 1232141425L;
        return courseBaseInfoService.updateCourseBase(companyId,dto);
    }

}
