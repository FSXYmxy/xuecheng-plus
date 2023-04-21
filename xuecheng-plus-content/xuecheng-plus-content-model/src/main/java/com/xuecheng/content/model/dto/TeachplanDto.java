package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author 31331
 * @version 1.0
 * @description TODO
 * @date 2023/3/1 17:26
 */

@Data
@ToString
public class TeachplanDto extends Teachplan {

    //子目录
    List<TeachplanDto> teachPlanTreeNodes;

    //媒资信息
    TeachplanMedia teachplanMedia;

}
