package org.dromara.system.mapper;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryColumn;
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

import static org.dromara.system.domain.table.SysDeptTableDef.SYS_DEPT;
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

    default List<SysUserExportVo> selectUserExportList(QueryWrapper queryWrapper) {
        queryWrapper
            .select("u.user_id", "u.dept_id", "u.nick_name", "u.user_name", "u.email", "u.avatar", "u.phonenumber", "u.sex","u.status", "u.del_flag","u.login_ip","u.login_date")
            .select("u.create_by", "u.create_time", "u.remark")
            .select("d.dept_name", "d.leader")
            .select(new QueryColumn("u1.user_name").as("leaderName"))
            .leftJoin(SYS_DEPT).as("d").on(new QueryColumn("u.dept_id").eq(new QueryColumn("d.dept_id")))
            .leftJoin(SYS_USER).as("u1").on(new QueryColumn("d.leader").eq(new QueryColumn("u1.user_id")));
        return this.selectListWithRelationsByQueryAs(queryWrapper, SysUserExportVo.class, DataPermission.of(
            DataColumn.of("deptName", "d.dept_id"),
            DataColumn.of("userName", "u.user_id")
        ));
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
     * 根据条件分页查询未分配用户角色列表
     *
     * @param queryWrapper 查询条件
     * @return 用户信息集合信息
     */

    default Page<SysUserVo> selectUnallocatedList(PageQuery page, QueryWrapper queryWrapper){
        return this.paginateAs(page, queryWrapper, SysUserVo.class, DataColumn.of("deptName", "d.dept_id"), DataColumn.of("userName", "u.user_id"));
    }


    default long countUserById(Long userId){
        return this.selectCountByQuery(QueryWrapper.create().eq(SysUser::getUserId, userId), DataColumn.of("deptName", "dept_id"), DataColumn.of("userName", "user_id"));
    }


    default boolean update(UpdateChain<SysUser> updateChain) {
        return this.update(updateChain, DataColumn.of("deptName", "dept_id"), DataColumn.of("userName", "user_id"));
    }


}
