package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;

import java.util.List;

/**
 * @author 31331
 * @version 1.0
 * @description TODO
 * @date 2023/3/1 21:00
 */

public interface TeachPlanService{

    /**
    * @description 查找课程计划（树形结构）
    * @param courseId
    * @return List<TeachplanDto>
    * @author 31331
    * @date 2023/3/4 11:29
    */
    public List<TeachplanDto> findTeachplanTree(Long courseId);

    /**
    * @description 保存或修改课程计划
    * @param teachplanDto
    * @return void
    * @author 31331
    * @date 2023/3/4 11:28
    */
    public void saveTeachplan(SaveTeachplanDto teachplanDto);

    public void associateMedia(BindTeachplanMediaDto bindTeachplanMediaDto);

    void disAssociateMedia(Long teachplanId, String mediaId);
}
