package org.dromara.common.mybatis.core.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.annotation.DataPermission;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.List;

/**
 * 自定义 Mapper 接口, 实现 自定义扩展
 *
 * @param <T> table 泛型
 * @param <V> vo 泛型
 * @author Lion Li
 * @since 2021-05-13
 */
public interface BaseMapperPlus<T> extends BaseMapper<T> {


    Log log = LogFactory.getLog(BaseMapperPlus.class);


    default <V>  V selectOneByQueryAs(QueryWrapper queryWrapper, Class<V> asType, DataPermission dataPermission) {
        dataPermission.handler(queryWrapper);
        return this.selectOneByQueryAs(queryWrapper, asType);
    }

    default <V> V selectOneWithRelationsByQueryAs(QueryWrapper queryWrapper, Class<V> asType, DataPermission dataPermission) {
        dataPermission.handler(queryWrapper);
        return this.selectOneWithRelationsByQueryAs(queryWrapper, asType);
    }



    default <V> Page<V> paginateAs(Page<V> page, QueryWrapper queryWrapper, Class<V> asType, DataPermission dataPermission) {
        dataPermission.handler(queryWrapper);
        return this.paginateAs(page, queryWrapper, asType);
    }

    default <V> Page<V> paginateAs(PageQuery pageQuery, QueryWrapper queryWrapper, Class<V> asType, DataPermission dataPermission) {
        dataPermission.handler(queryWrapper);
        return this.paginateAs(pageQuery.build(), queryWrapper, asType);
    }

    default <V> Page<V> paginateWithRelationsAs(PageQuery pageQuery, QueryWrapper queryWrapper, Class<V> asType, DataPermission dataPermission) {
        dataPermission.handler(queryWrapper);
        return this.paginateWithRelationsAs(pageQuery.build(), queryWrapper, asType);
    }



    default <V> List<V> selectListByQueryAs(QueryWrapper queryWrapper, Class<V> asType, DataPermission dataPermission) {
        dataPermission.handler(queryWrapper);
        return this.selectListByQueryAs(queryWrapper, asType);
    }

    default <V> List<V> selectListWithRelationsByQueryAs(QueryWrapper queryWrapper, Class<V> asType, DataPermission dataPermission) {
        dataPermission.handler(queryWrapper);
        return this.selectListWithRelationsByQueryAs(queryWrapper, asType);
    }


    default <V> List<V> selectObjectListByQueryAs(QueryWrapper queryWrapper, Class<V> asType, DataPermission dataPermission) {
        dataPermission.handler(queryWrapper);
        return this.selectObjectListByQueryAs(queryWrapper, asType);
    }


    default <V> Page<V> paginateAs(PageQuery pageQuery, QueryWrapper queryWrapper, Class<V> asType) {
        queryWrapper.orderBy(pageQuery.buildOrderBy());
        return this.paginateAs(pageQuery.build(), queryWrapper, asType);
    }

    default Page<T> paginate(PageQuery pageQuery, QueryWrapper queryWrapper) {
        queryWrapper.orderBy(pageQuery.buildOrderBy());
        return this.paginate(pageQuery.build(), queryWrapper);
    }


    default boolean update(UpdateChain<T> updateChain, DataPermission dataPermission) {
        String sql = dataPermission.toSQL(false);
        if (StringUtils.isNotBlank(sql)) {
            updateChain.and(sql);
        }
        return updateChain.update();
    }

    default int update(T entity, DataPermission dataPermission) {
        String sql = dataPermission.toSQL(false);
        if (StringUtils.isBlank(sql)) {
            return this.update(entity);
        }
        QueryWrapper queryWrapper = QueryWrapper.create().where(sql);
        return updateByQuery(entity, queryWrapper);
    }


}
