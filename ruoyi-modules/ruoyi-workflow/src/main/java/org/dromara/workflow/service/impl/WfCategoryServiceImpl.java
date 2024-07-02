package org.dromara.workflow.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.workflow.domain.WfCategory;
import org.dromara.workflow.domain.bo.WfCategoryBo;
import org.dromara.workflow.domain.vo.WfCategoryVo;
import org.dromara.workflow.mapper.WfCategoryMapper;
import org.dromara.workflow.service.IWfCategoryService;
import org.dromara.workflow.utils.QueryUtils;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.Model;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * 流程分类Service业务层处理
 *
 * @author may
 * @date 2023-06-28
 */
@RequiredArgsConstructor
@Service
public class WfCategoryServiceImpl implements IWfCategoryService {

    private final WfCategoryMapper baseMapper;

    private final RepositoryService repositoryService;

    /**
     * 查询流程分类
     */
    @Override
    public WfCategoryVo queryById(Long id) {
        return baseMapper.selectOneByQueryAs(QueryWrapper.create().eq(WfCategory::getId, id), WfCategoryVo.class);
    }


    /**
     * 查询流程分类列表
     */
    @Override
    public List<WfCategoryVo> queryList(WfCategoryBo bo) {
        QueryWrapper lqw = buildQueryWrapper(bo);
        return baseMapper.selectListByQueryAs(lqw, WfCategoryVo.class);
    }

    private QueryWrapper buildQueryWrapper(WfCategoryBo bo) {
        QueryWrapper lqw = QueryWrapper.create();
        lqw.like(WfCategory::getCategoryName, bo.getCategoryName());
        lqw.eq(WfCategory::getCategoryCode, bo.getCategoryCode());
        return lqw;
    }

    /**
     * 新增流程分类
     */
    @Override
    public Boolean insertByBo(WfCategoryBo bo) {
        WfCategory add = MapstructUtils.convert(bo, WfCategory.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改流程分类
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(WfCategoryBo bo) {
        WfCategory update = MapstructUtils.convert(bo, WfCategory.class);
        validEntityBeforeSave(update);
        WfCategoryVo wfCategoryVo = baseMapper.selectOneByQueryAs(QueryWrapper.create().eq(WfCategory::getId, update.getId()), WfCategoryVo.class);
        List<ProcessDefinition> processDefinitionList = QueryUtils.definitionQuery().processDefinitionCategory(wfCategoryVo.getCategoryCode()).list();
        for (ProcessDefinition processDefinition : processDefinitionList) {
            repositoryService.setProcessDefinitionCategory(processDefinition.getId(), bo.getCategoryCode());
        }
        List<Deployment> deploymentList = QueryUtils.deploymentQuery().deploymentCategory(wfCategoryVo.getCategoryCode()).list();
        for (Deployment deployment : deploymentList) {
            repositoryService.setDeploymentCategory(deployment.getId(), bo.getCategoryCode());
        }
        List<Model> modelList = QueryUtils.modelQuery().modelCategory(wfCategoryVo.getCategoryCode()).list();
        for (Model model : modelList) {
            model.setCategory(bo.getCategoryCode());
            repositoryService.saveModel(model);
        }
        return baseMapper.update(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(WfCategory entity) {
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除流程分类
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchByIds(ids) > 0;
    }

    /**
     * 按照类别编码查询
     *
     * @param categoryCode 分类比吗
     */
    @Override
    public WfCategory queryByCategoryCode(String categoryCode) {
        return baseMapper.selectOneByQuery(QueryWrapper.create().eq(WfCategory::getCategoryCode, categoryCode));
    }
}
