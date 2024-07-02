package org.dromara.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.constant.UserConstants;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StreamUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.helper.DataBaseHelper;
import org.dromara.system.domain.SysDept;
import org.dromara.system.domain.SysPost;
import org.dromara.system.domain.bo.SysPostBo;
import org.dromara.system.domain.vo.SysPostVo;
import org.dromara.system.mapper.SysDeptMapper;
import org.dromara.system.mapper.SysPostMapper;
import org.dromara.system.mapper.SysUserPostMapper;
import org.dromara.system.service.ISysPostService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.dromara.system.domain.table.SysPostTableDef.SYS_POST;

/**
 * 岗位信息 服务层处理
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Service
public class SysPostServiceImpl implements ISysPostService {

    private final SysPostMapper baseMapper;
    private final SysDeptMapper deptMapper;
    private final SysUserPostMapper userPostMapper;

    @Override
    public TableDataInfo<SysPostVo> selectPagePostList(SysPostBo post, PageQuery pageQuery) {
        QueryWrapper lqw = buildQueryWrapper(post);
        Page<SysPostVo> page = baseMapper.paginateAs(pageQuery, lqw, SysPostVo.class);
        return TableDataInfo.build(page);
    }

    /**
     * 查询岗位信息集合
     *
     * @param post 岗位信息
     * @return 岗位信息集合
     */
    @Override
    public List<SysPostVo> selectPostList(SysPostBo post) {
        QueryWrapper lqw = buildQueryWrapper(post);
        return baseMapper.selectListByQueryAs(lqw, SysPostVo.class);
    }

    /**
     * 根据查询条件构建查询包装器
     *
     * @param bo 查询条件对象
     * @return 构建好的查询包装器
     */
    private QueryWrapper buildQueryWrapper(SysPostBo bo) {
        QueryWrapper wrapper = QueryWrapper.create();
        wrapper.like(SysPost::getPostCode, bo.getPostCode())
            .like(SysPost::getPostCategory, bo.getPostCategory())
            .like(SysPost::getPostName, bo.getPostName())
            .eq(SysPost::getStatus, bo.getStatus())
            .orderBy(SysPost::getPostSort);
        if (ObjectUtil.isNotNull(bo.getDeptId())) {
            // 优先单部门搜索
            wrapper.eq(SysPost::getDeptId, bo.getDeptId());
        } else if (ObjectUtil.isNotNull(bo.getBelongDeptId())) {
            // 部门树搜索
            wrapper.and(x -> {
                List<Long> deptIds = new ArrayList<>(deptMapper.selectListByQueryAs(QueryWrapper.create()
                    .select(SysDept::getDeptId)
                    .where(DataBaseHelper.findInSet(bo.getBelongDeptId(), "ancestors")), Long.class));
                deptIds.add(bo.getBelongDeptId());
                x.in(SysPost::getDeptId, deptIds);
            });
        }

        return wrapper;
    }

    /**
     * 查询所有岗位
     *
     * @return 岗位列表
     */
    @Override
    public List<SysPostVo> selectPostAll() {
        return baseMapper.selectListByQueryAs(new QueryWrapper().from(SysPost.class), SysPostVo.class);
    }

    /**
     * 通过岗位ID查询岗位信息
     *
     * @param postId 岗位ID
     * @return 角色对象信息
     */
    @Override
    public SysPostVo selectPostById(Long postId) {
        return baseMapper.selectOneWithRelationsByIdAs(postId, SysPostVo.class);
    }

    /**
     * 根据用户ID获取岗位选择框列表
     *
     * @param userId 用户ID
     * @return 选中岗位ID列表
     */
    @Override
    public List<Long> selectPostListByUserId(Long userId) {
        List<SysPostVo> list = baseMapper.selectPostsByUserId(userId);
        return StreamUtils.toList(list, SysPostVo::getPostId);
    }

    /**
     * 通过岗位ID串查询岗位
     *
     * @param postIds 岗位id串
     * @return 岗位列表信息
     */
    @Override
    public List<SysPostVo> selectPostByIds(List<Long> postIds) {
        return baseMapper.selectListByQueryAs(QueryWrapper.create()
            .select(SysPost::getPostId, SysPost::getPostName, SysPost::getPostCode)
            .eq(SysPost::getStatus, UserConstants.POST_NORMAL)
            .in(SysPost::getPostId, postIds, CollectionUtil.isNotEmpty(postIds)), SysPostVo.class);
    }

    /**
     * 校验岗位名称是否唯一
     *
     * @param post 岗位信息
     * @return 结果
     */
    @Override
    public boolean checkPostNameUnique(SysPostBo post) {
        boolean exist = baseMapper.selectCountByQuery(
            QueryWrapper.create().from(SYS_POST).where(SYS_POST.POST_NAME.eq(post.getPostName()))
                .and(SYS_POST.POST_ID.ne(post.getPostId()))) > 0;
        return !exist;
    }

    /**
     * 校验岗位编码是否唯一
     *
     * @param post 岗位信息
     * @return 结果
     */
    @Override
    public boolean checkPostCodeUnique(SysPostBo post) {
        boolean exist = baseMapper.selectCountByQuery(
            QueryWrapper.create().from(SYS_POST).where(SYS_POST.POST_CODE.eq(post.getPostCode()))
                .and(SYS_POST.POST_ID.ne(post.getPostId()))) > 0;
        return !exist;
    }

    /**
     * 通过岗位ID查询岗位使用数量
     *
     * @param postId 岗位ID
     * @return 结果
     */
    @Override
    public long countUserPostById(Long postId) {
        return userPostMapper.selectCountByQuery(QueryWrapper.create().from(SYS_POST).where(SYS_POST.POST_ID.eq(postId)));
    }

    /**
     * 删除岗位信息
     *
     * @param postId 岗位ID
     * @return 结果
     */
    @Override
    public int deletePostById(Long postId) {
        return baseMapper.deleteById(postId);
    }

    /**
     * 批量删除岗位信息
     *
     * @param postIds 需要删除的岗位ID
     * @return 结果
     */
    @Override
    public int deletePostByIds(Long[] postIds) {
        for (Long postId : postIds) {
            SysPost post = baseMapper.selectOneById(postId);
            if (countUserPostById(postId) > 0) {
                throw new ServiceException(String.format("%1$s已分配，不能删除!", post.getPostName()));
            }
        }
        return baseMapper.deleteBatchByIds(Arrays.asList(postIds));
    }

    /**
     * 新增保存岗位信息
     *
     * @param bo 岗位信息
     * @return 结果
     */
    @Override
    public int insertPost(SysPostBo bo) {
        SysPost post = MapstructUtils.convert(bo, SysPost.class);
        return baseMapper.insert(post, true);
    }

    /**
     * 修改保存岗位信息
     *
     * @param bo 岗位信息
     * @return 结果
     */
    @Override
    public int updatePost(SysPostBo bo) {
        SysPost post = MapstructUtils.convert(bo, SysPost.class);
        return baseMapper.update(post);
    }
}
