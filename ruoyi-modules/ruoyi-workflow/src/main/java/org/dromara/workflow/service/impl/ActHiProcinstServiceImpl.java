package org.dromara.workflow.service.impl;


import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.workflow.domain.ActHiProcinst;
import org.dromara.workflow.mapper.ActHiProcinstMapper;
import org.dromara.workflow.service.IActHiProcinstService;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * 流程实例Service业务层处理
 *
 * @author may
 * @date 2023-07-22
 */
@RequiredArgsConstructor
@Service
public class ActHiProcinstServiceImpl implements IActHiProcinstService {

    private final ActHiProcinstMapper baseMapper;

    /**
     * 按照业务id查询
     *
     * @param businessKeys 业务id
     */
    @Override
    public List<ActHiProcinst> selectByBusinessKeyIn(List<String> businessKeys) {
        return baseMapper.selectListByQuery(QueryWrapper.create()
            .in(ActHiProcinst::getBusinessKey, businessKeys)
            .eq(ActHiProcinst::getTenantId, TenantHelper.getTenantId(),TenantHelper.isEnable()));
    }

    /**
     * 按照业务id查询
     *
     * @param businessKey 业务id
     */
    @Override
    public ActHiProcinst selectByBusinessKey(String businessKey) {
        return baseMapper.selectOneByQuery((QueryWrapper.create()
            .eq(ActHiProcinst::getBusinessKey, businessKey)
            .eq(ActHiProcinst::getTenantId, TenantHelper.getTenantId(), TenantHelper.isEnable())));

    }
}
