package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachPlanService;
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
 * @date 2023/3/1 21:02
 */

@Slf4j
@Service
public class TeachplanServiceImpl implements TeachPlanService {
    @Autowired
    TeachplanMapper teachplanMapper;
    @Autowired
    TeachplanMediaMapper teachplanMediaMapper;

    @Override
    public List<TeachplanDto> findTeachplanTree(Long courseId) {
        return teachplanMapper.selectTreeNodes(courseId);
    }

    @Transactional
    @Override
    public void saveTeachplan(SaveTeachplanDto teachplanDto) {

        Long id = teachplanDto.getId();
        Teachplan teachplan = teachplanMapper.selectById(id);

        if (id == null) {
            teachplan = new Teachplan();
            BeanUtils.copyProperties(teachplanDto, teachplan);

            //查找同级别目录数,实现在添加到同级目录的最后
            int teachplanCount = getTeachplanCount(teachplanDto.getCourseId(), teachplanDto.getParentid());
            teachplan.setOrderby(teachplanCount + 1);

            teachplanMapper.insert(teachplan);
        }else {
            BeanUtils.copyProperties(teachplanDto, teachplan);

            teachplanMapper.updateById(teachplan);
        }

    }

    @Transactional
    @Override
    public void associateMedia(BindTeachplanMediaDto bindTeachplanMediaDto) {

        //教学计划id
        Long teachplanId = bindTeachplanMediaDto.getTeachplanId();
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        if(teachplan==null){
            XueChengPlusException.cast("教学计划不存在");
        }
        Integer grade = teachplan.getGrade();
        if(grade!=2){
            XueChengPlusException.cast("只允许第二级教学计划绑定媒资文件");
        }


        //先删除原有记录
        teachplanMediaMapper.delete(new LambdaQueryWrapper<TeachplanMedia>().eq(TeachplanMedia::getTeachplanId, teachplanId));

        //添加新纪录
        TeachplanMedia teachplanMedia = new TeachplanMedia();

        BeanUtils.copyProperties(bindTeachplanMediaDto, teachplanMedia);

        //老师没有装填日期和课程id，自己加
        teachplanMedia.setCourseId(teachplan.getCourseId());
        teachplanMedia.setMediaFilename(bindTeachplanMediaDto.getFileName());
        teachplanMedia.setCreatePeople("mxy");
        teachplanMedia.setChangePeople("mxy");
        teachplanMedia.setCreateDate(LocalDateTime.now());

        teachplanMediaMapper.insert(teachplanMedia);
    }

    @Override
    public void disAssociateMedia(Long teachplanId, String mediaId) {

        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        if(teachplan==null){
            XueChengPlusException.cast("教学计划不存在");
        }

        teachplanMediaMapper.delete(new LambdaQueryWrapper<TeachplanMedia>().eq(TeachplanMedia::getMediaId, mediaId));

    }

    //计算课程计划排序
    public int getTeachplanCount(Long courseId, Long parentId){
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(Teachplan::getCourseId, courseId).eq(Teachplan::getParentid, parentId);

        return teachplanMapper.selectCount(queryWrapper);
    }

}
