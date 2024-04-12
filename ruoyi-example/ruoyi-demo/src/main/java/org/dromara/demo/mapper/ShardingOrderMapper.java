package org.dromara.demo.mapper;

import com.mybatisflex.annotation.UseDataSource;
import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.demo.domain.ShardingOrder;


@Mapper
@UseDataSource("sharding")
public interface ShardingOrderMapper extends BaseMapperPlus<ShardingOrder> {


}
