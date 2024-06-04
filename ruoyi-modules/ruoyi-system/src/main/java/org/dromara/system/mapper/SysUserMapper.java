package org.dromara.system.mapper;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import org.dromara.common.mybatis.annotation.DataColumn;
import org.dromara.common.mybatis.annotation.DataPermission;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.system.domain.SysUser;
import org.dromara.system.domain.vo.SysUserExportVo;
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
    @DataPermission({
        @DataColumn(key = "deptName", value = "d.dept_id"),
        @DataColumn(key = "userName", value = "u.user_id")
    })
    List<SysUserExportVo> selectUserExportList(@Param(Constants.WRAPPER) Wrapper<SysUser> queryWrapper);

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
     * 根据条件分页查询未分配用户角色列表
     *
     * @param queryWrapper 查询条件
     * @return 用户信息集合信息
     */
    @DataPermission({
        @DataColumn(key = "deptName", value = "d.dept_id"),
        @DataColumn(key = "userName", value = "u.user_id")
    })
    Page<SysUserVo> selectUnallocatedList(@Param("page") Page<SysUser> page, @Param(Constants.WRAPPER) Wrapper<SysUser> queryWrapper);


    @DataPermission({
        @DataColumn(key = "deptName", value = "dept_id"),
        @DataColumn(key = "userName", value = "user_id")
    })
    long countUserById(Long userId);


    default boolean update(UpdateChain<SysUser> updateChain) {
        return this.update(updateChain, DataColumn.of("deptName", "dept_id"), DataColumn.of("userName", "user_id"));
    }


}
