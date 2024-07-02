package org.dromara.workflow.domain;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 流程历史任务对象 act_hi_taskinst
 *
 * @author may
 * @date 2024-03-02
 */
@Data
@Table("ACT_HI_TASKINST")
public class ActHiTaskinst implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    @Id
    @Column(value = "ID_")
    private String id;

    /**
     * 版本
     */
    @Column(value = "REV_")
    private Long rev;

    /**
     * 流程定义id
     */
    @Column(value = "PROC_DEF_ID_")
    private String procDefId;

    /**
     *
     */
    @Column(value = "TASK_DEF_ID_")
    private String taskDefId;

    /**
     * 任务节点id
     */
    @Column(value = "TASK_DEF_KEY_")
    private String taskDefKey;

    /**
     * 流程实例id
     */
    @Column(value = "PROC_INST_ID_")
    private String procInstId;

    /**
     * 流程执行id
     */
    @Column(value = "EXECUTION_ID")
    private String executionId;

    /**
     *
     */
    @Column(value = "SCOPE_ID_")
    private String scopeId;

    /**
     *
     */
    @Column(value = "SUB_SCOPE_ID_")
    private String subScopeId;

    /**
     * 先用当前字段标识抄送类型
     */
    @Column(value = "SCOPE_TYPE_")
    private String scopeType;

    /**
     *
     */
    @Column(value = "SCOPE_DEFINITION_ID_")
    private String scopeDefinitionId;

    /**
     *
     */
    @Column(value = "PROPAGATED_STAGE_INST_ID_")
    private String propagatedStageInstId;

    /**
     * 任务名称
     */
    @Column(value = "NAME_")
    private String name;

    /**
     * 父级id
     */
    @Column(value = "PARENT_TASK_ID_")
    private String parentTaskId;

    /**
     * 描述
     */
    @Column(value = "DESCRIPTION_")
    private String description;

    /**
     * 办理人
     */
    @Column(value = "OWNER_")
    private String owner;

    /**
     * 办理人
     */
    @Column(value = "ASSIGNEE_")
    private String assignee;

    /**
     * 开始事件
     */
    @Column(value = "START_TIME_")
    private Date startTime;

    /**
     * 认领时间
     */
    @Column(value = "CLAIM_TIME_")
    private Date claimTime;

    /**
     * 结束时间
     */
    @Column(value = "END_TIME_")
    private Date endTime;

    /**
     * 持续时间
     */
    @Column(value = "DURATION_")
    private Long duration;

    /**
     * 删除原因
     */
    @Column(value = "DELETE_REASON_")
    private String deleteReason;

    /**
     * 优先级
     */
    @Column(value = "PRIORITY_")
    private Long priority;

    /**
     * 到期时间
     */
    @Column(value = "DUE_DATE_")
    private Date dueDate;

    /**
     *
     */
    @Column(value = "FORM_KEY_")
    private String formKey;

    /**
     * 分类
     */
    @Column(value = "CATEGORY_")
    private String category;

    /**
     * 最后修改时间
     */
    @Column(value = "LAST_UPDATED_TIME_")
    private Date lastUpdatedTime;

    /**
     * 租户id
     */
    @Column(value = "TENANT_ID_")
    private String tenantId;


}
