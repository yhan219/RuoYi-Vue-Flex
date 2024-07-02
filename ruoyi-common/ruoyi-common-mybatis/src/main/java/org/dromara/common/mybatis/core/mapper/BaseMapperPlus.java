package org.dromara.common.mybatis.core.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.exception.FlexAssert;
import com.mybatisflex.core.field.FieldQueryBuilder;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.row.Row;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.core.util.MapperUtil;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.annotation.DataColumn;
import org.dromara.common.mybatis.annotation.DataPermission;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 自定义 Mapper 接口, 实现 自定义扩展
 *
 * @param <T> table 泛型
 * @author yhan219
 * @since 2023-12-29
 */
public interface BaseMapperPlus<T> extends BaseMapper<T> {


    Log log = LogFactory.getLog(BaseMapperPlus.class);

    default int deleteByQuery(QueryWrapper queryWrapper, DataColumn... columns) {
        DataPermission dataPermission = DataPermission.of(columns);
        dataPermission.handler(queryWrapper);
        return this.deleteByQuery(queryWrapper);
    }


    default int updateByMap(T entity, Map<String, Object> whereConditions, DataColumn... columns) {
        FlexAssert.notEmpty(whereConditions, "whereConditions");
        return this.updateByQuery(entity, QueryWrapper.create().where(whereConditions), columns);
    }

    default int updateByMap(T entity, boolean ignoreNulls, Map<String, Object> whereConditions, DataColumn... columns) {
        FlexAssert.notEmpty(whereConditions, "whereConditions");
        return this.updateByQuery(entity, ignoreNulls, QueryWrapper.create().where(whereConditions), columns);
    }

    default int updateByCondition(T entity, QueryCondition whereConditions, DataColumn... columns) {
        FlexAssert.notNull(whereConditions, "whereConditions");
        return this.updateByQuery(entity, QueryWrapper.create().where(whereConditions), columns);
    }

    default int updateByCondition(T entity, boolean ignoreNulls, QueryCondition whereConditions, DataColumn... columns) {
        FlexAssert.notNull(whereConditions, "whereConditions");
        return this.updateByQuery(entity, ignoreNulls, QueryWrapper.create().where(whereConditions), columns);
    }


    default int updateByQuery(T t, boolean b, QueryWrapper queryWrapper, DataColumn... columns) {
        DataPermission dataPermission = DataPermission.of(columns);
        dataPermission.handler(queryWrapper);
        return this.updateByQuery(t, b, queryWrapper);
    }

    default int updateByQuery(T entity, QueryWrapper queryWrapper, DataColumn... columns) {
        DataPermission dataPermission = DataPermission.of(columns);
        dataPermission.handler(queryWrapper);
        return this.updateByQuery(entity, queryWrapper);
    }

    default T selectOneByQuery(QueryWrapper queryWrapper, DataColumn... columns) {
        DataPermission dataPermission = DataPermission.of(columns);
        dataPermission.handler(queryWrapper);
        return this.selectOneByQuery(queryWrapper);
    }

    default <R> R selectOneByQueryAs(QueryWrapper queryWrapper, Class<R> asType, DataColumn... columns) {
        DataPermission dataPermission = DataPermission.of(columns);
        dataPermission.handler(queryWrapper);
        return this.selectOneByQueryAs(queryWrapper, asType);
    }

    default T selectOneWithRelationsByMap(Map<String, Object> whereConditions, DataColumn... columns) {
        FlexAssert.notEmpty(whereConditions, "whereConditions");
        return this.selectOneWithRelationsByQuery(QueryWrapper.create().where(whereConditions).limit(1L), columns);
    }

    default T selectOneWithRelationsByCondition(QueryCondition whereConditions, DataColumn... columns) {
        FlexAssert.notNull(whereConditions, "whereConditions");
        return this.selectOneWithRelationsByQuery(QueryWrapper.create().where(whereConditions).limit(1L), columns);
    }

    default T selectOneWithRelationsByQuery(QueryWrapper queryWrapper, DataColumn... columns) {
        DataPermission dataPermission = DataPermission.of(columns);
        dataPermission.handler(queryWrapper);
        return this.selectOneWithRelationsByQuery(queryWrapper);
    }


    default <R> R selectOneWithRelationsByQueryAs(QueryWrapper queryWrapper, Class<R> asType, DataColumn... columns) {
        DataPermission dataPermission = DataPermission.of(columns);
        dataPermission.handler(queryWrapper);
        return this.selectOneWithRelationsByQueryAs(queryWrapper, asType);
    }


    default List<T> selectListByMap(Map<String, Object> whereConditions, DataColumn... columns) {
        FlexAssert.notEmpty(whereConditions, "whereConditions");
        return this.selectListByQuery(QueryWrapper.create().where(whereConditions), columns);
    }

    default List<T> selectListByMap(Map<String, Object> whereConditions, Long count, DataColumn... columns) {
        FlexAssert.notEmpty(whereConditions, "whereConditions");
        return this.selectListByQuery(QueryWrapper.create().where(whereConditions).limit(count), columns);
    }

    default List<T> selectListByCondition(QueryCondition whereConditions, DataColumn... columns) {
        FlexAssert.notNull(whereConditions, "whereConditions");
        return this.selectListByQuery(QueryWrapper.create().where(whereConditions), columns);
    }

    default List<T> selectListByCondition(QueryCondition whereConditions, Long count, DataColumn... columns) {
        FlexAssert.notNull(whereConditions, "whereConditions");
        return this.selectListByQuery(QueryWrapper.create().where(whereConditions).limit(count), columns);
    }

    default List<T> selectListByQuery(QueryWrapper queryWrapper, DataColumn... columns) {
        DataPermission dataPermission = DataPermission.of(columns);
        dataPermission.handler(queryWrapper);
        return this.selectListByQuery(queryWrapper);
    }

    default List<T> selectListByQuery(QueryWrapper queryWrapper, DataPermission dataPermission, Consumer<FieldQueryBuilder<T>>... consumers) {
        dataPermission.handler(queryWrapper);
        return this.selectListByQuery(queryWrapper, consumers);
    }

    default Cursor<T> selectCursorByQuery(QueryWrapper queryWrapper, DataColumn... columns) {
        DataPermission dataPermission = DataPermission.of(columns);
        dataPermission.handler(queryWrapper);
        return this.selectCursorByQuery(queryWrapper);
    }

    default List<Row> selectRowsByQuery(QueryWrapper queryWrapper, DataColumn... columns) {
        DataPermission dataPermission = DataPermission.of(columns);
        dataPermission.handler(queryWrapper);
        return this.selectRowsByQuery(queryWrapper);
    }

    default <R> List<R> selectListByQueryAs(QueryWrapper queryWrapper, Class<R> asType, DataColumn... columns) {
        DataPermission dataPermission = DataPermission.of(columns);
        dataPermission.handler(queryWrapper);
        return this.selectListByQueryAs(queryWrapper, asType);
    }

    default <R> List<R> selectListByQueryAs(QueryWrapper queryWrapper, Class<R> asType, DataPermission dataPermission, Consumer<FieldQueryBuilder<R>>... consumers) {
        dataPermission.handler(queryWrapper);
        return this.selectListByQueryAs(queryWrapper, asType, consumers);
    }

    default List<T> selectListWithRelationsByQuery(QueryWrapper queryWrapper, DataColumn... columns) {
        DataPermission dataPermission = DataPermission.of(columns);
        dataPermission.handler(queryWrapper);
        return this.selectListWithRelationsByQuery(queryWrapper);
    }

    default <R> List<R> selectListWithRelationsByQueryAs(QueryWrapper queryWrapper, Class<R> asType, DataColumn... columns) {
        DataPermission dataPermission = DataPermission.of(columns);
        dataPermission.handler(queryWrapper);
        return this.selectListWithRelationsByQueryAs(queryWrapper, asType);
    }

    default <R> List<R> selectListWithRelationsByQueryAs(QueryWrapper queryWrapper, Class<R> asType, DataPermission dataPermission, Consumer<FieldQueryBuilder<R>>... consumers) {
        dataPermission.handler(queryWrapper);
        return this.selectListWithRelationsByQueryAs(queryWrapper, asType, consumers);
    }

    default List<T> selectAll(DataColumn... columns) {
        DataPermission dataPermission = DataPermission.of(columns);
        QueryWrapper queryWrapper = new QueryWrapper();
        dataPermission.handler(queryWrapper);
        return selectListByQuery(queryWrapper);
    }

    default List<T> selectAllWithRelations(DataColumn... columns) {
        QueryWrapper queryWrapper = new QueryWrapper();
        DataPermission dataPermission = DataPermission.of(columns);
        dataPermission.handler(queryWrapper);
        return MapperUtil.queryRelations(this, selectListByQuery(queryWrapper));
    }

    default Object selectObjectByQuery(QueryWrapper queryWrapper, DataColumn... columns) {
        DataPermission dataPermission = DataPermission.of(columns);
        dataPermission.handler(queryWrapper);
        return this.selectObjectByQuery(queryWrapper);
    }

    default <R> R selectObjectByQueryAs(QueryWrapper queryWrapper, Class<R> asType, DataColumn... columns) {
        DataPermission dataPermission = DataPermission.of(columns);
        dataPermission.handler(queryWrapper);
        return this.selectObjectByQueryAs(queryWrapper, asType);
    }


    default List<Object> selectObjectListByQuery(QueryWrapper queryWrapper, DataColumn... columns) {
        DataPermission dataPermission = DataPermission.of(columns);
        dataPermission.handler(queryWrapper);
        return this.selectObjectListByQuery(queryWrapper);
    }


    default <R> List<R> selectObjectListByQueryAs(QueryWrapper queryWrapper, Class<R> asType, DataColumn... columns) {
        DataPermission dataPermission = DataPermission.of(columns);
        dataPermission.handler(queryWrapper);
        return this.selectObjectListByQueryAs(queryWrapper, asType);
    }

    default long selectCountByQuery(QueryWrapper queryWrapper, DataColumn... columns) {
        DataPermission dataPermission = DataPermission.of(columns);
        dataPermission.handler(queryWrapper);
        return this.selectCountByQuery(queryWrapper);
    }

    default long selectCountByCondition(QueryCondition whereConditions, DataColumn... columns) {
        FlexAssert.notNull(whereConditions, "whereConditions");
        QueryWrapper queryWrapper = QueryWrapper.create().where(whereConditions);
        DataPermission dataPermission = DataPermission.of(columns);
        dataPermission.handler(queryWrapper);
        return this.selectCountByQuery(queryWrapper);
    }

    default Page<T> paginate(Number pageNumber, Number pageSize, QueryWrapper queryWrapper, DataColumn... columns) {
        DataPermission dataPermission = DataPermission.of(columns);
        dataPermission.handler(queryWrapper);
        return this.paginate(pageNumber, pageSize, queryWrapper);
    }

    default Page<T> paginateWithRelations(Number pageNumber, Number pageSize, QueryWrapper queryWrapper, DataColumn... columns) {
        DataPermission dataPermission = DataPermission.of(columns);
        dataPermission.handler(queryWrapper);
        return this.paginateWithRelations(pageNumber, pageSize, queryWrapper);
    }

    default Page<T> paginate(Number pageNumber, Number pageSize, QueryCondition whereConditions, DataColumn... columns) {
        Page<T> page = new Page(pageNumber, pageSize);
        return this.paginate(page, (new QueryWrapper()).where(whereConditions), columns);
    }

    default Page<T> paginateWithRelations(Number pageNumber, Number pageSize, QueryCondition whereConditions, DataColumn... columns) {
        Page<T> page = new Page(pageNumber, pageSize);
        return this.paginateWithRelations(page, (new QueryWrapper()).where(whereConditions), columns);
    }

    default Page<T> paginate(Number pageNumber, Number pageSize, Number totalRow, QueryWrapper queryWrapper, DataColumn... columns) {
        DataPermission dataPermission = DataPermission.of(columns);
        dataPermission.handler(queryWrapper);
        return this.paginate(pageNumber, pageSize, totalRow, queryWrapper);
    }

    default Page<T> paginateWithRelations(Number pageNumber, Number pageSize, Number totalRow, QueryWrapper queryWrapper, DataColumn... columns) {
        DataPermission dataPermission = DataPermission.of(columns);
        dataPermission.handler(queryWrapper);
        return this.paginateWithRelations(pageNumber, pageSize, totalRow, queryWrapper);
    }

    default Page<T> paginate(Number pageNumber, Number pageSize, Number totalRow, QueryCondition whereConditions, DataColumn... columns) {
        FlexAssert.notNull(whereConditions, "whereConditions");
        Page<T> page = new Page<>(pageNumber, pageSize, totalRow);
        return this.paginate(page, (new QueryWrapper()).where(whereConditions), columns);
    }

    default Page<T> paginateWithRelations(Number pageNumber, Number pageSize, Number totalRow, QueryCondition whereConditions, DataColumn... columns) {
        FlexAssert.notNull(whereConditions, "whereConditions");
        Page<T> page = new Page<>(pageNumber, pageSize, totalRow);
        return this.paginateWithRelations(page, (new QueryWrapper()).where(whereConditions), columns);
    }

    default Page<T> paginate(Page<T> page, QueryWrapper queryWrapper, DataColumn... columns) {
        DataPermission dataPermission = DataPermission.of(columns);
        dataPermission.handler(queryWrapper);
        return this.paginate(page, queryWrapper);
    }

    default Page<T> paginate(Page<T> page, QueryWrapper queryWrapper, DataPermission dataPermission, Consumer<FieldQueryBuilder<T>>... consumers) {
        dataPermission.handler(queryWrapper);
        return this.paginate(page, queryWrapper, consumers);
    }

    default Page<T> paginateWithRelations(Page<T> page, QueryWrapper queryWrapper, DataColumn... columns) {
        DataPermission dataPermission = DataPermission.of(columns);
        dataPermission.handler(queryWrapper);
        return this.paginateWithRelations(page, queryWrapper);
    }

    default Page<T> paginateWithRelations(Page<T> page, QueryWrapper queryWrapper, DataPermission dataPermission, Consumer<FieldQueryBuilder<T>>... consumers) {
        dataPermission.handler(queryWrapper);
        return this.paginateWithRelations(page, queryWrapper, consumers);
    }

    default <R> Page<R> paginateAs(Number pageNumber, Number pageSize, QueryWrapper queryWrapper, Class<R> asType, DataColumn... columns) {
        DataPermission dataPermission = DataPermission.of(columns);
        dataPermission.handler(queryWrapper);
        return this.paginateAs(pageNumber, pageSize, queryWrapper, asType);
    }

    default <R> Page<R> paginateAs(Number pageNumber, Number pageSize, Number totalRow, QueryWrapper queryWrapper, Class<R> asType, DataColumn... columns) {
        DataPermission dataPermission = DataPermission.of(columns);
        dataPermission.handler(queryWrapper);
        return this.paginateAs(pageNumber, pageSize, totalRow, queryWrapper, asType);
    }

    default <R> Page<R> paginateAs(Page<R> page, QueryWrapper queryWrapper, Class<R> asType, DataColumn... columns) {
        DataPermission dataPermission = DataPermission.of(columns);
        dataPermission.handler(queryWrapper);
        return this.paginateAs(page, queryWrapper, asType);
    }

    default <R> Page<R> paginateAs(Page<R> page, QueryWrapper queryWrapper, Class<R> asType, DataPermission dataPermission, Consumer<FieldQueryBuilder<R>>... consumers) {
        dataPermission.handler(queryWrapper);
        return this.paginateAs(page, queryWrapper, asType, consumers);
    }

    default <R> Page<R> paginateWithRelationsAs(Number pageNumber, Number pageSize, QueryWrapper queryWrapper, Class<R> asType, DataColumn... columns) {
        DataPermission dataPermission = DataPermission.of(columns);
        dataPermission.handler(queryWrapper);
        return this.paginateWithRelationsAs(pageNumber, pageSize, queryWrapper, asType);
    }

    default <R> Page<R> paginateWithRelationsAs(Number pageNumber, Number pageSize, Number totalRow, QueryWrapper queryWrapper, Class<R> asType, DataColumn... columns) {
        DataPermission dataPermission = DataPermission.of(columns);
        dataPermission.handler(queryWrapper);
        return this.paginateWithRelationsAs(pageNumber, pageSize, totalRow, queryWrapper, asType);
    }

    default <R> Page<R> paginateWithRelationsAs(Page<R> page, QueryWrapper queryWrapper, Class<R> asType, DataColumn... columns) {
        DataPermission dataPermission = DataPermission.of(columns);
        dataPermission.handler(queryWrapper);
        return this.paginateWithRelationsAs(page, queryWrapper, asType);
    }

    default <R> Page<R> paginateWithRelationsAs(Page<R> page, QueryWrapper queryWrapper, Class<R> asType, DataPermission dataPermission, Consumer<FieldQueryBuilder<R>>... consumers) {
        dataPermission.handler(queryWrapper);
        return this.paginateWithRelationsAs(page, queryWrapper, asType, consumers);
    }

    default <E> Page<E> xmlPaginate(String dataSelectId, Page<E> page, QueryWrapper queryWrapper, DataColumn... columns) {
        DataPermission dataPermission = DataPermission.of(columns);
        dataPermission.handler(queryWrapper);
        return this.xmlPaginate(dataSelectId, page, queryWrapper);
    }

    default <E> Page<E> xmlPaginate(String dataSelectId, Page<E> page, Map<String, Object> otherParams, DataColumn... columns) {
        return this.xmlPaginate(dataSelectId, dataSelectId + "_COUNT", page, (QueryWrapper) null, otherParams, columns);
    }

    default <E> Page<E> xmlPaginate(String dataSelectId, Page<E> page, QueryWrapper queryWrapper, Map<String, Object> otherParams, DataColumn... columns) {
        return this.xmlPaginate(dataSelectId, dataSelectId + "_COUNT", page, queryWrapper, otherParams, columns);
    }

    default <E> Page<E> xmlPaginate(String dataSelectId, String countSelectId, Page<E> page, QueryWrapper queryWrapper, Map<String, Object> otherParams, DataColumn... columns) {
        DataPermission dataPermission = DataPermission.of(columns);
        dataPermission.handler(queryWrapper);
        return this.xmlPaginate(dataSelectId, countSelectId, page, queryWrapper, otherParams);
    }


    default <V> Page<V> paginateAs(PageQuery pageQuery, QueryWrapper queryWrapper, Class<V> asType, DataColumn... columns) {
        DataPermission dataPermission = DataPermission.of(columns);
        dataPermission.handler(queryWrapper);
        return this.paginateAs(pageQuery.build(), queryWrapper, asType);
    }

    default <V> Page<V> paginateWithRelationsAs(PageQuery pageQuery, QueryWrapper queryWrapper, Class<V> asType, DataColumn... columns) {
        DataPermission dataPermission = DataPermission.of(columns);
        dataPermission.handler(queryWrapper);
        return this.paginateWithRelationsAs(pageQuery.build(), queryWrapper, asType);
    }

    default <V> Page<V> paginateAs(PageQuery pageQuery, QueryWrapper queryWrapper, Class<V> asType) {
        queryWrapper.orderBy(pageQuery.buildOrderBy());
        return this.paginateAs(pageQuery.build(), queryWrapper, asType);
    }

    default Page<T> paginate(PageQuery pageQuery, QueryWrapper queryWrapper) {
        queryWrapper.orderBy(pageQuery.buildOrderBy());
        return this.paginate(pageQuery.build(), queryWrapper);
    }

    default boolean update(UpdateChain<T> updateChain, DataColumn... columns) {
        DataPermission dataPermission = DataPermission.of(columns);
        String sql = dataPermission.toSQL(false);
        if (StringUtils.isNotBlank(sql)) {
            updateChain.and(sql);
        }
        return updateChain.update();
    }

    default int update(T entity, DataColumn... columns) {
        DataPermission dataPermission = DataPermission.of(columns);
        String sql = dataPermission.toSQL(false);
        if (StringUtils.isBlank(sql)) {
            return this.update(entity);
        }
        QueryWrapper queryWrapper = QueryWrapper.create().where(sql);
        return updateByQuery(entity, queryWrapper);
    }


}
