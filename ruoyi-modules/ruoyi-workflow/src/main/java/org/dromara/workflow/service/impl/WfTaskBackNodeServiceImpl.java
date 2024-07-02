package org.dromara.workflow.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.workflow.domain.WfTaskBackNode;
import org.dromara.workflow.domain.vo.MultiInstanceVo;
import org.dromara.workflow.mapper.WfTaskBackNodeMapper;
import org.dromara.workflow.service.IWfTaskBackNodeService;
import org.dromara.workflow.utils.WorkflowUtils;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.dromara.workflow.common.constant.FlowConstant.MULTI_INSTANCE;
import static org.dromara.workflow.common.constant.FlowConstant.USER_TASK;


/**
 * 节点驳回记录Service业务层处理
 *
 * @author may
 * @date 2024-03-13
 */
@RequiredArgsConstructor
@Service
public class WfTaskBackNodeServiceImpl implements IWfTaskBackNodeService {

    private final WfTaskBackNodeMapper wfTaskBackNodeMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordExecuteNode(Task task) {
        List<WfTaskBackNode> list = getListByInstanceId(task.getProcessInstanceId());
        WfTaskBackNode wfTaskBackNode = new WfTaskBackNode();
        wfTaskBackNode.setNodeId(task.getTaskDefinitionKey());
        wfTaskBackNode.setNodeName(task.getName());
        wfTaskBackNode.setInstanceId(task.getProcessInstanceId());
        wfTaskBackNode.setAssignee(String.valueOf(LoginHelper.getUserId()));
        MultiInstanceVo multiInstance = WorkflowUtils.isMultiInstance(task.getProcessDefinitionId(), task.getTaskDefinitionKey());
        if (ObjectUtil.isNotEmpty(multiInstance)) {
            wfTaskBackNode.setTaskType(MULTI_INSTANCE);
        } else {
            wfTaskBackNode.setTaskType(USER_TASK);
        }
        if (CollUtil.isEmpty(list)) {
            wfTaskBackNode.setOrderNo(0);
            wfTaskBackNodeMapper.insert(wfTaskBackNode);
        } else {
            WfTaskBackNode taskNode = list.stream().filter(e -> e.getNodeId().equals(wfTaskBackNode.getNodeId()) && e.getOrderNo() == 0).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(taskNode)) {
                wfTaskBackNode.setOrderNo(list.get(0).getOrderNo() + 1);
                WfTaskBackNode node = getListByInstanceIdAndNodeId(wfTaskBackNode.getInstanceId(), wfTaskBackNode.getNodeId());
                if (ObjectUtil.isNotEmpty(node)) {
                    node.setAssignee(node.getAssignee() + StringUtils.SEPARATOR + LoginHelper.getUserId());
                    wfTaskBackNodeMapper.update(node);
                } else {
                    wfTaskBackNodeMapper.insert(wfTaskBackNode);
                }
            }
        }
    }

    @Override
    public List<WfTaskBackNode> getListByInstanceId(String processInstanceId) {
        QueryWrapper wrapper = QueryWrapper.create();
        wrapper.eq(WfTaskBackNode::getInstanceId, processInstanceId);
        wrapper.orderBy(WfTaskBackNode::getOrderNo,false);
        return wfTaskBackNodeMapper.selectListByQuery(wrapper);
    }

    @Override
    public WfTaskBackNode getListByInstanceIdAndNodeId(String processInstanceId, String nodeId) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.eq(WfTaskBackNode::getInstanceId, processInstanceId);
        queryWrapper.eq(WfTaskBackNode::getNodeId, nodeId);
        return wfTaskBackNodeMapper.selectOneByQuery(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteBackTaskNode(String processInstanceId, String targetActivityId) {
        try {
            QueryWrapper queryWrapper = QueryWrapper.create();
            queryWrapper.eq(WfTaskBackNode::getInstanceId, processInstanceId);
            queryWrapper.eq(WfTaskBackNode::getNodeId, targetActivityId);
            WfTaskBackNode actTaskNode = wfTaskBackNodeMapper.selectOneByQuery(queryWrapper);
            if (ObjectUtil.isNotNull(actTaskNode)) {
                Integer orderNo = actTaskNode.getOrderNo();
                List<WfTaskBackNode> taskNodeList = getListByInstanceId(processInstanceId);
                List<Long> ids = new ArrayList<>();
                if (CollUtil.isNotEmpty(taskNodeList)) {
                    for (WfTaskBackNode taskNode : taskNodeList) {
                        if (taskNode.getOrderNo() >= orderNo) {
                            ids.add(taskNode.getId());
                        }
                    }
                }
                if (CollUtil.isNotEmpty(ids)) {
                    wfTaskBackNodeMapper.deleteBatchByIds(ids);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException("删除失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByInstanceId(String processInstanceId) {
        QueryWrapper wrapper = QueryWrapper.create();
        wrapper.eq(WfTaskBackNode::getInstanceId, processInstanceId);
        List<WfTaskBackNode> list = wfTaskBackNodeMapper.selectListByQuery(wrapper);
        int delete = wfTaskBackNodeMapper.deleteByQuery(wrapper);
        if (list.size() != delete) {
            throw new ServiceException("删除失败");
        }
        return true;
    }

    @Override
    public boolean deleteByInstanceIds(List<String> processInstanceIds) {
        QueryWrapper wrapper = QueryWrapper.create();
        wrapper.in(WfTaskBackNode::getInstanceId, processInstanceIds);
        List<WfTaskBackNode> list = wfTaskBackNodeMapper.selectListByQuery(wrapper);
        int delete = wfTaskBackNodeMapper.deleteByQuery(wrapper);
        if (list.size() != delete) {
            throw new ServiceException("删除失败");
        }
        return true;
    }
}
