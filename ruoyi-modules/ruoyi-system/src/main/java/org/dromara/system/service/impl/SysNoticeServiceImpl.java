package org.dromara.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.system.domain.SysNotice;
import org.dromara.system.domain.SysUser;
import org.dromara.system.domain.bo.SysNoticeBo;
import org.dromara.system.domain.vo.SysNoticeVo;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.mapper.SysNoticeMapper;
import org.dromara.system.mapper.SysUserMapper;
import org.dromara.system.service.ISysNoticeService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import static org.dromara.common.core.constant.UserConstants.SYS_USER;
import static org.dromara.system.domain.table.SysNoticeTableDef.SYS_NOTICE;

/**
 * 公告 服务层实现
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Service
public class SysNoticeServiceImpl implements ISysNoticeService {

    private final SysNoticeMapper baseMapper;
    private final SysUserMapper userMapper;

    @Override
    public TableDataInfo<SysNoticeVo> selectPageNoticeList(SysNoticeBo notice, PageQuery pageQuery) {
        QueryWrapper lqw = buildQueryWrapper(notice);
        Page<SysNoticeVo> page = baseMapper.paginateAs(pageQuery, lqw, SysNoticeVo.class);
        return TableDataInfo.build(page);
    }

    /**
     * 查询公告信息
     *
     * @param noticeId 公告ID
     * @return 公告信息
     */
    @Override
    public SysNoticeVo selectNoticeById(Long noticeId) {
        return baseMapper.selectOneWithRelationsByIdAs(noticeId, SysNoticeVo.class);
    }

    /**
     * 查询公告列表
     *
     * @param notice 公告信息
     * @return 公告集合
     */
    @Override
    public List<SysNoticeVo> selectNoticeList(SysNoticeBo notice) {
        QueryWrapper lqw = buildQueryWrapper(notice);
        return baseMapper.selectListByQueryAs(lqw, SysNoticeVo.class);
    }

    private QueryWrapper buildQueryWrapper(SysNoticeBo bo) {
        QueryWrapper queryWrapper = QueryWrapper.create().from(SYS_NOTICE)
            .where(SYS_NOTICE.NOTICE_TITLE.like(bo.getNoticeTitle()))
            .and(SYS_NOTICE.NOTICE_TYPE.eq(bo.getNoticeType()));
        if (StringUtils.isNotBlank(bo.getCreateByName())) {
            SysUserVo sysUser = userMapper.selectOneByQueryAs(QueryWrapper.create().from(SYS_USER).eq(SysUser::getUserName, bo.getCreateByName()),SysUserVo.class);
            queryWrapper.eq(SysNotice::getCreateBy, ObjectUtil.isNotNull(sysUser) ? sysUser.getUserId() : null);
        }
        queryWrapper.orderBy(SYS_NOTICE.NOTICE_ID, true);
        return queryWrapper;
    }

    /**
     * 新增公告
     *
     * @param bo 公告信息
     * @return 结果
     */
    @Override
    public int insertNotice(SysNoticeBo bo) {
        SysNotice notice = MapstructUtils.convert(bo, SysNotice.class);
        return baseMapper.insert(notice,true);
    }

    /**
     * 修改公告
     *
     * @param bo 公告信息
     * @return 结果
     */
    @Override
    public int updateNotice(SysNoticeBo bo) {
        SysNotice notice = MapstructUtils.convert(bo, SysNotice.class);
        return baseMapper.update(notice);
    }

    /**
     * 删除公告对象
     *
     * @param noticeId 公告ID
     * @return 结果
     */
    @Override
    public int deleteNoticeById(Long noticeId) {
        return baseMapper.deleteById(noticeId);
    }

    /**
     * 批量删除公告信息
     *
     * @param noticeIds 需要删除的公告ID
     * @return 结果
     */
    @Override
    public int deleteNoticeByIds(Long[] noticeIds) {
        return baseMapper.deleteBatchByIds(Arrays.asList(noticeIds));
    }
}
