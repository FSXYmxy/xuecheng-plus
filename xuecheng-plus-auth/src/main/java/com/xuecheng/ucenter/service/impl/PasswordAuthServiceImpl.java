package com.xuecheng.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.feignclient.CheckCodeClient;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * @author 31331
 * @version 1.0
 * @description 账号密码登录
 * @date 2023/4/13 11:28
 */

@Service("password_authservice")
public class PasswordAuthServiceImpl implements AuthService {

    @Autowired
    XcUserMapper xcUserMapper;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    CheckCodeClient checkCodeClient;

    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {

        //账号
        String username = authParamsDto.getUsername();


        //校验验证码
        //验证码对应的key
        String checkcodekey = authParamsDto.getCheckcodekey();
        //输入的验证码
        String checkcode = authParamsDto.getCheckcode();
        if (StringUtils.isEmpty(checkcode)||StringUtils.isEmpty(checkcodekey)) {
            throw new RuntimeException("请输入验证码");

        }

        Boolean verify = checkCodeClient.verify(checkcodekey, checkcode);
        if (!verify || verify == null) {
            throw new RuntimeException("验证码错误");
        }


        //账号是否存在
        LambdaQueryWrapper<XcUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(XcUser::getUsername, username);
        XcUser xcUser = xcUserMapper.selectOne(queryWrapper);

        //返回空框架会自动抛出异常
        if (xcUser == null) {
            throw new RuntimeException("账号不存在");
        }

        //验证密码是否正确
        String password = xcUser.getPassword();
        if (!passwordEncoder.matches(authParamsDto.getPassword(), password)) {
            throw new RuntimeException("账号或密码错误");
        }

        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(xcUser, xcUserExt);

        return xcUserExt;
    }
}
