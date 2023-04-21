package com.xuecheng.content;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.service.CoursePublishService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.assertj.core.util.Files;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Objects;

/**
 * @author 31331
 * @version 1.0
 * @description 测试
 * @date 2023/4/2 14:32
 */

@SpringBootTest
public class FreeMarkerTest {
    @Autowired
    CoursePublishService coursePublishService;

    @Test
    public void testGenerateHtmlByTemplate() throws IOException, TemplateException {

        Configuration configuration = new Configuration(Configuration.getVersion());

        String classPath = Objects.requireNonNull(this.getClass().getResource("/")).getPath();
        configuration.setDirectoryForTemplateLoading(new File(classPath + "/templates/"));
        configuration.setDefaultEncoding("utf-8");

        Template template = configuration.getTemplate("course_template.ftl");

        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(18L);
        HashMap<String, Object> map = new HashMap<>();
        map.put("model", coursePreviewInfo);

        String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);

        InputStream inputStream = IOUtils.toInputStream(html, "utf-8");
        FileOutputStream outputStream = new FileOutputStream(Files.newFile("D://develop//html//20.html"));

        IOUtils.copy(inputStream, outputStream);
    }


}
