package org.dromara.demo.domain;

import com.mybatisflex.annotation.Table;
import lombok.Data;

@Table("t_order_item")
@Data
public class ShardingOrderItem {

    private Long orderItemId;

    private Long orderId;

    private Long userId;

    private int totalMoney;
}
