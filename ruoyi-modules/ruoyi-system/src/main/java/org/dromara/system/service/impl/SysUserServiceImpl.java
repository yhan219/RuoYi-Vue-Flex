package org.dromara.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.If;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.row.Db;
import com.mybatisflex.core.util.UpdateEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.constant.CacheNames;
import org.dromara.common.core.constant.UserConstants;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.service.UserService;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StreamUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.helper.DataBaseHelper;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.system.domain.SysDept;
import org.dromara.system.domain.SysUser;
import org.dromara.system.domain.SysUserPost;
import org.dromara.system.domain.SysUserRole;
import org.dromara.system.domain.bo.SysUserBo;
import org.dromara.system.domain.vo.SysPostVo;
import org.dromara.system.domain.vo.SysRoleVo;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.mapper.*;
import org.dromara.system.service.ISysUserService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static com.mybatisflex.core.query.QueryMethods.distinct;
import static org.dromara.system.domain.table.SysDeptTableDef.SYS_DEPT;
import static org.dromara.system.domain.table.SysRoleTableDef.SYS_ROLE;
import static org.dromara.system.domain.table.SysUserRoleTableDef.SYS_USER_ROLE;
import static org.dromara.system.domain.table.SysUserTableDef.SYS_USER;

/**
 * 用户 业务层处理
 *
 * @author Lion Li
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SysUserServiceImpl implements ISysUserService, UserService {

    private final SysUserMapper baseMapper;
    private final SysDeptMapper deptMapper;
    private final SysRoleMapper roleMapper;
    private final SysPostMapper postMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysUserPostMapper userPostMapper;

    @Override
    public TableDataInfo<SysUserVo> selectPageUserList(SysUserBo user, PageQuery pageQuery) {
        Page<SysUserVo> page = baseMapper.selectPageUserList(pageQuery, this.buildQueryWrapper(user));
        return TableDataInfo.build(page);
    }

    /**
     * 根据条件分页查询用户列表
     *
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    @Override
    public List<SysUserVo> selectUserList(SysUserBo user) {
        return baseMapper.selectUserList(this.buildQueryWrapper(user));
    }

    private QueryWrapper buildQueryWrapper(SysUserBo user) {
        Map<String, Object> params = user.getParams();
        QueryWrapper queryWrapper = QueryWrapper.create().from(SYS_USER.as("u"))
            .where(SYS_USER.DEL_FLAG.eq(UserConstants.USER_NORMAL))
            .and(SYS_USER.USER_ID.eq(user.getUserId()))
            .and(SYS_USER.USER_NAME.like(user.getUserName()))
            .and(SYS_USER.STATUS.eq(user.getStatus()))
            .and(SYS_USER.PHONENUMBER.eq(user.getPhonenumber()))
            .and(SYS_USER.CREATE_TIME.between(params.get("beginTime"), params.get("endTime"), params.get("beginTime") != null && params.get("endTime") != null));
        if (ObjectUtil.isNotNull(user.getDeptId())) {
            List<SysDept> deptList = deptMapper.selectListByQuery(
                QueryWrapper.create().select(SYS_DEPT.DEPT_ID).from(SYS_DEPT)
                    .and(DataBaseHelper.findInSet(user.getDeptId(), "ancestors")));
            List<Long> ids = StreamUtils.toList(deptList, SysDept::getDeptId);
            ids.add(user.getDeptId());
            queryWrapper.and(SYS_USER.DEPT_ID.in(ids));
        }
        queryWrapper.orderBy(SYS_USER.USER_ID, true);
        return queryWrapper;
    }

    /**
     * 根据条件分页查询已分配用户角色列表
     *
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    @Override
    public TableDataInfo<SysUserVo> selectAllocatedList(SysUserBo user, PageQuery pageQuery) {
        QueryWrapper queryWrapper = QueryWrapper.create()
            .select(distinct(SYS_USER.USER_ID, SYS_USER.DEPT_ID, SYS_USER.USER_NAME, SYS_USER.NICK_NAME, SYS_USER.EMAIL, SYS_USER.PHONENUMBER, SYS_USER.STATUS, SYS_USER.CREATE_TIME))
            .from(SYS_USER).as("u")
            .leftJoin(SYS_DEPT).as("d").on(SYS_USER.DEPT_ID.eq(SYS_DEPT.DEPT_ID))
            .leftJoin(SYS_USER_ROLE).on(SYS_USER.USER_ID.eq(SYS_USER_ROLE.USER_ID))
            .leftJoin(SYS_ROLE).on(SYS_ROLE.ROLE_ID.eq(SYS_USER_ROLE.ROLE_ID))
            .where(SYS_USER.DEL_FLAG.eq(UserConstants.USER_NORMAL))
            .and(SYS_ROLE.ROLE_ID.eq(user.getRoleId()))
            .and(SYS_USER.USER_NAME.like(user.getUserName()))
            .and(SYS_USER.STATUS.eq(user.getStatus()))
            .and(SYS_USER.PHONENUMBER.eq(user.getPhonenumber()))
            .orderBy(SYS_USER.USER_ID, true);
        Page<SysUserVo> page = baseMapper.selectAllocatedList(pageQuery, queryWrapper);
        return TableDataInfo.build(page);
    }

    /**
     * 根据条件分页查询未分配用户角色列表
     *
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    @Override
    public TableDataInfo<SysUserVo> selectUnallocatedList(SysUserBo user, PageQuery pageQuery) {
        List<Long> userIds = userRoleMapper.selectUserIdsByRoleId(user.getRoleId());
        QueryWrapper queryWrapper = QueryWrapper.create()
            .select(distinct(SYS_USER.USER_ID, SYS_USER.DEPT_ID, SYS_USER.USER_NAME, SYS_USER.NICK_NAME, SYS_USER.EMAIL, SYS_USER.PHONENUMBER, SYS_USER.STATUS, SYS_USER.CREATE_TIME))
            .from(SYS_USER).as("u")
            .leftJoin(SYS_DEPT).as("d").on(SYS_USER.DEPT_ID.eq(SYS_DEPT.DEPT_ID))
            .leftJoin(SYS_USER_ROLE).on(SYS_USER.USER_ID.eq(SYS_USER_ROLE.USER_ID))
            .leftJoin(SYS_ROLE).on(SYS_ROLE.ROLE_ID.eq(SYS_USER_ROLE.ROLE_ID))
            .where(SYS_USER.DEL_FLAG.eq(UserConstants.USER_NORMAL))
            .and(SYS_ROLE.ROLE_ID.eq(user.getRoleId()).or(SYS_ROLE.ROLE_ID.isNull()))
            .and(SYS_USER.USER_ID.notIn(userIds, If::isNotEmpty))
            .and(SYS_USER.USER_NAME.like(user.getUserName()))
            .and(SYS_USER.PHONENUMBER.eq(user.getPhonenumber()))
            .orderBy(SYS_USER.USER_ID, true);
        Page<SysUserVo> page = baseMapper.selectAllocatedList(pageQuery, queryWrapper);
        return TableDataInfo.build(page);
    }

    /**
     * 通过用户名查询用户
     *
     * @param userName 用户名
     * @return 用户对象信息
     */
    @Override
    public SysUserVo selectUserByUserName(String userName) {
        return baseMapper.selectUserByUserName(userName);
    }

    /**
     * 通过手机号查询用户
     *
     * @param phonenumber 手机号
     * @return 用户对象信息
     */
    @Override
    public SysUserVo selectUserByPhonenumber(String phonenumber) {
        return baseMapper.selectUserByPhonenumber(phonenumber);
    }

    /**
     * 通过用户ID查询用户
     *
     * @param userId 用户ID
     * @return 用户对象信息
     */
    @Override
    public SysUserVo selectUserById(Long userId) {
        return baseMapper.selectUserById(userId);
    }

    /**
     * 查询用户所属角色组
     *
     * @param userName 用户名
     * @return 结果
     */
    @Override
    public String selectUserRoleGroup(String userName) {
        List<SysRoleVo> list = roleMapper.selectRolesByUserName(userName);
        if (CollUtil.isEmpty(list)) {
            return StringUtils.EMPTY;
        }
        return StreamUtils.join(list, SysRoleVo::getRoleName);
    }

    /**
     * 查询用户所属岗位组
     *
     * @param userName 用户名
     * @return 结果
     */
    @Override
    public String selectUserPostGroup(String userName) {
        List<SysPostVo> list = postMapper.selectPostsByUserName(userName);
        if (CollUtil.isEmpty(list)) {
            return StringUtils.EMPTY;
        }
        return StreamUtils.join(list, SysPostVo::getPostName);
    }

    /**
     * 校验用户名称是否唯一
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public boolean checkUserNameUnique(SysUserBo user) {
        return baseMapper.selectCountByQuery(QueryWrapper.create().from(SYS_USER)
            .where(SYS_USER.USER_NAME.eq(user.getUserName())).and(SYS_USER.USER_ID.ne(user.getUserId()))) == 0;
    }

    /**
     * 校验手机号码是否唯一
     *
     * @param user 用户信息
     */
    @Override
    public boolean checkPhoneUnique(SysUserBo user) {
        return baseMapper.selectCountByQuery(QueryWrapper.create().from(SYS_USER)
            .where(SYS_USER.PHONENUMBER.eq(user.getPhonenumber())).and(SYS_USER.USER_ID.ne(user.getUserId()))) == 0;
    }

    /**
     * 校验email是否唯一
     *
     * @param user 用户信息
     */
    @Override
    public boolean checkEmailUnique(SysUserBo user) {
        return baseMapper.selectCountByQuery(QueryWrapper.create().from(SYS_USER)
            .where(SYS_USER.EMAIL.eq(user.getEmail())).and(SYS_USER.USER_ID.ne(user.getUserId()))) == 0;
    }

    /**
     * 校验用户是否允许操作
     *
     * @param userId 用户ID
     */
    @Override
    public void checkUserAllowed(Long userId) {
        if (ObjectUtil.isNotNull(userId) && LoginHelper.isSuperAdmin(userId)) {
            throw new ServiceException("不允许操作超级管理员用户");
        }
    }

    /**
     * 校验用户是否有数据权限
     *
     * @param userId 用户id
     */
    @Override
    public void checkUserDataScope(Long userId) {
        if (ObjectUtil.isNull(userId)) {
            return;
        }
        if (LoginHelper.isSuperAdmin()) {
            return;
        }
        if (ObjectUtil.isNull(selectUserById(userId))) {
            throw new ServiceException("没有权限访问用户数据！");
        }
    }

    /**
     * 新增保存用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertUser(SysUserBo user) {
        SysUser sysUser = MapstructUtils.convert(user, SysUser.class);
        // 新增用户信息
        int rows = baseMapper.insert(sysUser,true);
        user.setUserId(sysUser.getUserId());
        // 新增用户岗位关联
        insertUserPost(user, false);
        // 新增用户与角色管理
        insertUserRole(user, false);
        return rows;
    }

    /**
     * 注册用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public boolean registerUser(SysUserBo user, String tenantId) {
        user.setCreateBy(user.getUserId());
        user.setUpdateBy(user.getUserId());
        SysUser sysUser = MapstructUtils.convert(user, SysUser.class);
        sysUser.setTenantId(tenantId);
        return baseMapper.insert(sysUser,true) > 0;
    }

    /**
     * 修改保存用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateUser(SysUserBo user) {
        // 新增用户与角色管理
        insertUserRole(user, true);
        // 新增用户与岗位管理
        insertUserPost(user, true);
        if (StringUtils.isBlank(user.getPassword())) {
            user.setPassword(null);
        }
        SysUser sysUser = MapstructUtils.convert(user, SysUser.class);
        // 防止错误更新后导致的数据误删除
        int flag = baseMapper.update(sysUser);
        if (flag < 1) {
            throw new ServiceException("修改用户" + user.getUserName() + "信息失败");
        }
        return flag;
    }

    /**
     * 用户授权角色
     *
     * @param userId  用户ID
     * @param roleIds 角色组
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertUserAuth(Long userId, Long[] roleIds) {
        insertUserRole(userId, roleIds, true);
    }

    /**
     * 修改用户状态
     *
     * @param userId 用户ID
     * @param status 帐号状态
     * @return 结果
     */
    @Override
    public boolean updateUserStatus(Long userId, String status) {
        SysUser sysUser = UpdateEntity.of(SysUser.class, userId);
        sysUser.setStatus(status);
        return baseMapper.update(sysUser, true) > 0;
    }

    /**
     * 修改用户基本信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int updateUserProfile(SysUserBo user) {
        SysUser sysUser = UpdateEntity.of(SysUser.class, user.getUserId());
        if (StringUtils.isNotBlank(user.getNickName())) {
            sysUser.setNickName(user.getNickName());
        }
        sysUser.setPhonenumber(user.getPhonenumber());
        sysUser.setEmail(user.getEmail());
        sysUser.setSex(user.getSex());
        return baseMapper.update(sysUser,true);
    }

    /**
     * 修改用户头像
     *
     * @param userId 用户ID
     * @param avatar 头像地址
     * @return 结果
     */
    @Override
    public boolean updateUserAvatar(Long userId, Long avatar) {
        SysUser sysUser = UpdateEntity.of(SysUser.class, userId);
        sysUser.setAvatar(avatar);
        return baseMapper.update(sysUser, true) > 0;
    }

    /**
     * 重置用户密码
     *
     * @param userId   用户ID
     * @param password 密码
     * @return 结果
     */
    @Override
    public boolean resetUserPwd(Long userId, String password) {
        SysUser sysUser = UpdateEntity.of(SysUser.class, userId);
        sysUser.setPassword(password);
        return baseMapper.update(sysUser, true) > 0;
    }

    /**
     * 新增用户角色信息
     *
     * @param user  用户对象
     * @param clear 清除已存在的关联数据
     */
    private void insertUserRole(SysUserBo user, boolean clear) {
        this.insertUserRole(user.getUserId(), user.getRoleIds(), clear);
    }

    /**
     * 新增用户岗位信息
     *
     * @param user  用户对象
     * @param clear 清除已存在的关联数据
     */
    private void insertUserPost(SysUserBo user, boolean clear) {
        Long[] posts = user.getPostIds();
        if (ArrayUtil.isNotEmpty(posts)) {
            if (clear) {
                // 删除用户与岗位关联
                userPostMapper.deleteByQuery(QueryWrapper.create().from(SysUserPost.class).where(SysUserPost::getUserId).eq(user.getUserId()));
            }
            // 新增用户与岗位管理
            List<SysUserPost> list = StreamUtils.toList(List.of(posts), postId -> {
                SysUserPost up = new SysUserPost();
                up.setUserId(user.getUserId());
                up.setPostId(postId);
                return up;
            });
            Db.executeBatch(list, 1000, SysUserPostMapper.class, BaseMapper::insertWithPk);
        }
    }

    /**
     * 新增用户角色信息
     *
     * @param userId  用户ID
     * @param roleIds 角色组
     * @param clear   清除已存在的关联数据
     */
    private void insertUserRole(Long userId, Long[] roleIds, boolean clear) {
        if (ArrayUtil.isNotEmpty(roleIds)) {
            // 判断是否具有此角色的操作权限
            List<SysRoleVo> roles = roleMapper.selectRoleList(QueryWrapper.create());
            if (CollUtil.isEmpty(roles)) {
                throw new ServiceException("没有权限访问角色的数据");
            }
            List<Long> roleList = StreamUtils.toList(roles, SysRoleVo::getRoleId);
            if (!LoginHelper.isSuperAdmin(userId)) {
                roleList.remove(UserConstants.SUPER_ADMIN_ID);
            }
            List<Long> canDoRoleList = StreamUtils.filter(List.of(roleIds), roleList::contains);
            if (CollUtil.isEmpty(canDoRoleList)) {
                throw new ServiceException("没有权限访问角色的数据");
            }
            if (clear) {
                // 删除用户与角色关联
                userRoleMapper.deleteByQuery(new QueryWrapper().from(SysUserRole.class).where(SysUserRole::getUserId).eq(userId));
            }
            // 新增用户与角色管理
            List<SysUserRole> list = StreamUtils.toList(canDoRoleList, roleId -> {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                return ur;
            });
            Db.executeBatch(list, 1000, SysUserRoleMapper.class, BaseMapper::insertWithPk);
        }
    }

    /**
     * 通过用户ID删除用户
     *
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteUserById(Long userId) {
        // 删除用户与角色关联
        userRoleMapper.deleteByQuery(new QueryWrapper().from(SysUserRole.class).where(SysUserRole::getUserId).eq(userId));
        // 删除用户与岗位表
        userPostMapper.deleteByQuery(new QueryWrapper().from(SysUserPost.class).where(SysUserPost::getUserId).eq(userId));
        // 防止更新失败导致的数据删除
        int flag = baseMapper.deleteById(userId);
        if (flag < 1) {
            throw new ServiceException("删除用户失败!");
        }
        return flag;
    }

    /**
     * 批量删除用户信息
     *
     * @param userIds 需要删除的用户ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteUserByIds(Long[] userIds) {
        for (Long userId : userIds) {
            checkUserAllowed(userId);
            checkUserDataScope(userId);
        }
        List<Long> ids = List.of(userIds);
        // 删除用户与角色关联
        userRoleMapper.deleteByQuery(new QueryWrapper().from(SysUserRole.class).where(SysUserRole::getUserId).in(ids));

        // 删除用户与岗位表
        userPostMapper.deleteByQuery(new QueryWrapper().from(SysUserPost.class).where(SysUserPost::getUserId).in(ids));

        // 防止更新失败导致的数据删除
        int flag = baseMapper.deleteBatchByIds(ids);
        if (flag < 1) {
            throw new ServiceException("删除用户失败!");
        }
        return flag;
    }

    /**
     * 通过部门id查询当前部门所有用户
     *
     * @param deptId
     * @return
     */
    @Override
    public List<SysUserVo> selectUserListByDept(Long deptId) {
        QueryWrapper queryWrapper = QueryWrapper.create().from(SysUser.class)
            .where(SysUser::getDeptId).eq(deptId)
            .orderBy(SysUser::getUserId, true);
        return baseMapper.selectListByQueryAs(queryWrapper, SysUserVo.class);
    }

    @Cacheable(cacheNames = CacheNames.SYS_USER_NAME, key = "#userId")
    @Override
    public String selectUserNameById(Long userId) {
        SysUser sysUser = baseMapper.selectOneByQuery(QueryWrapper.create().select(SysUser::getUserName).from(SysUser.class)
            .where(SysUser::getUserId).eq(userId));
        return ObjectUtil.isNull(sysUser) ? null : sysUser.getUserName();
    }
}
