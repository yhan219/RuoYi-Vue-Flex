package org.dromara.demo.mapper;


import com.mybatisflex.annotation.UseDataSource;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.dromara.demo.domain.ShardingOrderItem;

@Mapper
@UseDataSource("sharding")
public interface ShardingOrderItemMapper extends BaseMapper<ShardingOrderItem> {


}
