package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.mapper.XcUserRoleMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.model.po.XcUserRole;
import com.xuecheng.ucenter.service.AuthService;
import com.xuecheng.ucenter.service.WxAuthService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * @author 31331
 * @version 1.0
 * @description 微信扫码认证
 * @date 2023/4/13 11:29
 */

@Service("wx_authservice")
public class WxAuthServiceImpl implements AuthService, WxAuthService {

    @Autowired
    XcUserMapper xcUserMapper;
    @Autowired
    XcUserRoleMapper xcUserRoleMapper;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    WxAuthServiceImpl currentProxy;

    @Value("${weixin.appid}")
    String appid;
    @Value("${weixin.secret}")
    String secret;

    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {

        XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, authParamsDto.getUsername()));

        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(xcUser, xcUserExt);

        return xcUserExt;
    }

    @Override
    public XcUser wxAuth(String code) {
        //请求微信申请令牌
        Map<String, String> access_token_map = getAccess_token(code);

        //拿到令牌查询用户信息
        String accessToken = access_token_map.get("access_token");
        String openid = access_token_map.get("openid");
        Map<String, String> userinfo = getUserinfo(accessToken, openid);

        //将用户信息写入本项目数据库
        XcUser xcUser = currentProxy.addWxUser(userinfo);

        return xcUser;
    }

    /**
    * @description 携带授权码获取令牌
     * https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code
    * @param code
    * @return java.util.Map<java.lang.String,java.lang.String>
    * @author 31331
    * @date 2023/4/15 11:12
    */
    private Map<String, String> getAccess_token(String code) {
        String url_template = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
        //最终的请求路径
        String url = String.format(url_template, appid, secret, code);

        //远程调用此url
        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.POST, null, String.class);
        //获取响应的结果
        String result = exchange.getBody();

        Map<String, String> map = JSON.parseObject(result, Map.class);

        return map;
    }

    /**
    * @description 获取用户信息
    * @param access_token
     * @param openid
    * @return java.util.Map<java.lang.String,java.lang.String>
    * @author 31331
    * @date 2023/4/15 11:52
    */
    private Map<String,String> getUserinfo(String access_token,String openid) {
        String url_template = "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";
        //最终的请求路径
        String url = String.format(url_template, access_token, openid);
        //远程调用此url
        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.GET, null, String.class);

        //获取响应的结果
        String result = new String(exchange.getBody().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);

        Map<String, String> map = JSON.parseObject(result, Map.class);

        return map;
    }

    /**
    * @description 将用户信息保存到数据库
    * @param userInfo_map
    * @return com.xuecheng.ucenter.model.po.XcUser
    * @author 31331
    * @date 2023/4/15 11:53
    */
    @Transactional
    public XcUser addWxUser(Map userInfo_map){

        //根据unionid查询数据库
        String  unionid = userInfo_map.get("unionid").toString();

        XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getWxUnionid, unionid));
        if (xcUser != null) {
            return xcUser;
        }

        xcUser = new XcUser();
        String userId = UUID.randomUUID().toString();//主键
        xcUser = new XcUser();
        xcUser.setId(userId);
        xcUser.setWxUnionid(unionid);
        //记录从微信得到的昵称
        xcUser.setNickname(userInfo_map.get("nickname").toString());
        xcUser.setUserpic(userInfo_map.get("headimgurl").toString());
        xcUser.setName(userInfo_map.get("nickname").toString());
        xcUser.setUsername(unionid);
        xcUser.setPassword(unionid);
        xcUser.setUtype("101001");//学生类型
        xcUser.setStatus("1");//用户状态
        xcUser.setCreateTime(LocalDateTime.now());
        xcUserMapper.insert(xcUser);

        //向用户角色关系表插入数据
        XcUserRole xcUserRole = new XcUserRole();
        xcUserRole.setId(UUID.randomUUID().toString());
        xcUserRole.setUserId(userId);
        xcUserRole.setRoleId("17");//学生角色
        xcUserRole.setCreateTime(LocalDateTime.now());
        xcUserRoleMapper.insert(xcUserRole);

        return xcUser;
    }
}
