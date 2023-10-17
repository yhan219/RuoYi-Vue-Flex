package org.dromara.system.domain;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

/**
 * 用户和角色关联 sys_user_role
 *
 * @author Lion Li
 */

@Data
@Table("sys_user_role")
public class SysUserRole {

    /**
     * 用户ID
     */
    @Id(keyType = KeyType.None)
    private Long userId;

    /**
     * 角色ID
     */
    private Long roleId;

}
