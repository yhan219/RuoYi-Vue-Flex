package org.dromara.workflow.mapper;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryTable;
import com.mybatisflex.core.query.QueryWrapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.workflow.domain.bo.TaskBo;
import org.dromara.workflow.domain.vo.TaskVo;

import static org.dromara.workflow.domain.table.ActHiProcinstTableDef.ACT_HI_PROCINST;


/**
 * 任务信息Mapper接口
 *
 * @author may
 * @date 2024-03-02
 */
public interface ActTaskMapper extends BaseMapperPlus<TaskVo> {

    default Page<TaskVo> getTaskFinishByPage(PageQuery pageQuery, String userId, TaskBo taskBo) {
        QueryWrapper queryWrapper = QueryWrapper.create().select("HTI.*")
            .select(ACT_HI_PROCINST.BUSINESS_STATUS, ACT_HI_PROCINST.BUSINESS_KEY)
            .select(new QueryColumn("ARP", "NAME_").as("processDefinitionName"))
            .select(new QueryColumn("ARP", "KEY_").as("processDefinitionKey"))
            .select(new QueryColumn("ARP", "VERSION_").as("processDefinitionVersion"))
            .from(new QueryTable("ACT_HI_TASKINST").as("HTI"))
            .innerJoin(ACT_HI_PROCINST.as("AHP")).on(new QueryColumn("HTI", "PROC_INST_ID_").eq(ACT_HI_PROCINST.PROC_INST_ID))
            .innerJoin(new QueryTable("ACT_RE_PROCDEF").as("ARP")).on(new QueryColumn("ARP", "ID_").eq(new QueryColumn("HTI", "PROC_DEF_ID_")))
            .where(new QueryColumn("HTI", "PARENT_TASK_ID_").isNull())
            .and(new QueryColumn("HTI", "END_TIME_").isNotNull())
            .and(new QueryColumn("HTI", "NAME").like(taskBo.getName()))
            .and(new QueryColumn("ARP", "NAME_").like(taskBo.getProcessDefinitionName()))
            .and(new QueryColumn("ARP", "KEY_").like(taskBo.getProcessDefinitionKey()))
            .and(new QueryColumn("HTI", "ASSIGNEE_").eq(userId))
            .orderBy(new QueryColumn("HTI", "START_TIME_").desc());
        return this.paginateAs(pageQuery, queryWrapper, TaskVo.class);
    }


}
