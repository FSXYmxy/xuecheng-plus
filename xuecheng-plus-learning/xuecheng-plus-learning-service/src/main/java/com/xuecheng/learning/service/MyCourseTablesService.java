package com.xuecheng.learning.service;

import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;

/**
 * @author 31331
 * @version 1.0
 * @description TODO
 * @date 2023/4/17 18:10
 */

public interface MyCourseTablesService {
    XcChooseCourseDto addChooseCourse(String userId, Long courseId);

    XcCourseTablesDto getLearningStatus(String userId, Long courseId);
}
