package org.dromara.system.mapper;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.mybatis.annotation.DataColumn;
import org.dromara.common.mybatis.annotation.DataPermission;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.system.domain.SysUser;
import org.dromara.system.domain.dto.SysUserDto;
import org.dromara.system.domain.vo.SysUserVo;

import java.util.List;

import static org.dromara.system.domain.table.SysDeptTableDef.SYS_DEPT;
import static org.dromara.system.domain.table.SysRoleTableDef.SYS_ROLE;
import static org.dromara.system.domain.table.SysUserRoleTableDef.SYS_USER_ROLE;
import static org.dromara.system.domain.table.SysUserTableDef.SYS_USER;

/**
 * 用户表 数据层
 *
 * @author Lion Li
 */
public interface SysUserMapper extends BaseMapperPlus<SysUser> {

    default Page<SysUserVo> selectPageUserList(PageQuery pageQuery, QueryWrapper queryWrapper) {
        selectListVo(queryWrapper);
        Page<SysUserDto> sysUserDtoPage = this.paginateAs(pageQuery, queryWrapper, SysUserDto.class, DataPermission.of(
                DataColumn.of("deptName", "d.dept_id"),
                DataColumn.of("userName", "u.user_id")
            )
        );
        Page<SysUserVo> p = Page.of(pageQuery.getPageNum(), pageQuery.getPageSize(), sysUserDtoPage.getTotalRow());
        List<SysUserVo> records = MapstructUtils.convert(sysUserDtoPage.getRecords(), SysUserVo.class);
        p.setRecords(records);
        return p;
    }

    default void selectListVo(QueryWrapper queryWrapper) {
        queryWrapper.select(SYS_USER.USER_ID, SYS_USER.DEPT_ID, SYS_USER.NICK_NAME, SYS_USER.USER_NAME, SYS_USER.EMAIL, SYS_USER.AVATAR, SYS_USER.PHONENUMBER, SYS_USER.SEX,
                SYS_USER.STATUS, SYS_USER.DEL_FLAG, SYS_USER.LOGIN_IP, SYS_USER.LOGIN_DATE, SYS_USER.CREATE_BY, SYS_USER.CREATE_TIME, SYS_USER.REMARK,
                SYS_DEPT.DEPT_NAME, SYS_DEPT.LEADER, SYS_USER.USER_NAME.as("leaderName"))
            .leftJoin(SYS_DEPT).as("d").on(SYS_USER.DEPT_ID.eq(SYS_DEPT.DEPT_ID))
            .leftJoin(SYS_USER).as("u1").on(SYS_USER.USER_ID.eq(SYS_DEPT.LEADER));
    }

    /**
     * 根据条件分页查询用户列表
     *
     * @param queryWrapper 查询条件
     * @return 用户信息集合信息
     */


    default List<SysUserVo> selectUserList(QueryWrapper queryWrapper) {
        selectListVo(queryWrapper);
        List<SysUserDto> sysUserDtos = this.selectListByQueryAs(queryWrapper, SysUserDto.class, DataPermission.of(
                DataColumn.of("deptName", "d.dept_id"),
                DataColumn.of("userName", "u.user_id")
            )
        );
        return MapstructUtils.convert(sysUserDtos, SysUserVo.class);
    }

    /**
     * 根据条件分页查询已配用户角色列表
     *
     * @param queryWrapper 查询条件
     * @return 用户信息集合信息
     */
    default Page<SysUserVo> selectAllocatedList(PageQuery page, QueryWrapper queryWrapper) {
        Page<SysUserDto> sysUserDtoPage = this.paginateAs(page, queryWrapper, SysUserDto.class, DataPermission.of(DataColumn.of("deptName", "d.dept_id"), DataColumn.of("userName", "u.user_id")));
        Page<SysUserVo> p = Page.of(page.getPageNum(), page.getPageSize(), sysUserDtoPage.getTotalRow());
        List<SysUserVo> records = MapstructUtils.convert(sysUserDtoPage.getRecords(), SysUserVo.class);
        p.setRecords(records);
        return p;
    }

    private static void selectUserVo(QueryWrapper queryWrapper) {
        queryWrapper.select(SYS_USER.USER_ID, SYS_USER.TENANT_ID, SYS_USER.DEPT_ID, SYS_USER.USER_NAME, SYS_USER.NICK_NAME, SYS_USER.USER_TYPE, SYS_USER.EMAIL, SYS_USER.AVATAR, SYS_USER.PHONENUMBER, SYS_USER.PASSWORD, SYS_USER.SEX, SYS_USER.STATUS, SYS_USER.DEL_FLAG,
                SYS_USER.LOGIN_IP, SYS_USER.LOGIN_DATE, SYS_USER.CREATE_BY, SYS_USER.CREATE_TIME, SYS_USER.REMARK, SYS_DEPT.DEPT_ID, SYS_DEPT.PARENT_ID, SYS_DEPT.ANCESTORS, SYS_DEPT.DEPT_NAME, SYS_DEPT.ORDER_NUM, SYS_DEPT.LEADER, SYS_DEPT.STATUS,
                SYS_ROLE.ROLE_ID, SYS_ROLE.ROLE_NAME, SYS_ROLE.ROLE_KEY, SYS_ROLE.ROLE_SORT, SYS_ROLE.DATA_SCOPE, SYS_ROLE.STATUS
            ).from(SYS_USER).as("u")
            .leftJoin(SYS_DEPT).as("d").on(SYS_USER.DEPT_ID.eq(SYS_DEPT.DEPT_ID))
            .leftJoin(SYS_USER_ROLE).as("sur").on(SYS_USER.USER_ID.eq(SYS_USER_ROLE.USER_ID))
            .leftJoin(SYS_ROLE).as("r").on(SYS_ROLE.ROLE_ID.eq(SYS_USER_ROLE.ROLE_ID));
    }




    /**
     * 通过用户名查询用户
     *
     * @param userName 用户名
     * @return 用户对象信息
     */
    default SysUserVo selectUserByUserName(String userName) {
        QueryWrapper queryWrapper = QueryWrapper.create().where(SYS_USER.USER_NAME.eq(userName));
        selectUserVo(queryWrapper);
        SysUserDto sysUserDto = selectOneByQueryAs(queryWrapper, SysUserDto.class);
        return MapstructUtils.convert(sysUserDto, SysUserVo.class);
    }

    /**
     * 通过手机号查询用户
     *
     * @param phonenumber 手机号
     * @return 用户对象信息
     */
    default SysUserVo selectUserByPhonenumber(String phonenumber) {
        QueryWrapper queryWrapper = QueryWrapper.create().where(SYS_USER.PHONENUMBER.eq(phonenumber));
        selectUserVo(queryWrapper);
        SysUserDto sysUserDto = selectOneByQueryAs(queryWrapper, SysUserDto.class);
        return MapstructUtils.convert(sysUserDto, SysUserVo.class);
    }

    /**
     * 通过邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户对象信息
     */
    default SysUserVo selectUserByEmail(String email){
        QueryWrapper queryWrapper = QueryWrapper.create().where(SYS_USER.EMAIL.eq(email));
        selectUserVo(queryWrapper);
        SysUserDto sysUserDto = selectOneByQueryAs(queryWrapper, SysUserDto.class);
        return MapstructUtils.convert(sysUserDto, SysUserVo.class);
    }

    /**
     * 通过用户名查询用户(不走租户插件)
     *
     * @param userName 用户名
     * @param tenantId 租户id
     * @return 用户对象信息
     */
    default SysUserVo selectTenantUserByUserName(String userName,String tenantId) {
        QueryWrapper queryWrapper = QueryWrapper.create().where(SYS_USER.USER_NAME.eq(userName)).and(SYS_USER.TENANT_ID.eq(tenantId));
        selectUserVo(queryWrapper);
        SysUserDto sysUserDto = selectOneByQueryAs(queryWrapper, SysUserDto.class);
        return MapstructUtils.convert(sysUserDto, SysUserVo.class);
    }

    /**
     * 通过手机号查询用户(不走租户插件)
     *
     * @param phonenumber 手机号
     * @param tenantId    租户id
     * @return 用户对象信息
     */
    default SysUserVo selectTenantUserByPhonenumber(String phonenumber, String tenantId) {
        QueryWrapper queryWrapper = QueryWrapper.create().where(SYS_USER.PHONENUMBER.eq(phonenumber)).and(SYS_USER.TENANT_ID.eq(tenantId));
        selectUserVo(queryWrapper);
        SysUserDto sysUserDto = selectOneByQueryAs(queryWrapper, SysUserDto.class);
        return MapstructUtils.convert(sysUserDto, SysUserVo.class);
    }

    /**
     * 通过邮箱查询用户(不走租户插件)
     *
     * @param email    邮箱
     * @param tenantId 租户id
     * @return 用户对象信息
     */
    default SysUserVo selectTenantUserByEmail(String email,  String tenantId) {
        QueryWrapper queryWrapper = QueryWrapper.create().where(SYS_USER.EMAIL.eq(email)).and(SYS_USER.TENANT_ID.eq(tenantId));
        selectUserVo(queryWrapper);
        SysUserDto sysUserDto = selectOneByQueryAs(queryWrapper, SysUserDto.class);
        return MapstructUtils.convert(sysUserDto, SysUserVo.class);
    }


    /**
     * 通过用户ID查询用户
     *
     * @param userId 用户ID
     * @return 用户对象信息
     */

    default SysUserVo selectUserById(Long userId) {
        QueryWrapper queryWrapper = QueryWrapper.create().where(SysUser::getUserId).eq(userId);
        selectUserVo(queryWrapper);
        SysUserDto sysUserDto = selectOneByQueryAs(queryWrapper, SysUserDto.class, DataPermission.of(DataColumn.of("deptName", "d.dept_id"), DataColumn.of("userName", "u.user_id")));
        return MapstructUtils.convert(sysUserDto, SysUserVo.class);
    }


    default boolean update(UpdateChain<SysUser> updateChain) {
        return this.update(updateChain, DataPermission.of(DataColumn.of("deptName", "dept_id"), DataColumn.of("userName", "user_id")));
    }


}
