package org.dromara.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.constant.TenantConstants;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.system.domain.SysTenant;
import org.dromara.system.domain.SysTenantPackage;
import org.dromara.system.domain.bo.SysTenantPackageBo;
import org.dromara.system.domain.vo.SysTenantPackageVo;
import org.dromara.system.mapper.SysTenantMapper;
import org.dromara.system.mapper.SysTenantPackageMapper;
import org.dromara.system.service.ISysTenantPackageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.dromara.system.domain.table.SysTenantPackageTableDef.SYS_TENANT_PACKAGE;
import static org.dromara.system.domain.table.SysTenantTableDef.SYS_TENANT;

/**
 * 租户套餐Service业务层处理
 *
 * @author Michelle.Chung
 */
@RequiredArgsConstructor
@Service
public class SysTenantPackageServiceImpl implements ISysTenantPackageService {

    private final SysTenantPackageMapper baseMapper;
    private final SysTenantMapper tenantMapper;

    /**
     * 查询租户套餐
     */
    @Override
    public SysTenantPackageVo queryById(Long packageId){
        return baseMapper.selectOneWithRelationsByIdAs(packageId, SysTenantPackageVo.class);
    }

    /**
     * 查询租户套餐列表
     */
    @Override
    public TableDataInfo<SysTenantPackageVo> queryPageList(SysTenantPackageBo bo, PageQuery pageQuery) {
        QueryWrapper lqw = buildQueryWrapper(bo);
        Page<SysTenantPackageVo> result = baseMapper.paginateAs(pageQuery, lqw, SysTenantPackageVo.class);
        return TableDataInfo.build(result);
    }

    @Override
    public List<SysTenantPackageVo> selectList() {
        return baseMapper.selectListByQueryAs(QueryWrapper.create().from(SYS_TENANT_PACKAGE)
            .where(SYS_TENANT_PACKAGE.STATUS.eq(TenantConstants.NORMAL)), SysTenantPackageVo.class);
    }

    /**
     * 查询租户套餐列表
     */
    @Override
    public List<SysTenantPackageVo> queryList(SysTenantPackageBo bo) {
        QueryWrapper lqw = buildQueryWrapper(bo);
        return baseMapper.selectListByQueryAs(lqw, SysTenantPackageVo.class);
    }

    private QueryWrapper buildQueryWrapper(SysTenantPackageBo bo) {
        return QueryWrapper.create()
            .from(SYS_TENANT_PACKAGE)
            .where(SYS_TENANT_PACKAGE.PACKAGE_NAME.like(bo.getPackageName()))
            .and(SYS_TENANT_PACKAGE.STATUS.eq(bo.getStatus()))
            .orderBy(SYS_TENANT_PACKAGE.PACKAGE_ID, true);
    }

    /**
     * 新增租户套餐
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(SysTenantPackageBo bo) {
        SysTenantPackage add = MapstructUtils.convert(bo, SysTenantPackage.class);
        // 保存菜单id
        List<Long> menuIds = Arrays.asList(bo.getMenuIds());
        if (CollUtil.isNotEmpty(menuIds)) {
            add.setMenuIds(StringUtils.join(menuIds, ", "));
        } else {
            add.setMenuIds("");
        }
        boolean flag = baseMapper.insert(add,true) > 0;
        if (flag) {
            bo.setPackageId(add.getPackageId());
        }
        return flag;
    }

    /**
     * 修改租户套餐
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(SysTenantPackageBo bo) {
        SysTenantPackage update = MapstructUtils.convert(bo, SysTenantPackage.class);
        // 保存菜单id
        List<Long> menuIds = Arrays.asList(bo.getMenuIds());
        if (CollUtil.isNotEmpty(menuIds)) {
            update.setMenuIds(StringUtils.join(menuIds, ", "));
        } else {
            update.setMenuIds("");
        }
        return baseMapper.update(update) > 0;
    }

    /**
     * 修改套餐状态
     *
     * @param bo 套餐信息
     * @return 结果
     */
    @Override
    public int updatePackageStatus(SysTenantPackageBo bo) {
        SysTenantPackage tenantPackage = MapstructUtils.convert(bo, SysTenantPackage.class);
        return baseMapper.update(tenantPackage);
    }

    /**
     * 批量删除租户套餐
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            boolean exists = tenantMapper.selectCountByQuery(QueryWrapper.create().from(SYS_TENANT).where(SYS_TENANT.PACKAGE_ID.in(ids))) >0;
            if (exists) {
                throw new ServiceException("租户套餐已被使用");
            }
        }
        return baseMapper.deleteBatchByIds(ids) > 0;
    }

}
