package org.dromara.system.service.impl;

import cn.hutool.crypto.SecureUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.system.domain.SysClient;
import org.dromara.system.domain.bo.SysClientBo;
import org.dromara.system.domain.vo.SysClientVo;
import org.dromara.system.mapper.SysClientMapper;
import org.dromara.system.service.ISysClientService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static org.dromara.system.domain.table.SysClientTableDef.SYS_CLIENT;

/**
 * 客户端管理Service业务层处理
 *
 * @author Michelle.Chung
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SysClientServiceImpl implements ISysClientService {

    private final SysClientMapper baseMapper;

    /**
     * 查询客户端管理
     */
    @Override
    public SysClientVo queryById(Long id) {
        SysClientVo vo = baseMapper.selectOneWithRelationsByIdAs(id, SysClientVo.class);
        vo.setGrantTypeList(List.of(vo.getGrantType().split(",")));
        return vo;
    }


    /**
     * 查询客户端管理
     */
    @Override
    public SysClientVo queryByClientId(String clientId) {
        return baseMapper.selectOneByQueryAs(QueryWrapper.create().from(SYS_CLIENT).where(SYS_CLIENT.CLIENT_ID.eq(clientId)), SysClientVo.class);
    }

    /**
     * 查询客户端管理列表
     */
    @Override
    public TableDataInfo<SysClientVo> queryPageList(SysClientBo bo, PageQuery pageQuery) {
        QueryWrapper lqw = buildQueryWrapper(bo);
        Page<SysClientVo> result = baseMapper.paginateAs(pageQuery, lqw, SysClientVo.class);
        result.getRecords().forEach(r -> r.setGrantTypeList(List.of(r.getGrantType().split(","))));
        return TableDataInfo.build(result);
    }

    /**
     * 查询客户端管理列表
     */
    @Override
    public List<SysClientVo> queryList(SysClientBo bo) {
        QueryWrapper lqw = buildQueryWrapper(bo);
        return baseMapper.selectListByQueryAs(lqw, SysClientVo.class);
    }

    private QueryWrapper buildQueryWrapper(SysClientBo bo) {
       return QueryWrapper.create()
            .from(SYS_CLIENT)
            .where(SYS_CLIENT.CLIENT_ID.eq(bo.getClientId()))
            .and(SYS_CLIENT.CLIENT_KEY.eq(bo.getClientKey()))
            .and(SYS_CLIENT.CLIENT_SECRET.eq(bo.getClientSecret()))
            .and(SYS_CLIENT.STATUS.eq(bo.getStatus()))
            .orderBy(SYS_CLIENT.ID, true);
    }

    /**
     * 新增客户端管理
     */
    @Override
    public Boolean insertByBo(SysClientBo bo) {
        SysClient add = MapstructUtils.convert(bo, SysClient.class);
        validEntityBeforeSave(add);
        add.setGrantType(String.join(",", bo.getGrantTypeList()));
        // 生成clientid
        String clientKey = bo.getClientKey();
        String clientSecret = bo.getClientSecret();
        add.setClientId(SecureUtil.md5(clientKey + clientSecret));
        boolean flag = baseMapper.insert(add,true) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改客户端管理
     */
    @Override
    public Boolean updateByBo(SysClientBo bo) {
        SysClient update = MapstructUtils.convert(bo, SysClient.class);
        validEntityBeforeSave(update);
        update.setGrantType(String.join(",", bo.getGrantTypeList()));
        return baseMapper.update(update) > 0;
    }

    /**
     * 修改状态
     */
    @Override
    public int updateUserStatus(Long id, String status) {
        return UpdateChain.of(SysClient.class)
            .set(SysClient::getStatus, status)
            .from(SysClient.class)
            .where(SysClient::getId).eq(id)
            .update() ? 1 : 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(SysClient entity) {
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除客户端管理
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchByIds(ids) > 0;
    }
}
