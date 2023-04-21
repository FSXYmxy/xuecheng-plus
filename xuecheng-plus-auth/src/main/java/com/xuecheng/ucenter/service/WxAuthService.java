package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.po.XcUser;

/**
 * @author 31331
 * @version 1.0
 * @description 微信扫码接入
 * @date 2023/4/15 11:05
 */

public interface WxAuthService {

    /**
    * @description 扫码认证后，申请令牌，携带令牌查询用户信息并保存到数据库
    * @param code 授权码
    * @return com.xuecheng.ucenter.model.po.XcUser
    * @author 31331
    * @date 2023/4/15 11:06
    */
    XcUser wxAuth(String code);
}
