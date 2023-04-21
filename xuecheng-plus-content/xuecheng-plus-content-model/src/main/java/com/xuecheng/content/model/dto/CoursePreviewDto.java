package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.CourseBase;
import lombok.Data;

import java.util.List;

/**
 * @author 31331
 * @version 1.0
 * @description TODO
 * @date 2023/3/26 12:16
 */

@Data
public class CoursePreviewDto extends CourseBase {

    //课程基本信息，营销信息
    private CourseBaseInfoDto courseBase;


    //课程计划信息
    private List<TeachplanDto> teachplans;



    //课程师资信息

}
