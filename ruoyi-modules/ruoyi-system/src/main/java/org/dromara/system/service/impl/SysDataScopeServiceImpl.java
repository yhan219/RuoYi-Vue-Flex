package org.dromara.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.StreamUtils;
import org.dromara.common.mybatis.helper.DataBaseHelper;
import org.dromara.system.domain.SysDept;
import org.dromara.system.domain.SysRoleDept;
import org.dromara.system.mapper.SysDeptMapper;
import org.dromara.system.mapper.SysRoleDeptMapper;
import org.dromara.system.service.ISysDataScopeService;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.dromara.system.domain.table.SysDeptTableDef.SYS_DEPT;
import static org.dromara.system.domain.table.SysRoleDeptTableDef.SYS_ROLE_DEPT;

/**
 * 数据权限 实现
 * <p>
 * 注意: 此Service内不允许调用标注`数据权限`注解的方法
 * 例如: deptMapper.selectList 此 selectList 方法标注了`数据权限`注解 会出现循环解析的问题
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Service("sdss")
public class SysDataScopeServiceImpl implements ISysDataScopeService {

    private final SysRoleDeptMapper roleDeptMapper;
    private final SysDeptMapper deptMapper;

    @Override
    public String getRoleCustom(Long roleId) {
        List<SysRoleDept> list = roleDeptMapper.selectListByQuery(
            QueryWrapper.create().from(SYS_ROLE_DEPT).select(SYS_ROLE_DEPT.DEPT_ID)
                .where(SYS_ROLE_DEPT.ROLE_ID.eq(roleId)));
        if (CollUtil.isNotEmpty(list)) {
            return StreamUtils.join(list, rd -> Convert.toStr(rd.getDeptId()));
        }
        return null;
    }

    @Override
    public String getDeptAndChild(Long deptId) {


        List<SysDept> deptList = deptMapper.selectListByQuery(QueryWrapper.create().from(SYS_DEPT).select(SYS_DEPT.DEPT_ID).where(DataBaseHelper.findInSet(deptId, "ancestors")));
        List<Long> ids = StreamUtils.toList(deptList, SysDept::getDeptId);
        ids.add(deptId);
        if (CollUtil.isNotEmpty(ids)) {
            return StreamUtils.join(ids, Convert::toStr);
        }
        return null;
    }

}
