package org.dromara.demo.mapper;

import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.demo.domain.TestDemo;

/**
 * 测试单表Mapper接口
 *
 * @author Lion Li
 * @date 2021-07-26
 */
public interface TestDemoMapper extends BaseMapperPlus<TestDemo> {

    // @DataPermission({
    //     @DataColumn(key = "deptName", value = "dept_id"),
    //     @DataColumn(key = "userName", value = "user_id")
    // })
    // Page<TestDemoVo> customPageList(@Param("page") Page<TestDemo> page, @Param("ew") Wrapper<TestDemo> wrapper);
    //
    // @Override
    // @DataPermission({
    //     @DataColumn(key = "deptName", value = "dept_id"),
    //     @DataColumn(key = "userName", value = "user_id")
    // })
    // List<TestDemo> selectList(IPage<TestDemo> page, @Param(Constants.WRAPPER) Wrapper<TestDemo> queryWrapper);
    //
    //
    // @Override
    // @DataPermission({
    //     @DataColumn(key = "deptName", value = "dept_id"),
    //     @DataColumn(key = "userName", value = "user_id")
    // })
    // List<TestDemo> selectList(@Param(Constants.WRAPPER) Wrapper<TestDemo> queryWrapper);
    //
    // @Override
    // @DataPermission({
    //     @DataColumn(key = "deptName", value = "dept_id"),
    //     @DataColumn(key = "userName", value = "user_id")
    // })
    // int updateById(@Param(Constants.ENTITY) TestDemo entity);
    //
    // @Override
    // @DataPermission({
    //     @DataColumn(key = "deptName", value = "dept_id"),
    //     @DataColumn(key = "userName", value = "user_id")
    // })
    // int deleteBatchIds(@Param(Constants.COLL) Collection<?> idList);
}
