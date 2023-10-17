package org.dromara.system.domain.vo;

import com.mybatisflex.annotation.Column;
import lombok.Data;

import java.util.Set;

/**
 * 登录用户信息
 *
 * @author Michelle.Chung
 */
@Data
public class UserInfoVo {

    /**
     * 用户基本信息
     */
    private SysUserVo user;

    /**
     * 菜单权限
     */
    @Column("permission")
    private Set<String> permissions;

    /**
     * 角色权限
     */
    @Column("role")
    private Set<String> roles;

}
