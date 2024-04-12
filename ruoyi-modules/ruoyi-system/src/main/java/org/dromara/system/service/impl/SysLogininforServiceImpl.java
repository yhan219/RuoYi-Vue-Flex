package org.dromara.system.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.system.domain.SysLogininfor;
import org.dromara.system.domain.bo.SysLogininforBo;
import org.dromara.system.domain.vo.SysLogininforVo;
import org.dromara.system.mapper.SysLogininforMapper;
import org.dromara.system.service.ISysLogininforService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.dromara.system.domain.table.SysLogininforTableDef.SYS_LOGININFOR;

/**
 * 系统访问日志情况信息 服务层处理
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class SysLogininforServiceImpl implements ISysLogininforService {

    private final SysLogininforMapper baseMapper;


    private String getBlock(Object msg) {
        if (msg == null) {
            msg = "";
        }
        return "[" + msg.toString() + "]";
    }

    @Override
    public TableDataInfo<SysLogininforVo> selectPageLogininforList(SysLogininforBo logininfor, PageQuery pageQuery) {
        Map<String, Object> params = logininfor.getParams();
        QueryWrapper lqw = QueryWrapper.create().from(SYS_LOGININFOR)
            .where(SYS_LOGININFOR.IPADDR.like(logininfor.getIpaddr()))
            .and(SYS_LOGININFOR.STATUS.eq(logininfor.getStatus()))
            .and(SYS_LOGININFOR.USER_NAME.like(logininfor.getUserName()))
            .and(SYS_LOGININFOR.LOGIN_TIME.between(params.get("beginTime"), params.get("endTime"), params.get("beginTime") != null && params.get("endTime") != null));
        if (StringUtils.isBlank(pageQuery.getOrderByColumn())) {
            lqw.orderBy(SYS_LOGININFOR.INFO_ID, false);
        } else {
            lqw.orderBy(pageQuery.buildOrderBy());
        }
        Page<SysLogininforVo> page = baseMapper.paginateAs(pageQuery.build(), lqw, SysLogininforVo.class);
        return TableDataInfo.build(page);
    }

    /**
     * 新增系统登录日志
     *
     * @param bo 访问日志对象
     */
    @Override
    public void insertLogininfor(SysLogininforBo bo) {
        SysLogininfor logininfor = MapstructUtils.convert(bo, SysLogininfor.class);
        logininfor.setLoginTime(new Date());
        baseMapper.insert(logininfor,true);
    }

    /**
     * 查询系统登录日志集合
     *
     * @param logininfor 访问日志对象
     * @return 登录记录集合
     */
    @Override
    public List<SysLogininforVo> selectLogininforList(SysLogininforBo logininfor) {
        Map<String, Object> params = logininfor.getParams();
        return baseMapper.selectListByQueryAs(QueryWrapper.create().from(SYS_LOGININFOR)
            .where(SYS_LOGININFOR.IPADDR.like(logininfor.getIpaddr()))
            .and(SYS_LOGININFOR.STATUS.eq(logininfor.getStatus()))
            .and(SYS_LOGININFOR.USER_NAME.like(logininfor.getUserName()))
            .and(SYS_LOGININFOR.LOGIN_TIME.between(params.get("beginTime"), params.get("endTime"), params.get("beginTime") != null && params.get("endTime") != null))
            .orderBy(SYS_LOGININFOR.INFO_ID, false), SysLogininforVo.class);
    }

    /**
     * 批量删除系统登录日志
     *
     * @param infoIds 需要删除的登录日志ID
     * @return 结果
     */
    @Override
    public int deleteLogininforByIds(Long[] infoIds) {
        return baseMapper.deleteBatchByIds(Arrays.asList(infoIds));
    }

    /**
     * 清空系统登录日志
     */
    @Override
    public void cleanLogininfor() {
        baseMapper.deleteByQuery(new QueryWrapper());
    }
}
