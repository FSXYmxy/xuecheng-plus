package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.ucenter.mapper.XcMenuMapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcMenu;
import com.xuecheng.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 31331
 * @version 1.0
 * @description TODO
 * @date 2023/4/12 21:18
 */

@Slf4j
@Component
public class UserServiceImpl implements UserDetailsService {

    @Autowired
    XcUserMapper xcUserMapper;
    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    XcMenuMapper xcMenuMapper;

    //根据用户名查询账号
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {

        //将传入的json转为对象
        AuthParamsDto authParamsDto = null;
        try {
            authParamsDto = JSON.parseObject(s, AuthParamsDto.class);
        } catch (Exception e) {
            throw new RuntimeException("请求认证的参数不符合要求");
        }

        //确定认证类型
        String authType = authParamsDto.getAuthType();
        //根据认证类型确定要注入的bean
        String beanName = authType + "_authservice";
        AuthService authService = applicationContext.getBean(beanName, AuthService.class);
        //调用统一的认证接口，完成认证
        XcUserExt xcUserExt = authService.execute(authParamsDto);

        //封装并返回用户信息
        return getUserPrincipal(xcUserExt);
    }

    private UserDetails getUserPrincipal(XcUserExt xcUserExt) {
        String[] authorities = {"test"};
        //根据用户id查询权限权限
        List<XcMenu> xcMenus = xcMenuMapper.selectPermissionByUserId(xcUserExt.getId());
        if (xcMenus.size()>0) {
            List<String > permissions = new ArrayList<>();
            xcMenus.forEach(m -> {
                //拿到了用户所拥有的权限标识符
                permissions.add(m.getCode());
            });

            authorities = permissions.toArray(new String[0]);
        }

        //扩充信息,但是需要保护隐私
        String userJson = JSON.toJSONString(xcUserExt);
        UserDetails userDetails = User.withUsername(userJson).password(xcUserExt.getPassword()).authorities(authorities).build();
        xcUserExt.setPassword(null);

        return userDetails;
    }
}
