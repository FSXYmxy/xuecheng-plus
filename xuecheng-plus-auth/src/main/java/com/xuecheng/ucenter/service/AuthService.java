package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;

/**
 * @author 31331
 * @version 1.0
 * @description 统一认证接口
 * @date 2023/4/13 11:26
 */

public interface AuthService {

    XcUserExt execute(AuthParamsDto authParamsDto);
}
