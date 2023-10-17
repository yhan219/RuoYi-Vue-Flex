package org.dromara.demo.domain;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;

/**
 * 测试单表对象 test_demo
 *
 * @author Lion Li
 * @date 2021-07-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("test_demo")
public class TestDemo extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @Id
    private Long id;

    /**
     * 部门id
     */
    private Long deptId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 排序号
     */
    private Integer orderNum;

    /**
     * key键
     */
    private String testKey;

    /**
     * 值
     */
    private String value;

    /**
     * 版本
     */
    @Column(version = true)
    private Long version;

    /**
     * 删除标志
     */
    private Long delFlag;

}
