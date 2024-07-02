package org.dromara.workflow.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.workflow.common.enums.FormTypeEnum;
import org.dromara.workflow.domain.WfFormManage;
import org.dromara.workflow.domain.bo.WfFormManageBo;
import org.dromara.workflow.domain.vo.WfFormManageVo;
import org.dromara.workflow.mapper.WfFormManageMapper;
import org.dromara.workflow.service.IWfFormManageService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * 表单管理Service业务层处理
 *
 * @author may
 * @date 2024-03-29
 */
@RequiredArgsConstructor
@Service
public class WfFormManageServiceImpl implements IWfFormManageService {

    private final WfFormManageMapper baseMapper;

    /**
     * 查询表单管理
     */
    @Override
    public WfFormManageVo queryById(Long id) {
        return baseMapper.selectOneByQueryAs(QueryWrapper.create().eq(WfFormManage::getId, id), WfFormManageVo.class);
    }

    @Override
    public List<WfFormManageVo> queryByIds(List<Long> ids) {
        return baseMapper.selectListByQueryAs(QueryWrapper.create().in(WfFormManage::getId, ids), WfFormManageVo.class);
    }

    /**
     * 查询表单管理列表
     */
    @Override
    public TableDataInfo<WfFormManageVo> queryPageList(WfFormManageBo bo, PageQuery pageQuery) {
        QueryWrapper lqw = buildQueryWrapper(bo);
        Page<WfFormManageVo> result = baseMapper.paginateAs(pageQuery.build(), lqw, WfFormManageVo.class);
        return TableDataInfo.build(result);
    }

    @Override
    public List<WfFormManageVo> selectList() {
        List<WfFormManageVo> wfFormManageVos = baseMapper.selectListByQueryAs(QueryWrapper.create().orderBy(WfFormManage::getUpdateTime, false), WfFormManageVo.class);
        for (WfFormManageVo wfFormManageVo : wfFormManageVos) {
            wfFormManageVo.setFormTypeName(FormTypeEnum.findByType(wfFormManageVo.getFormType()));
        }
        return wfFormManageVos;
    }

    /**
     * 查询表单管理列表
     */
    @Override
    public List<WfFormManageVo> queryList(WfFormManageBo bo) {
        QueryWrapper lqw = buildQueryWrapper(bo);
        return baseMapper.selectListByQueryAs(lqw, WfFormManageVo.class);
    }

    private QueryWrapper buildQueryWrapper(WfFormManageBo bo) {
        QueryWrapper lqw = QueryWrapper.create();
        lqw.like(WfFormManage::getFormName, bo.getFormName());
        lqw.eq(WfFormManage::getFormType, bo.getFormType());
        return lqw;
    }

    /**
     * 新增表单管理
     */
    @Override
    public Boolean insertByBo(WfFormManageBo bo) {
        WfFormManage add = MapstructUtils.convert(bo, WfFormManage.class);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改表单管理
     */
    @Override
    public Boolean updateByBo(WfFormManageBo bo) {
        WfFormManage update = MapstructUtils.convert(bo, WfFormManage.class);
        return baseMapper.update(update) > 0;
    }

    /**
     * 批量删除表单管理
     */
    @Override
    public Boolean deleteByIds(Collection<Long> ids) {
        return baseMapper.deleteBatchByIds(ids) > 0;
    }
}
