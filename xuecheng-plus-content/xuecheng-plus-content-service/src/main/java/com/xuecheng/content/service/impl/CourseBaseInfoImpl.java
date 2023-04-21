package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.service.CourseBaseInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author 31331
 * @version 1.0
 * @description 课程管理serviceimpl
 * @date 2023/2/8 22:46
 */

@Slf4j
@Service
public class CourseBaseInfoImpl implements CourseBaseInfoService {

    @Autowired
    CourseBaseMapper courseBaseMapper;
    @Autowired
    CourseMarketMapper courseMarketMapper;
    @Autowired
    CourseCategoryMapper courseCategoryMapper;
    @Autowired
    CourseMarketServiceImpl courseMarketService;


    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams params, QueryCourseParamsDto queryCourseParamsDto) {

        //构造查询条件
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        //根据课程名称
        queryWrapper.like(StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()),
                CourseBase::getName, queryCourseParamsDto.getCourseName());

        //根据审核状态
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()),
                CourseBase::getAuditStatus, queryCourseParamsDto.getAuditStatus());

        //根据发布状态
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getPublishStatus()),
                CourseBase::getStatus, queryCourseParamsDto.getPublishStatus());

        //根据培训机构id查询
        queryWrapper.eq(CourseBase::getCompanyId, companyId);


        //构建分页条件
        Page<CourseBase> courseBasePage = new Page<>(params.getPageNo(), params.getPageSize());

        //查询分页
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(courseBasePage, queryWrapper);

        //准备返回数据
        List<CourseBase> items = pageResult.getRecords();
        long counts = pageResult.getTotal();

        return new PageResult<CourseBase>(items, counts, params.getPageNo(), params.getPageSize());
    }

    @Override
    @Transactional
    public CourseBaseInfoDto getCourseBaseById(Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);

        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();

        if (courseBase == null || courseMarket == null) {
            XueChengPlusException.cast(CommonError.OBJECT_NULL);
        }

        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
        BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);

        //查询课程分类
        String mtName = courseCategoryMapper.selectById(courseBase.getMt()).getName();
        String stName = courseCategoryMapper.selectById(courseBase.getSt()).getName();

        courseBaseInfoDto.setMtName(mtName);
        courseBaseInfoDto.setStName(stName);

        return courseBaseInfoDto;
    }

    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto dto) {

        //对数据进行封装，调用mapper进行数据持久化
        CourseBase courseBase = new CourseBase();
        //将传入对象的数据设置到courseBase中
        BeanUtils.copyProperties(dto, courseBase);
        //设置机构id
        courseBase.setCompanyId(companyId);
        //设置创建时间
        courseBase.setCreateDate(LocalDateTime.now());
        //审核状态默认为未提交
        courseBase.setAuditStatus("202002");
        //发布状态默认为未发布
        courseBase.setStatus("203001");
        //向课程基本表插入数据
        int insert = courseBaseMapper.insert(courseBase);

        //获取id
        Long courseId = courseBase.getId();
        CourseMarket courseMarket = new CourseMarket();
        //将传入对象的数据设置到courseMarket中
        BeanUtils.copyProperties(dto, courseMarket);
        courseMarket.setId(courseId);

        //校验课程如果收费，则必须输入价格
        String charge = dto.getCharge();
        if (charge.equals("201001")){//收费
            if (courseMarket.getPrice() == null || courseMarket.getPrice().floatValue() <= 0){
                XueChengPlusException.cast("课程收费，价格不能为空且必须大于0");
            }
        }



        //向课程营销表插入数据
//        int insert1 = courseMarketMapper.insert(courseMarket);
        int insert1 = saveCourseMarket(courseMarket);

        //校验数据
        if (insert < 1 || insert1 <1){
            throw new RuntimeException("添加课程失败");
        }

        //组装要返回的结果
        CourseBaseInfoDto courseBaseInfoDto = getCourseBaseInfoDto(courseId);

        return courseBaseInfoDto;
    }

    @Transactional
    @Override
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto dto) {

        Long id = dto.getId();
        CourseBase courseBase = courseBaseMapper.selectById(id);

        //校验
        if (courseBase == null) {
            XueChengPlusException.cast("课程不存在");
        }

        //校验本机构只能修改本机构的课程
        if (!companyId.equals(courseBase.getCompanyId())){
            //不符合
            XueChengPlusException.cast("无法修改非本机构的课程");
        }

        //封装数据
        //封装基本数据
        BeanUtils.copyProperties(dto, courseBase);
        courseBase.setChangeDate(LocalDateTime.now());

        int i = courseBaseMapper.updateById(courseBase);
        if (i<0) {
            XueChengPlusException.cast("课程基本信息更新失败");
        }


        //封装营销信息
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(dto, courseMarket);

        int i1 = saveCourseMarket(courseMarket);
        if (i1<0) {
            XueChengPlusException.cast("课程营销信息更新失败");

        }

        //查询课程信息
        CourseBaseInfoDto courseBaseById = getCourseBaseById(id);
        return courseBaseById;
    }


    /**
    * @description 根据课程id查询课程的基本信息和营销信息
    * @param courseId 课程id
    * @return 课程的信息
    * @author 31331
    * @date 2023/2/14 15:10
    */
    public CourseBaseInfoDto getCourseBaseInfoDto(Long courseId){

        //初始化返回对象
        CourseBaseInfoDto dto = new CourseBaseInfoDto();

        //课程的基本信息
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        //课程的营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);

        //校验数据
        if (courseBase == null || courseMarket == null) {
            XueChengPlusException.cast(CommonError.OBJECT_NULL);
        }

        BeanUtils.copyProperties(courseBase, dto);
        BeanUtils.copyProperties(courseMarket, dto);

        //根据课程课程分类的id查询分类的名称
        String mt = courseBase.getMt();
        String st = courseBase.getSt();

        CourseCategory mtCategory = courseCategoryMapper.selectById(mt);
        CourseCategory stCategory = courseCategoryMapper.selectById(st);

        if (mtCategory != null) {
            String name = mtCategory.getName();
            dto.setMtName(name);
        }
        if (stCategory != null) {
            String name = stCategory.getName();
            dto.setStName(name);
        }
        return dto;
    }

    private int saveCourseMarket(CourseMarket courseMarket){

        //校验课程如果收费，则必须输入价格
        String charge = courseMarket.getCharge();
        if (charge == null) {
            XueChengPlusException.cast("未选择收费规则");
        }

        if (charge.equals("201001")){//收费
            if (courseMarket.getPrice() == null || courseMarket.getPrice().floatValue() <= 0){
                XueChengPlusException.cast("课程收费，价格不能为空且必须大于0");
            }
        }

        boolean b = courseMarketService.saveOrUpdate(courseMarket);

        return b? 1:-1;
    }
}
