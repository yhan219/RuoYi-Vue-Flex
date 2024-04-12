package org.dromara.demo.service.impl;

import cn.hutool.core.bean.BeanUtil;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.demo.domain.TestTree;
import org.dromara.demo.domain.bo.TestTreeBo;
import org.dromara.demo.domain.vo.TestTreeVo;
import org.dromara.demo.mapper.TestTreeMapper;
import org.dromara.demo.service.ITestTreeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 测试树表Service业务层处理
 *
 * @author Lion Li
 * @date 2021-07-26
 */
// @DS("slave") // 切换从库查询
@RequiredArgsConstructor
@Service
public class TestTreeServiceImpl implements ITestTreeService {

    private final TestTreeMapper baseMapper;

    @Override
    public TestTreeVo queryById(Long id) {
        // return baseMapper.selectVoById(id);
        return null;
    }

    // @DS("slave") // 切换从库查询
    @Override
    public List<TestTreeVo> queryList(TestTreeBo bo) {
        // LambdaQueryWrapper<TestTree> lqw = buildQueryWrapper(bo);
        // return baseMapper.selectVoList(lqw);
        return null;

    }

    // private LambdaQueryWrapper<TestTree> buildQueryWrapper(TestTreeBo bo) {
    //     Map<String, Object> params = bo.getParams();
    //     LambdaQueryWrapper<TestTree> lqw = Wrappers.lambdaQuery();
    //     lqw.like(StringUtils.isNotBlank(bo.getTreeName()), TestTree::getTreeName, bo.getTreeName());
    //     lqw.between(params.get("beginCreateTime") != null && params.get("endCreateTime") != null,
    //         TestTree::getCreateTime, params.get("beginCreateTime"), params.get("endCreateTime"));
    //     lqw.orderByAsc(TestTree::getId);
    //     return lqw;
    // }

    @Override
    public Boolean insertByBo(TestTreeBo bo) {
        TestTree add = BeanUtil.toBean(bo, TestTree.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    public Boolean updateByBo(TestTreeBo bo) {
        TestTree update = BeanUtil.toBean(bo, TestTree.class);
        validEntityBeforeSave(update);
        return baseMapper.update(update) > 0;
    }

    /**
     * 保存前的数据校验
     *
     * @param entity 实体类数据
     */
    private void validEntityBeforeSave(TestTree entity) {
        //TODO 做一些数据校验,如唯一约束
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchByIds(ids) > 0;
    }
}
