package org.dromara.workflow.domain;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;

/**
 * 节点配置对象 wf_node_config
 *
 * @author may
 * @date 2024-03-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("wf_node_config")
public class WfNodeConfig extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @Id
    private Long id;

    /**
     * 表单id
     */
    private Long formId;

    /**
     * 表单类型
     */
    private String formType;

    /**
     * 节点名称
     */
    private String nodeName;

    /**
     * 节点id
     */
    private String nodeId;

    /**
     * 流程定义id
     */
    private String definitionId;

    /**
     * 是否为申请人节点 （0是 1否）
     */
    private String applyUserTask;


}
