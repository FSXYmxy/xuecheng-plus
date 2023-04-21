package com.xuecheng.content.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author 31331
 * @version 1.0
 * @description freemarker入门程序
 * @date 2023/3/25 14:09
 */

@Controller
public class FreeMarkerController {

    @GetMapping("/testFreeMarker")
    public ModelAndView test(){
        ModelAndView modelAndView = new ModelAndView();

        //指定模型
        modelAndView.addObject("name", "孟祥越");
        //根据视图名称.ftl找到视图模板
        modelAndView.setViewName("test");

        return modelAndView;
    }
}
