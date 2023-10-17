package org.dromara.system.domain;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

/**
 * 用户和岗位关联 sys_user_post
 *
 * @author Lion Li
 */

@Data
@Table("sys_user_post")
public class SysUserPost {

    /**
     * 用户ID
     */
    @Id(keyType = KeyType.None)
    private Long userId;

    /**
     * 岗位ID
     */
    private Long postId;

}
