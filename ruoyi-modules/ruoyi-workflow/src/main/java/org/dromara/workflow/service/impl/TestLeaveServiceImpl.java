package org.dromara.workflow.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StreamUtils;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.workflow.domain.TestLeave;
import org.dromara.workflow.domain.bo.TestLeaveBo;
import org.dromara.workflow.domain.vo.TestLeaveVo;
import org.dromara.workflow.mapper.TestLeaveMapper;
import org.dromara.workflow.service.ITestLeaveService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * 请假Service业务层处理
 *
 * @author may
 * @date 2023-07-21
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class TestLeaveServiceImpl implements ITestLeaveService {

    private final TestLeaveMapper baseMapper;
    private final WorkflowService workflowService;

    /**
     * 查询请假
     */
    @Override
    public TestLeaveVo queryById(Long id) {
        return baseMapper.selectOneByQueryAs(QueryWrapper.create().eq(TestLeave::getId, id), TestLeaveVo.class);
    }

    /**
     * 查询请假列表
     */
    @Override
    public TableDataInfo<TestLeaveVo> queryPageList(TestLeaveBo bo, PageQuery pageQuery) {
        QueryWrapper lqw = buildQueryWrapper(bo);
        Page<TestLeaveVo> result = baseMapper.paginateAs(pageQuery.build(), lqw, TestLeaveVo.class);
        TableDataInfo<TestLeaveVo> build = TableDataInfo.build(result);
        List<TestLeaveVo> rows = build.getRows();
        if (CollUtil.isNotEmpty(rows)) {
            List<String> ids = StreamUtils.toList(rows, e -> String.valueOf(e.getId()));
            WorkflowUtils.setProcessInstanceListVo(rows, ids, "id");
        }
        return build;
    }

    /**
     * 查询请假列表
     */
    @Override
    public List<TestLeaveVo> queryList(TestLeaveBo bo) {
        QueryWrapper lqw = buildQueryWrapper(bo);
        return baseMapper.selectListByQueryAs(lqw, TestLeaveVo.class);
    }

    private QueryWrapper buildQueryWrapper(TestLeaveBo bo) {
        QueryWrapper lqw = QueryWrapper.create();
        lqw.eq(TestLeave::getLeaveType, bo.getLeaveType());
        lqw.ge(TestLeave::getLeaveDays, bo.getStartLeaveDays());
        lqw.le(TestLeave::getLeaveDays, bo.getEndLeaveDays());
        lqw.orderBy(BaseEntity::getCreateTime, false);
        return lqw;
    }

    /**
     * 新增请假
     */
    @Override
    public TestLeaveVo insertByBo(TestLeaveBo bo) {
        TestLeave add = MapstructUtils.convert(bo, TestLeave.class);
        if (StringUtils.isBlank(add.getStatus())) {
            add.setStatus(BusinessStatusEnum.DRAFT.getStatus());
        }
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return MapstructUtils.convert(add, TestLeaveVo.class);
    }

    /**
     * 修改请假
     */
    @Override
    public TestLeaveVo updateByBo(TestLeaveBo bo) {
        TestLeave update = MapstructUtils.convert(bo, TestLeave.class);
        baseMapper.update(update);
        return MapstructUtils.convert(update, TestLeaveVo.class);
    }

    /**
     * 批量删除请假
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithValidByIds(Collection<Long> ids) {
        List<String> idList = StreamUtils.toList(ids, String::valueOf);
        actProcessInstanceService.deleteRunAndHisInstance(idList);
        return baseMapper.deleteBatchByIds(ids) > 0;
    }

    /**
     * 总体流程监听(例如: 提交 退回 撤销 终止 作废等)
     * 正常使用只需#processEvent.key=='leave1'
     * 示例为了方便则使用startsWith匹配了全部示例key
     *
     * @param processEvent 参数
     */
    @EventListener(condition = "#processEvent.key.startsWith('leave')")
    public void processHandler(ProcessEvent processEvent) {
        log.info("当前任务执行了{}", processEvent.toString());
        TestLeave testLeave = baseMapper.selectById(Long.valueOf(processEvent.getBusinessKey()));
        testLeave.setStatus(processEvent.getStatus());
        if (processEvent.isSubmit()) {
            testLeave.setStatus(BusinessStatusEnum.WAITING.getStatus());
        }
        baseMapper.updateById(testLeave);
    }

    /**
     * 执行办理任务监听
     * 示例：也可通过  @EventListener(condition = "#processTaskEvent.key=='leave1'")进行判断
     * 在方法中判断流程节点key
     * if ("xxx".equals(processTaskEvent.getTaskDefinitionKey())) {
     * //执行业务逻辑
     * }
     *
     * @param processTaskEvent 参数
     */
    @EventListener(condition = "#processTaskEvent.key=='leave1' && #processTaskEvent.taskDefinitionKey=='Activity_14633hx'")
    public void processTaskHandler(ProcessTaskEvent processTaskEvent) {
        log.info("当前任务执行了{}", processTaskEvent.toString());
        TestLeave testLeave = baseMapper.selectById(Long.valueOf(processTaskEvent.getBusinessKey()));
        testLeave.setStatus(BusinessStatusEnum.WAITING.getStatus());
        baseMapper.updateById(testLeave);
    }
}
