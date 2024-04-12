package org.dromara.demo.domain;

import com.mybatisflex.annotation.Table;
import lombok.Data;

@Table("t_order")
@Data
public class ShardingOrder {


    private Long orderId;

    private Long userId;

    private int totalMoney;
}
