package org.dromara.workflow.domain;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 流程实例对象 act_hi_procinst
 *
 * @author may
 * @date 2023-07-22
 */
@Data
@Table("ACT_HI_PROCINST")
public class ActHiProcinst implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    @Id
    @Column(value = "ID_")
    private String id;

    /**
     *
     */
    @Column(value = "REV_")
    private Long rev;

    /**
     *
     */
    @Column(value = "PROC_INST_ID_")
    private String procInstId;

    /**
     *
     */
    @Column(value = "BUSINESS_KEY_")
    private String businessKey;

    /**
     *
     */
    @Column(value = "PROC_DEF_ID_")
    private String procDefId;

    /**
     *
     */
    @Column(value = "START_TIME_")
    private Date startTime;

    /**
     *
     */
    @Column(value = "END_TIME_")
    private Date endTime;

    /**
     *
     */
    @Column(value = "DURATION_")
    private Long duration;

    /**
     *
     */
    @Column(value = "START_USER_ID_")
    private String startUserId;

    /**
     *
     */
    @Column(value = "START_ACT_ID_")
    private String startActId;

    /**
     *
     */
    @Column(value = "END_ACT_ID_")
    private String endActId;

    /**
     *
     */
    @Column(value = "SUPER_PROCESS_INSTANCE_ID_")
    private String superProcessInstanceId;

    /**
     *
     */
    @Column(value = "DELETE_REASON_")
    private String deleteReason;

    /**
     *
     */
    @Column(value = "TENANT_ID_")
    private String tenantId;

    /**
     *
     */
    @Column(value = "NAME_")
    private String name;

    /**
     *
     */
    @Column(value = "CALLBACK_ID_")
    private String callbackId;

    /**
     *
     */
    @Column(value = "CALLBACK_TYPE_")
    private String callbackType;

    /**
     *
     */
    @Column(value = "REFERENCE_ID_")
    private String referenceId;

    /**
     *
     */
    @Column(value = "REFERENCE_TYPE_")
    private String referenceType;

    /**
     *
     */
    @Column(value = "PROPAGATED_STAGE_INST_ID_")
    private String propagatedStageInstId;

    /**
     *
     */
    @Column(value = "BUSINESS_STATUS_")
    private String businessStatus;


}
