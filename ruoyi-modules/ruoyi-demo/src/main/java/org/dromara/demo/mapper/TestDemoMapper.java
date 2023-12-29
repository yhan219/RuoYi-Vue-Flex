package org.dromara.demo.mapper;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.dromara.common.mybatis.annotation.DataColumn;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.demo.domain.TestDemo;
import org.dromara.demo.domain.vo.TestDemoVo;

/**
 * 测试单表Mapper接口
 *
 * @author Lion Li
 * @date 2021-07-26
 */
public interface TestDemoMapper extends BaseMapperPlus<TestDemo> {

    default Page<TestDemoVo> customPageList(PageQuery pageQuery, QueryWrapper queryWrapper){
        return this.paginateAs(pageQuery, queryWrapper, TestDemoVo.class, DataColumn.of("deptName", "dept_id"), DataColumn.of("userName", "user_id"));
    }

}
