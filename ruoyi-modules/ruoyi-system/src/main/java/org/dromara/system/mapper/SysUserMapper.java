package org.dromara.system.mapper;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import org.dromara.common.mybatis.annotation.DataColumn;
import org.dromara.common.mybatis.annotation.DataPermission;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.system.domain.SysUser;
import org.dromara.system.domain.vo.SysUserVo;

import java.util.List;

import static org.dromara.system.domain.table.SysUserTableDef.SYS_USER;

/**
 * 用户表 数据层
 *
 * @author Lion Li
 */
public interface SysUserMapper extends BaseMapperPlus<SysUser> {

    default Page<SysUserVo> selectPageUserList(PageQuery pageQuery, QueryWrapper queryWrapper) {
        return this.paginateWithRelationsAs(pageQuery, queryWrapper, SysUserVo.class, DataColumn.of("deptName", "u.dept_id"), DataColumn.of("userName", "u.user_id"));
    }

    /**
     * 根据条件分页查询用户列表
     *
     * @param queryWrapper 查询条件
     * @return 用户信息集合信息
     */


    default List<SysUserVo> selectUserList(QueryWrapper queryWrapper) {
        return this.selectListWithRelationsByQueryAs(queryWrapper, SysUserVo.class, DataPermission.of(
                DataColumn.of("deptName", "u.dept_id"),
                DataColumn.of("userName", "u.user_id")
            )
        );
    }

    /**
     * 根据条件分页查询已配用户角色列表
     *
     * @param queryWrapper 查询条件
     * @return 用户信息集合信息
     */
    default Page<SysUserVo> selectAllocatedList(PageQuery page, QueryWrapper queryWrapper) {
        return this.paginateAs(page, queryWrapper, SysUserVo.class, DataColumn.of("deptName", "d.dept_id"), DataColumn.of("userName", "u.user_id"));
    }


    /**
     * 通过用户名查询用户
     *
     * @param userName 用户名
     * @return 用户对象信息
     */
    default SysUserVo selectUserByUserName(String userName) {
        QueryWrapper queryWrapper = QueryWrapper.create().where(SYS_USER.USER_NAME.eq(userName));
        return selectOneWithRelationsByQueryAs(queryWrapper, SysUserVo.class);
    }

    /**
     * 通过手机号查询用户
     *
     * @param phonenumber 手机号
     * @return 用户对象信息
     */
    default SysUserVo selectUserByPhonenumber(String phonenumber) {
        QueryWrapper queryWrapper = QueryWrapper.create().where(SYS_USER.PHONENUMBER.eq(phonenumber));
        return selectOneWithRelationsByQueryAs(queryWrapper, SysUserVo.class);
    }

    /**
     * 通过邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户对象信息
     */
    default SysUserVo selectUserByEmail(String email) {
        QueryWrapper queryWrapper = QueryWrapper.create().where(SYS_USER.EMAIL.eq(email));
        return selectOneWithRelationsByQueryAs(queryWrapper, SysUserVo.class);
    }

    /**
     * 通过用户ID查询用户
     *
     * @param userId 用户ID
     * @return 用户对象信息
     */

    default SysUserVo selectUserById(Long userId) {
        QueryWrapper queryWrapper = QueryWrapper.create().where(SysUser::getUserId).eq(userId);
        return selectOneWithRelationsByQueryAs(queryWrapper, SysUserVo.class, DataColumn.of("deptName", "dept_id"), DataColumn.of("userName", "user_id"));
    }


    default boolean update(UpdateChain<SysUser> updateChain) {
        return this.update(updateChain, DataColumn.of("deptName", "dept_id"), DataColumn.of("userName", "user_id"));
    }


}
