package org.dromara.system.mapper;

import com.mybatisflex.core.query.QueryWrapper;
import org.apache.ibatis.annotations.Param;
import org.dromara.common.mybatis.annotation.DataColumn;
import org.dromara.common.mybatis.annotation.DataPermission;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.system.domain.SysDept;
import org.dromara.system.domain.vo.SysDeptVo;

import java.util.List;

import static org.dromara.system.domain.table.SysDeptTableDef.SYS_DEPT;

/**
 * 部门管理 数据层
 *
 * @author Lion Li
 */
public interface SysDeptMapper extends BaseMapperPlus<SysDept> {

    /**
     * 查询部门管理数据
     *
     * @param queryWrapper 查询条件
     * @return 部门信息集合
     */

    default List<SysDeptVo> selectDeptList(QueryWrapper queryWrapper) {
        return selectListByQueryAs(queryWrapper, SysDeptVo.class, DataPermission.of(
            DataColumn.of("deptName", "dept_id")
        ));
    }

    default SysDeptVo selectDeptById(Long deptId){
        QueryWrapper queryWrapper = QueryWrapper.create().where(SYS_DEPT.DEPT_ID.eq(deptId));
        return selectOneByQueryAs(queryWrapper, SysDeptVo.class, DataPermission.of(
            DataColumn.of("deptName", "dept_id")
        ));
    }


    /**
     * 根据角色ID查询部门树信息
     *
     * @param roleId            角色ID
     * @param deptCheckStrictly 部门树选择项是否关联显示
     * @return 选中部门列表
     */
    List<Long> selectDeptListByRoleId(@Param("roleId") Long roleId, @Param("deptCheckStrictly") boolean deptCheckStrictly);

}
