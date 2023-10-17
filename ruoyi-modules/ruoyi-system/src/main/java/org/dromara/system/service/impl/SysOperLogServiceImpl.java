package org.dromara.system.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.core.utils.ip.AddressUtils;
import org.dromara.common.log.event.OperLogEvent;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.system.domain.SysOperLog;
import org.dromara.system.domain.bo.SysOperLogBo;
import org.dromara.system.domain.vo.SysOperLogVo;
import org.dromara.system.mapper.SysOperLogMapper;
import org.dromara.system.service.ISysOperLogService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.dromara.system.domain.table.SysOperLogTableDef.SYS_OPER_LOG;

/**
 * 操作日志 服务层处理
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Service
public class SysOperLogServiceImpl implements ISysOperLogService {

    private final SysOperLogMapper baseMapper;

    /**
     * 操作日志记录
     *
     * @param operLogEvent 操作日志事件
     */
    @Async
    @EventListener
    public void recordOper(OperLogEvent operLogEvent) {
        SysOperLogBo operLog = MapstructUtils.convert(operLogEvent, SysOperLogBo.class);
        // 远程查询操作地点
        operLog.setOperLocation(AddressUtils.getRealAddressByIP(operLog.getOperIp()));
        insertOperlog(operLog);
    }

    @Override
    public TableDataInfo<SysOperLogVo> selectPageOperLogList(SysOperLogBo operLog, PageQuery pageQuery) {
        QueryWrapper lqw = buildQueryWrapper(operLog);
        if (StringUtils.isBlank(pageQuery.getOrderByColumn())) {
            lqw.orderBy(SYS_OPER_LOG.OPER_ID, false);
        }
        Page<SysOperLogVo> page = baseMapper.paginateAs(pageQuery.build(), lqw, SysOperLogVo.class);
        return TableDataInfo.build(page);
    }

    private static QueryWrapper buildQueryWrapper(SysOperLogBo operLog) {
        Map<String, Object> params = operLog.getParams();

        QueryWrapper queryWrapper = QueryWrapper.create().from(SYS_OPER_LOG)
            .like(StringUtils.isNotBlank(operLog.getOperIp()), SysOperLog::getOperIp, operLog.getOperIp())

            .where(SYS_OPER_LOG.TITLE.like(operLog.getTitle()))
            .and(SYS_OPER_LOG.BUSINESS_TYPE.eq(operLog.getBusinessType(), operLog.getBusinessType() != null && operLog.getBusinessType() > 0))
            .and(SYS_OPER_LOG.STATUS.eq(operLog.getStatus()))
            .and(SYS_OPER_LOG.OPER_NAME.like(operLog.getOperName()))
            .and(SYS_OPER_LOG.OPER_TIME.between(params.get("beginTime"), params.get("endTime"), params.get("beginTime") != null && params.get("endTime") != null));
        if (ArrayUtils.isNotEmpty(operLog.getBusinessTypes())) {
            queryWrapper.and(SYS_OPER_LOG.BUSINESS_TYPE.in(Arrays.asList(operLog.getBusinessTypes())));
        }
        return queryWrapper;
    }

    /**
     * 新增操作日志
     *
     * @param bo 操作日志对象
     */
    @Override
    public void insertOperlog(SysOperLogBo bo) {
        SysOperLog operLog = MapstructUtils.convert(bo, SysOperLog.class);
        operLog.setOperTime(new Date());
        baseMapper.insert(operLog,true);
    }

    /**
     * 查询系统操作日志集合
     *
     * @param operLog 操作日志对象
     * @return 操作日志集合
     */
    @Override
    public List<SysOperLogVo> selectOperLogList(SysOperLogBo operLog) {
        QueryWrapper queryWrapper = buildQueryWrapper(operLog);
        queryWrapper.orderBy(SYS_OPER_LOG.OPER_ID, false);
        return baseMapper.selectListByQueryAs(queryWrapper, SysOperLogVo.class);
    }

    /**
     * 批量删除系统操作日志
     *
     * @param operIds 需要删除的操作日志ID
     * @return 结果
     */
    @Override
    public int deleteOperLogByIds(Long[] operIds) {
        return baseMapper.deleteBatchByIds(Arrays.asList(operIds));
    }

    /**
     * 查询操作日志详细
     *
     * @param operId 操作ID
     * @return 操作日志对象
     */
    @Override
    public SysOperLogVo selectOperLogById(Long operId) {
        return baseMapper.selectOneWithRelationsByIdAs(operId, SysOperLogVo.class);
    }

    /**
     * 清空操作日志
     */
    @Override
    public void cleanOperLog() {
        baseMapper.deleteByQuery(QueryWrapper.create().from(SYS_OPER_LOG));
    }
}
