package com.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.mapper.CoursePublishPreMapper;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.model.po.CoursePublishPre;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.TeachPlanService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * @author 31331
 * @version 1.0
 * @description 课程发布相关接口实现
 * @date 2023/3/26 12:21
 */

@Slf4j
@Service
public class CoursePublishServiceImpl implements CoursePublishService {

    @Autowired
    CourseBaseInfoService courseBaseInfoService;
    @Autowired
    CourseBaseMapper courseBaseMapper;
    @Autowired
    TeachPlanService teachPlanService;
    @Autowired
    CourseMarketMapper courseMarketMapper;
    @Autowired
    CoursePublishPreMapper coursePublishPreMapper;
    @Autowired
    CoursePublishMapper coursePublishMapper;
    @Autowired
    MqMessageService mqMessageService;
    @Autowired
    CoursePublishService coursePublishService;
    @Autowired
    MediaServiceClient mediaServiceClient;

    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {

        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();

        //准备课程基本信息和课程计划
        CourseBaseInfoDto courseBase = courseBaseInfoService.getCourseBaseById(courseId);
        List<TeachplanDto> teachplans = teachPlanService.findTeachplanTree(courseId);

        coursePreviewDto.setCourseBase(courseBase);
        coursePreviewDto.setTeachplans(teachplans);

        return coursePreviewDto;
    }

    @Override
    @Transactional
    public void commitAudit(Long companyId, Long courseId) {

        //查找课程，得到基本信息
        CourseBaseInfoDto courseBase = courseBaseInfoService.getCourseBaseById(courseId);
        String auditStatus = courseBase.getAuditStatus();
        Long companyId1 = courseBase.getCompanyId();
        String pic = courseBase.getPic();

        //校验参数
        //已审核则不能再提交
        if ("202003".equals(auditStatus)) {
            XueChengPlusException.cast("课程已提交");
        }

        //本机构只允许提交本机构的课程
        if (!companyId.equals(companyId1)) {
            XueChengPlusException.cast("无法提交非本机构的课程");
        }

        //课程图片是否填写
        if (StringUtils.isEmpty(pic)) {
            XueChengPlusException.cast("课程图片为空，请上传图片后重试");
        }

        //添加课程预发布记录
        CoursePublishPre coursePublishPre = new CoursePublishPre();

        //课程基本信息加部分课程营销信息
        BeanUtils.copyProperties(courseBase, coursePublishPre);

        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);

        //转为JSON
        String marketJSON = JSON.toJSONString(courseMarket);

        if (courseMarket != null) {
            coursePublishPre.setMarket(marketJSON);
        }

        //课程计划是否为空
        List<TeachplanDto> teachplanTree = teachPlanService.findTeachplanTree(courseId);
        if (teachplanTree.size() == 0) {
            XueChengPlusException.cast("课程计划为空！");
        }

        String teachplanJSON = JSON.toJSONString(teachplanTree);

        coursePublishPre.setTeachplan(teachplanJSON);

        //设置发布状态
        coursePublishPre.setStatus("202003");

        //设置机构id
        coursePublishPre.setCompanyId(companyId);

        //设置创建时间
        coursePublishPre.setCreateDate(LocalDateTime.now());

        //无则插入，有则更新
        CoursePublishPre temp = coursePublishPreMapper.selectById(courseId);
        if (temp == null) {
            coursePublishPreMapper.insert(coursePublishPre);
        } else {
            coursePublishPreMapper.updateById(coursePublishPre);
        }

        //更新基本表的状态
        courseBase.setAuditStatus("202003");
        courseBaseMapper.updateById(courseBase);
        courseMarketMapper.updateById(courseMarket);

    }

    @Transactional
    @Override
    public void publish(Long companyId, Long courseId) {

        //查询课程预发布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre == null) {
            XueChengPlusException.cast("课程没有审核记录，无法发布");
        }

        //课程如果没有审核通过则不允许发布
        if (!coursePublishPre.getStatus().equals("202004")) {
            XueChengPlusException.cast("课程尚未审核通过");
        }

        //向课程发布表写入数据
        CoursePublish coursePublish = new CoursePublish();
        BeanUtils.copyProperties(coursePublishPre, coursePublish);

        CoursePublish coursePublishObj = coursePublishMapper.selectById(courseId);
        if (coursePublishObj == null) {
            coursePublishMapper.insert(coursePublish);
        }else{
            coursePublishMapper.updateById(coursePublish);
        }

        //向消息表写入数据
        saveCoursePublishMessage(courseId);

        //将预发布表数据删除
        coursePublishPreMapper.deleteById(courseId);

    }

    @Override
    public File generateCourseHtml(Long courseId) {

        //最终的静态文件
        File htmlFile = null;

        try {
            Configuration configuration = new Configuration(Configuration.getVersion());

            //指定路径
            String classPath = Objects.requireNonNull(this.getClass().getResource("/")).getPath();
            //指定模板
            configuration.setDirectoryForTemplateLoading(new File(classPath + "/templates/"));
            configuration.setDefaultEncoding("utf-8");

            //获取模板
            Template template = configuration.getTemplate("course_template.ftl");

            //准备数据
            CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(courseId);
            HashMap<String, Object> map = new HashMap<>();
            map.put("model", coursePreviewInfo);

            //将数据写入模板
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);

            InputStream inputStream = IOUtils.toInputStream(html, "utf-8");

            //使用流将html写入文件
            htmlFile = File.createTempFile("coursePublish", ".html");
            FileOutputStream outputStream = new FileOutputStream(htmlFile);

            IOUtils.copy(inputStream, outputStream);
        } catch (Exception e) {
            log.error("课程静态化出现问题，课程id{}", courseId);
            e.printStackTrace();
        }

        return htmlFile;
    }

    @Override
    public void uploadCourseHtml(Long courseId, File file) {

        //将file转成MultipartFile
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);

        try {
            String upload = mediaServiceClient.upload(multipartFile, "course/" + courseId + ".html");

            if (upload == null) {
                log.debug("远程调用失败，执行降级逻辑，课程id为{}", courseId);
                XueChengPlusException.cast("上传静态文件时出现异常");
            }
        } catch (Exception e){
            e.printStackTrace();
            XueChengPlusException.cast("上传静态文件时出现异常");

        }
    }

    @Override
    public CoursePublish getCoursePublish(Long courseId){
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        return coursePublish ;
    }


    private void saveCoursePublishMessage(Long courseId){
        MqMessage mqMessage = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);

        if (mqMessage == null) {
            XueChengPlusException.cast(CommonError.UNKOWN_ERROR);
        }

    }
}
