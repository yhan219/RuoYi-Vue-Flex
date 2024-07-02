package org.dromara.workflow.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.row.Db;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.StreamUtils;
import org.dromara.workflow.domain.WfNodeConfig;
import org.dromara.workflow.domain.vo.WfFormManageVo;
import org.dromara.workflow.domain.vo.WfNodeConfigVo;
import org.dromara.workflow.mapper.WfNodeConfigMapper;
import org.dromara.workflow.service.IWfFormManageService;
import org.dromara.workflow.service.IWfNodeConfigService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * 节点配置Service业务层处理
 *
 * @author may
 * @date 2024-03-30
 */
@RequiredArgsConstructor
@Service
public class WfNodeConfigServiceImpl implements IWfNodeConfigService {

    private final WfNodeConfigMapper baseMapper;
    private final IWfFormManageService wfFormManageService;

    /**
     * 查询节点配置
     */
    @Override
    public WfNodeConfigVo queryById(Long id) {
        return baseMapper.selectOneByQueryAs(QueryWrapper.create().eq(WfNodeConfig::getId, id), WfNodeConfigVo.class);
    }

    /**
     * 保存节点配置
     */
    @Override
    public Boolean saveOrUpdate(List<WfNodeConfig> list) {
        int[] result = Db.executeBatch(list.size(), 1000, WfNodeConfigMapper.class, (mapper, index) -> {
            WfNodeConfig it = list.get(index);
            mapper.insertOrUpdate(it);
        });
        return result.length > 0;
    }

    /**
     * 批量删除节点配置
     */
    @Override
    public Boolean deleteByIds(Collection<Long> ids) {
        return baseMapper.deleteBatchByIds(ids) > 0;
    }



    @Override
    public Boolean deleteByDefIds(Collection<String> ids) {
        return baseMapper.deleteByQuery(QueryWrapper.create().in(WfNodeConfig::getDefinitionId, ids)) > 0;
    }

    @Override
    public List<WfNodeConfigVo> selectByDefIds(Collection<String> ids) {
        List<WfNodeConfigVo> wfNodeConfigVos = baseMapper.selectListByQueryAs(QueryWrapper.create().in(WfNodeConfig::getDefinitionId, ids), WfNodeConfigVo.class);
        if (CollUtil.isNotEmpty(wfNodeConfigVos)) {
            List<Long> formIds = StreamUtils.toList(wfNodeConfigVos, WfNodeConfigVo::getFormId);
            List<WfFormManageVo> wfFormManageVos = wfFormManageService.queryByIds(formIds);
            for (WfNodeConfigVo wfNodeConfigVo : wfNodeConfigVos) {
                wfFormManageVos.stream().filter(e -> ObjectUtil.equals(e.getId(), wfNodeConfigVo.getFormId())).findFirst().ifPresent(wfNodeConfigVo::setWfFormManageVo);
            }
        }
        return wfNodeConfigVos;
    }
}
