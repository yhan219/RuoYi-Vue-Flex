package org.dromara.web.service;

import cn.dev33.satoken.secure.BCrypt;
import cn.hutool.core.util.ObjectUtil;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.constant.Constants;
import org.dromara.common.core.constant.GlobalConstants;
import org.dromara.common.core.domain.model.RegisterBody;
import org.dromara.common.core.enums.UserType;
import org.dromara.common.core.exception.user.CaptchaException;
import org.dromara.common.core.exception.user.CaptchaExpireException;
import org.dromara.common.core.exception.user.UserException;
import org.dromara.common.core.utils.MessageUtils;
import org.dromara.common.core.utils.ServletUtils;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.log.event.LogininforEvent;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.common.web.config.properties.CaptchaProperties;
import org.dromara.system.domain.SysUser;
import org.dromara.system.domain.bo.SysUserBo;
import org.dromara.system.mapper.SysUserMapper;
import org.dromara.system.service.ISysUserService;
import org.springframework.stereotype.Service;

import static org.dromara.system.domain.table.SysUserTableDef.SYS_USER;

/**
 * 注册校验方法
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Service
public class SysRegisterService {

    private final ISysUserService userService;
    private final SysUserMapper userMapper;
    private final CaptchaProperties captchaProperties;

    /**
     * 注册
     */
    public void register(RegisterBody registerBody) {
        String tenantId = registerBody.getTenantId();
        String username = registerBody.getUsername();
        String password = registerBody.getPassword();
        // 校验用户类型是否存在
        String userType = UserType.getUserType(registerBody.getUserType()).getUserType();

        boolean captchaEnabled = captchaProperties.getEnable();
        // 验证码开关
        if (captchaEnabled) {
            validateCaptcha(tenantId, username, registerBody.getCode(), registerBody.getUuid());
        }
        SysUserBo sysUser = new SysUserBo();
        sysUser.setUserName(username);
        sysUser.setNickName(username);
        sysUser.setPassword(BCrypt.hashpw(password));
        sysUser.setUserType(userType);


        boolean exist = TenantHelper.dynamic(tenantId, () -> userMapper.selectCountByQuery(QueryWrapper.create()
             .from(SYS_USER)
             .and(SYS_USER.USER_NAME.eq(sysUser.getUserName()))
             .and(SYS_USER.USER_ID.ne(sysUser.getUserId()))) != 0);
        if (exist) {
            throw new UserException("user.register.save.error", username);
        }
        boolean regFlag = userService.registerUser(sysUser, tenantId);
        if (!regFlag) {
            throw new UserException("user.register.error");
        }
        recordLogininfor(tenantId, username, Constants.REGISTER, MessageUtils.message("user.register.success"));
    }

    /**
     * 校验验证码
     *
     * @param username 用户名
     * @param code     验证码
     * @param uuid     唯一标识
     */
    public void validateCaptcha(String tenantId, String username, String code, String uuid) {
        String verifyKey = GlobalConstants.CAPTCHA_CODE_KEY + StringUtils.defaultString(uuid, "");
        String captcha = RedisUtils.getCacheObject(verifyKey);
        RedisUtils.deleteObject(verifyKey);
        if (captcha == null) {
            recordLogininfor(tenantId, username, Constants.REGISTER, MessageUtils.message("user.jcaptcha.expire"));
            throw new CaptchaExpireException();
        }
        if (!code.equalsIgnoreCase(captcha)) {
            recordLogininfor(tenantId, username, Constants.REGISTER, MessageUtils.message("user.jcaptcha.error"));
            throw new CaptchaException();
        }
    }

    /**
     * 记录登录信息
     *
     * @param tenantId 租户ID
     * @param username 用户名
     * @param status   状态
     * @param message  消息内容
     * @return
     */
    private void recordLogininfor(String tenantId, String username, String status, String message) {
        LogininforEvent logininforEvent = new LogininforEvent();
        logininforEvent.setTenantId(tenantId);
        logininforEvent.setUsername(username);
        logininforEvent.setStatus(status);
        logininforEvent.setMessage(message);
        logininforEvent.setRequest(ServletUtils.getRequest());
        SpringUtils.context().publishEvent(logininforEvent);
    }

}
