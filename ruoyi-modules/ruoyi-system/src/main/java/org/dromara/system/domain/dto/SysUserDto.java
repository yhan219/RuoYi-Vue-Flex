package org.dromara.system.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.common.sensitive.annotation.Sensitive;
import org.dromara.common.sensitive.core.SensitiveStrategy;
import org.dromara.common.translation.annotation.Translation;
import org.dromara.common.translation.constant.TransConstant;
import org.dromara.system.domain.SysUser;
import org.dromara.system.domain.vo.SysDeptVo;
import org.dromara.system.domain.vo.SysRoleVo;
import org.dromara.system.domain.vo.SysUserVo;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@AutoMapper(target = SysUserVo.class)
public class SysUserDto implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 用户ID
         */
        private Long userId;

        /**
         * 租户ID
         */
        private String tenantId;

        /**
         * 部门ID
         */
        private Long deptId;

        /**
         * 用户账号
         */
        private String userName;

        /**
         * 用户昵称
         */
        private String nickName;

        /**
         * 用户类型（sys_user系统用户）
         */
        private String userType;

        /**
         * 用户邮箱
         */
        @Sensitive(strategy = SensitiveStrategy.EMAIL)
        private String email;

        /**
         * 手机号码
         */
        @Sensitive(strategy = SensitiveStrategy.PHONE)
        private String phonenumber;

        /**
         * 用户性别（0男 1女 2未知）
         */
        private String sex;

        /**
         * 头像地址
         */
        @Translation(type = TransConstant.OSS_ID_TO_URL)
        private Long avatar;

        /**
         * 密码
         */
        @JsonIgnore
        @JsonProperty
        private String password;

        /**
         * 帐号状态（0正常 1停用）
         */
        private String status;

        /**
         * 最后登录IP
         */
        private String loginIp;

        /**
         * 最后登录时间
         */
        private Date loginDate;

        /**
         * 备注
         */
        private String remark;

        /**
         * 创建时间
         */
        private Date createTime;

        /**
         * 部门对象
         */
        private SysDeptVo dept;

        /**
         * 角色对象
         */
        private List<SysRoleVo> roles;

}
