package com.xuecheng.content;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CourseCategoryService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @author 31331
 * @version 1.0
 * @description TODO
 * @date 2023/2/7 13:27
 */

@SpringBootTest
public class ContentServiceTest {

    @Autowired
    CourseBaseMapper courseBaseMapper;
    @Autowired
    CourseBaseInfoService courseBaseInfoService;
    @Autowired
    CourseCategoryService courseCategoryService;

    @Test
    void testCBM(){
        CourseBase courseBase = courseBaseMapper.selectById(22);

        Assertions.assertNotNull(courseBase);

        System.out.println(courseBase);
    }

    @Test
    void testQueryCourseBaseList(){
        PageResult<CourseBase> pageResult = courseBaseInfoService.queryCourseBaseList(new PageParams(2, 10), new QueryCourseParamsDto());

        System.out.println(pageResult);
    }


    @Test
    void testQueryCategoryTreeNodes(){
        List<CourseCategoryTreeDto> categoryTreeDtos = courseCategoryService.queryTreeNodes("1");

        System.out.println(categoryTreeDtos);
    }
}
