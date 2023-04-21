package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;

import java.util.List;

/**
 * @author 31331
 * @version 1.0
 * @description 课程分类相关操作
 * @date 2023/2/12 23:57
 */

public interface CourseCategoryService {

    /**
    * @description 课程分类查询
    * @param id 根节点id
    * @return 根节点下的所有id
    * @author 31331
    * @date 2023/2/12 23:59
    */
    List<CourseCategoryTreeDto> queryTreeNodes(String id);
}
