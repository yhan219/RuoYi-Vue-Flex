package org.dromara.demo.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.demo.domain.TestDemo;
import org.dromara.demo.domain.bo.TestDemoBo;
import org.dromara.demo.domain.vo.TestDemoVo;
import org.dromara.demo.mapper.TestDemoMapper;
import org.dromara.demo.service.ITestDemoService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 测试单表Service业务层处理
 *
 * @author Lion Li
 * @date 2021-07-26
 */
@RequiredArgsConstructor
@Service
public class TestDemoServiceImpl implements ITestDemoService {

    private final TestDemoMapper baseMapper;

    @Override
    public TestDemoVo queryById(Long id) {
        return baseMapper.selectOneWithRelationsByIdAs(id,TestDemoVo.class);
    }

    @Override
    public TableDataInfo<TestDemoVo> queryPageList(TestDemoBo bo, PageQuery pageQuery) {
        QueryWrapper lqw = buildQueryWrapper(bo);
        Page<TestDemoVo> result = baseMapper.paginateAs(pageQuery.build(), lqw,TestDemoVo.class);
        return TableDataInfo.build(result);
    }

    /**
     * 自定义分页查询
     */
    @Override
    public TableDataInfo<TestDemoVo> customPageList(TestDemoBo bo, PageQuery pageQuery) {
        QueryWrapper lqw = buildQueryWrapper(bo);
        Page<TestDemoVo> result = baseMapper.customPageList(pageQuery, lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<TestDemoVo> queryList(TestDemoBo bo) {
        return baseMapper.selectListByQueryAs(buildQueryWrapper(bo),TestDemoVo.class);
    }

    private QueryWrapper buildQueryWrapper(TestDemoBo bo) {
        Map<String, Object> params = bo.getParams();
        QueryWrapper lqw = QueryWrapper.create(TestDemo.class);
//        todo
//        lqw.like(StringUtils.isNotBlank(bo.getTestKey()), TestDemo::getTestKey, bo.getTestKey());
//        lqw.eq(StringUtils.isNotBlank(bo.getValue()), TestDemo::getValue, bo.getValue());
//        lqw.between(params.get("beginCreateTime") != null && params.get("endCreateTime") != null,
//            TestDemo::getCreateTime, params.get("beginCreateTime"), params.get("endCreateTime"));
//        lqw.orderByAsc(TestDemo::getId);
        return lqw;
    }

    @Override
    public Boolean insertByBo(TestDemoBo bo) {
        TestDemo add = MapstructUtils.convert(bo, TestDemo.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add,true) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    public Boolean updateByBo(TestDemoBo bo) {
        TestDemo update = MapstructUtils.convert(bo, TestDemo.class);
        validEntityBeforeSave(update);
        return baseMapper.update(update) > 0;
    }

    /**
     * 保存前的数据校验
     *
     * @param entity 实体类数据
     */
    private void validEntityBeforeSave(TestDemo entity) {
        //TODO 做一些数据校验,如唯一约束
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchByIds(ids) > 0;
    }

    @Override
    public Boolean saveBatch(List<TestDemo> list) {
        return baseMapper.insertBatch(list) > 0;
    }
}
