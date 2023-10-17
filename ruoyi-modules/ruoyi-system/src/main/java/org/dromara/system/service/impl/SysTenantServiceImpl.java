package org.dromara.system.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.row.Db;
import com.mybatisflex.core.tenant.TenantManager;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.constant.CacheNames;
import org.dromara.common.core.constant.Constants;
import org.dromara.common.core.constant.TenantConstants;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.system.domain.*;
import org.dromara.system.domain.bo.SysTenantBo;
import org.dromara.system.domain.vo.SysTenantVo;
import org.dromara.system.mapper.*;
import org.dromara.system.service.ISysTenantService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.dromara.system.domain.table.SysConfigTableDef.SYS_CONFIG;
import static org.dromara.system.domain.table.SysDictDataTableDef.SYS_DICT_DATA;
import static org.dromara.system.domain.table.SysDictTypeTableDef.SYS_DICT_TYPE;
import static org.dromara.system.domain.table.SysRoleMenuTableDef.SYS_ROLE_MENU;
import static org.dromara.system.domain.table.SysRoleTableDef.SYS_ROLE;
import static org.dromara.system.domain.table.SysTenantTableDef.SYS_TENANT;

/**
 * 租户Service业务层处理
 *
 * @author Michelle.Chung
 */
@RequiredArgsConstructor
@Service
public class SysTenantServiceImpl implements ISysTenantService {

    private final SysTenantMapper baseMapper;
    private final SysTenantPackageMapper tenantPackageMapper;
    private final SysUserMapper userMapper;
    private final SysDeptMapper deptMapper;
    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysRoleDeptMapper roleDeptMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysDictTypeMapper dictTypeMapper;
    private final SysDictDataMapper dictDataMapper;
    private final SysConfigMapper configMapper;

    /**
     * 查询租户列表
     */
    @Override
    public List<SysTenantVo> queryList(SysTenantBo bo) {
        QueryWrapper lqw = buildQueryWrapper(bo);
        return TenantHelper.ignore(() -> baseMapper.selectListByQueryAs(lqw, SysTenantVo.class));
    }

    /**
     * 查询租户
     */
    @Override
    public SysTenantVo queryById(Long id) {
        return TenantHelper.ignore(() -> baseMapper.selectOneWithRelationsByIdAs(id, SysTenantVo.class));
    }

    /**
     * 基于租户ID查询租户
     */
    @Cacheable(cacheNames = CacheNames.SYS_TENANT, key = "#tenantId")
    @Override
    public SysTenantVo queryByTenantId(String tenantId) {
        return baseMapper.selectOneByQueryAs(QueryWrapper.create().from(SYS_TENANT).where(SYS_TENANT.TENANT_ID.eq(tenantId)), SysTenantVo.class);
    }

    /**
     * 查询租户列表
     */
    @Override
    public TableDataInfo<SysTenantVo> queryPageList(SysTenantBo bo, PageQuery pageQuery) {
        return TenantHelper.ignore(() -> {
            QueryWrapper lqw = buildQueryWrapper(bo);
            Page<SysTenantVo> result = baseMapper.paginateAs(pageQuery, lqw, SysTenantVo.class);
            return TableDataInfo.build(result);
        });
    }

    private QueryWrapper buildQueryWrapper(SysTenantBo bo) {
        return QueryWrapper.create().from(SYS_TENANT)
            .where(SYS_TENANT.TENANT_ID.eq(bo.getTenantId()))
            .and(SYS_TENANT.CONTACT_USER_NAME.like(bo.getContactUserName()))
            .and(SYS_TENANT.CONTACT_PHONE.eq(bo.getContactPhone()))
            .and(SYS_TENANT.COMPANY_NAME.like(bo.getCompanyName()))
            .and(SYS_TENANT.LICENSE_NUMBER.eq(bo.getLicenseNumber()))
            .and(SYS_TENANT.ADDRESS.eq(bo.getAddress()))
            .and(SYS_TENANT.INTRO.eq(bo.getIntro()))
            .and(SYS_TENANT.DOMAIN.like(bo.getDomain()))
            .and(SYS_TENANT.PACKAGE_ID.eq(bo.getPackageId()))
            .and(SYS_TENANT.EXPIRE_TIME.eq(bo.getExpireTime()))
            .and(SYS_TENANT.ACCOUNT_COUNT.eq(bo.getAccountCount()))
            .and(SYS_TENANT.STATUS.eq(bo.getStatus()))
            .orderBy(SYS_TENANT.ID, true);
    }

    /**
     * 新增租户
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(SysTenantBo bo) {
        SysTenant add = MapstructUtils.convert(bo, SysTenant.class);

        // 获取所有租户编号
        return TenantHelper.ignore(() -> {
            TenantManager.ignoreTenantCondition();

            //此处操作的数据不会带有 tenant_id 的条件
            List<String> tenantIds = baseMapper.selectObjectListByQueryAs(
                QueryWrapper.create().select(SYS_TENANT.ID).from(SYS_TENANT), String.class);

            String tenantId = generateTenantId(tenantIds);
            add.setTenantId(tenantId);
            boolean flag = baseMapper.insert(add, true) > 0;
            if (!flag) {
                throw new ServiceException("创建租户失败");
            }
            bo.setId(add.getId());


            // 根据套餐创建角色
            Long roleId = createTenantRole(tenantId, bo.getPackageId());

            // 创建部门: 公司名是部门名称
            SysDept dept = new SysDept();
            dept.setTenantId(tenantId);
            dept.setDeptName(bo.getCompanyName());
            dept.setParentId(Constants.TOP_PARENT_ID);
            dept.setAncestors(Constants.TOP_PARENT_ID.toString());
            deptMapper.insert(dept, true);
            Long deptId = dept.getDeptId();

            // 角色和部门关联表
            SysRoleDept roleDept = new SysRoleDept();
            roleDept.setRoleId(roleId);
            roleDept.setDeptId(deptId);
            roleDeptMapper.insertWithPk(roleDept);

            // 创建系统用户
            SysUser user = new SysUser();
            user.setTenantId(tenantId);
            user.setUserName(bo.getUsername());
            user.setNickName(bo.getUsername());
            user.setPassword(BCrypt.hashpw(bo.getPassword()));
            user.setDeptId(deptId);
            userMapper.insert(user, true);
            //新增系统用户后，默认当前用户为部门的负责人
            SysDept sd = new SysDept();
            sd.setLeader(user.getUserId());
            sd.setDeptId(deptId);
            deptMapper.update(sd);

            // 用户和角色关联表
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(user.getUserId());
            userRole.setRoleId(roleId);
            userRoleMapper.insertWithPk(userRole);

            String defaultTenantId = TenantConstants.DEFAULT_TENANT_ID;
            List<SysDictType> dictTypeList = dictTypeMapper.selectListByQuery(QueryWrapper.create().from(SYS_DICT_TYPE)
                .where(SYS_DICT_TYPE.TENANT_ID.eq(defaultTenantId)));
            List<SysDictData> dictDataList = dictDataMapper.selectListByQuery(
                QueryWrapper.create().from(SYS_DICT_DATA)
                    .where(SYS_DICT_DATA.TENANT_ID.eq(defaultTenantId)));
            for (SysDictType dictType : dictTypeList) {
                dictType.setDictId(null);
                dictType.setTenantId(tenantId);
            }
            for (SysDictData dictData : dictDataList) {
                dictData.setDictCode(null);
                dictData.setTenantId(tenantId);
            }
            dictTypeMapper.insertBatch(dictTypeList);
            dictDataMapper.insertBatch(dictDataList);

            List<SysConfig> sysConfigList = configMapper.selectListByQuery(
                QueryWrapper.create().from(SYS_CONFIG).where(SYS_CONFIG.TENANT_ID.eq(defaultTenantId)));
            for (SysConfig config : sysConfigList) {
                config.setConfigId(null);
                config.setTenantId(tenantId);
            }
            configMapper.insertBatch(sysConfigList);
            return true;
        });

    }

    /**
     * 生成租户id
     *
     * @param tenantIds 已有租户id列表
     * @return 租户id
     */
    private String generateTenantId(List<String> tenantIds) {
        // 随机生成6位
        String numbers = RandomUtil.randomNumbers(6);
        // 判断是否存在，如果存在则重新生成
        if (tenantIds.contains(numbers)) {
            generateTenantId(tenantIds);
        }
        return numbers;
    }

    /**
     * 根据租户菜单创建租户角色
     *
     * @param tenantId  租户编号
     * @param packageId 租户套餐id
     * @return 角色id
     */
    private Long createTenantRole(String tenantId, Long packageId) {
        // 获取租户套餐
        SysTenantPackage tenantPackage = tenantPackageMapper.selectOneById(packageId);
        if (ObjectUtil.isNull(tenantPackage)) {
            throw new ServiceException("套餐不存在");
        }
        // 获取套餐菜单id
        List<Long> menuIds = StringUtils.splitTo(tenantPackage.getMenuIds(), Convert::toLong);

        // 创建角色
        SysRole role = new SysRole();
        role.setTenantId(tenantId);
        role.setRoleName(TenantConstants.TENANT_ADMIN_ROLE_NAME);
        role.setRoleKey(TenantConstants.TENANT_ADMIN_ROLE_KEY);
        role.setRoleSort(1);
        role.setStatus(TenantConstants.NORMAL);
        roleMapper.insert(role, true);
        Long roleId = role.getRoleId();

        // 创建角色菜单
        List<SysRoleMenu> roleMenus = new ArrayList<>(menuIds.size());
        menuIds.forEach(menuId -> {
            SysRoleMenu roleMenu = new SysRoleMenu();
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menuId);
            roleMenus.add(roleMenu);
        });
        Db.executeBatch(roleMenus, 1000, SysRoleMenuMapper.class, BaseMapper::insertWithPk);

        return roleId;
    }

    /**
     * 修改租户
     */
    @CacheEvict(cacheNames = CacheNames.SYS_TENANT, key = "#bo.tenantId")
    @Override
    public Boolean updateByBo(SysTenantBo bo) {
        SysTenant tenant = MapstructUtils.convert(bo, SysTenant.class);
        tenant.setTenantId(null);
        tenant.setPackageId(null);
        return TenantHelper.ignore(() -> baseMapper.update(tenant) > 0);
    }

    /**
     * 修改租户状态
     *
     * @param bo 租户信息
     * @return 结果
     */
    @CacheEvict(cacheNames = CacheNames.SYS_TENANT, key = "#bo.tenantId")
    @Override
    public int updateTenantStatus(SysTenantBo bo) {
        SysTenant tenant = MapstructUtils.convert(bo, SysTenant.class);
        return TenantHelper.ignore(() -> baseMapper.update(tenant));
    }

    /**
     * 校验租户是否允许操作
     *
     * @param tenantId 租户ID
     */
    @Override
    public void checkTenantAllowed(String tenantId) {
        if (ObjectUtil.isNotNull(tenantId) && TenantConstants.DEFAULT_TENANT_ID.equals(tenantId)) {
            throw new ServiceException("不允许操作管理租户");
        }
    }

    /**
     * 批量删除租户
     */
    @CacheEvict(cacheNames = CacheNames.SYS_TENANT, allEntries = true)
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            // 做一些业务上的校验,判断是否需要校验
            if (ids.contains(TenantConstants.SUPER_ADMIN_ID)) {
                throw new ServiceException("超管租户不能删除");
            }
        }
        return TenantHelper.ignore(() -> baseMapper.deleteBatchByIds(ids) > 0);
    }

    /**
     * 校验企业名称是否唯一
     */
    @Override
    public boolean checkCompanyNameUnique(SysTenantBo bo) {
        return TenantHelper.ignore(() -> baseMapper.selectCountByQuery(
            QueryWrapper.create().from(SYS_TENANT).where(SYS_TENANT.COMPANY_NAME.eq(bo.getCompanyName()))
                .and(SYS_TENANT.TENANT_ID.ne(bo.getTenantId())))) == 0;
    }

    /**
     * 校验账号余额
     */
    @Override
    public boolean checkAccountBalance(String tenantId) {
        SysTenantVo tenant = SpringUtils.getAopProxy(this).queryByTenantId(tenantId);
        // 如果余额为-1代表不限制
        if (tenant.getAccountCount() == -1) {
            return true;
        }
        Long userNumber = userMapper.selectCountByQuery(new QueryWrapper());
        // 如果余额大于0代表还有可用名额
        return tenant.getAccountCount() - userNumber > 0;
    }

    /**
     * 校验有效期
     */
    @Override
    public boolean checkExpireTime(String tenantId) {
        SysTenantVo tenant = SpringUtils.getAopProxy(this).queryByTenantId(tenantId);
        // 如果未设置过期时间代表不限制
        if (ObjectUtil.isNull(tenant.getExpireTime())) {
            return true;
        }
        // 如果当前时间在过期时间之前则通过
        return new Date().before(tenant.getExpireTime());
    }

    /**
     * 同步租户套餐
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean syncTenantPackage(String tenantId, Long packageId) {
        SysTenantPackage tenantPackage = tenantPackageMapper.selectOneById(packageId);
        List<SysRole> roles = TenantHelper.ignore(() -> roleMapper.selectListByQuery(
            QueryWrapper.create().from(SYS_ROLE).where(SYS_ROLE.TENANT_ID.eq(tenantId))));
        List<Long> roleIds = new ArrayList<>(roles.size() - 1);
        List<Long> menuIds = StringUtils.splitTo(tenantPackage.getMenuIds(), Convert::toLong);
        roles.forEach(item -> {
            if (TenantConstants.TENANT_ADMIN_ROLE_KEY.equals(item.getRoleKey())) {
                List<SysRoleMenu> roleMenus = new ArrayList<>(menuIds.size());
                menuIds.forEach(menuId -> {
                    SysRoleMenu roleMenu = new SysRoleMenu();
                    roleMenu.setRoleId(item.getRoleId());
                    roleMenu.setMenuId(menuId);
                    roleMenus.add(roleMenu);
                });
                roleMenuMapper.deleteByQuery(QueryWrapper.create().from(SYS_ROLE_MENU).where(SYS_ROLE_MENU.ROLE_ID.eq(item.getRoleId())));
                Db.executeBatch(roleMenus, 1000, SysRoleMenuMapper.class, BaseMapper::insertWithPk);
            } else {
                roleIds.add(item.getRoleId());
            }
        });
        if (!roleIds.isEmpty()) {
            roleMenuMapper.deleteByQuery(
                QueryWrapper.create().from(SYS_ROLE_MENU).where(SYS_ROLE_MENU.ROLE_ID.in(roleIds))
                    .and(SYS_ROLE_MENU.MENU_ID.notIn(menuIds)));
        }
        return true;
    }
}
