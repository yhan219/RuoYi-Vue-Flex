package org.dromara.system.mapper;

import com.mybatisflex.core.query.QueryWrapper;
import org.dromara.common.core.constant.UserConstants;
import org.dromara.system.domain.SysMenu;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.system.domain.vo.SysMenuVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

import static org.dromara.system.domain.table.SysMenuTableDef.SYS_MENU;

/**
 * 菜单表 数据层
 *
 * @author Lion Li
 */
public interface SysMenuMapper extends BaseMapperPlus<SysMenu> {

    /**
     * 根据用户所有权限
     *
     * @return 权限列表
     */
    List<String> selectMenuPerms();


    /**
     * 根据用户ID查询权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    List<String> selectMenuPermsByUserId(Long userId);

    /**
     * 根据角色ID查询权限
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<String> selectMenuPermsByRoleId(Long roleId);

    /**
     * 根据用户ID查询菜单
     *
     * @return 菜单列表
     */
    default List<SysMenu> selectMenuTreeAll() {
        return selectListByQuery(QueryWrapper.create().from(SYS_MENU)
            .where(SYS_MENU.MENU_TYPE.in(UserConstants.TYPE_DIR, UserConstants.TYPE_MENU))
            .and(SYS_MENU.STATUS.eq(UserConstants.MENU_NORMAL))
            .orderBy(SYS_MENU.PARENT_ID, true)
            .orderBy(SYS_MENU.ORDER_NUM, true)
        );
    }

    /**
     * 根据用户ID查询菜单
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    List<SysMenu> selectMenuTreeByUserId(Long userId);

    /**
     * 根据角色ID查询菜单树信息
     *
     * @param roleId            角色ID
     * @param menuCheckStrictly 菜单树选择项是否关联显示
     * @return 选中菜单列表
     */
    List<Long> selectMenuListByRoleId(@Param("roleId") Long roleId, @Param("menuCheckStrictly") boolean menuCheckStrictly);

}
