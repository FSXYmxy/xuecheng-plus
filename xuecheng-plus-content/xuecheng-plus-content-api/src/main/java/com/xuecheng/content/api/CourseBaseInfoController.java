package com.xuecheng.content.api;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 31331
 * @version 1.0
 * @description 课程信息编辑接口
 * @date 2023/2/5 15:56
 */

@Api(value = "课程信息编辑接口",tags = "课程信息编辑接口")
@RestController
@RequestMapping("/course")
public class CourseBaseInfoController {

    @Autowired
    CourseBaseInfoService courseBaseInfoService;

    @ApiOperation("课程查询接口")
    @PostMapping("/list")
    public PageResult<CourseBase> list(PageParams params, @RequestBody QueryCourseParamsDto queryCourseParamsDto){
//        CourseBase courseBase = new CourseBase();
//        courseBase.setName("测试名称");
//        courseBase.setChangeDate(LocalDateTime.now());
//
//        List<CourseBase> courseBases = new ArrayList<>();
//        courseBases.add(courseBase);
//
//        return new PageResult<>(courseBases, 10, 1, 10);

        PageResult<CourseBase> pageResult = courseBaseInfoService.queryCourseBaseList(params, queryCourseParamsDto);

        return pageResult;
    }

}
