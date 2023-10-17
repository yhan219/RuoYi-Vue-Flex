package org.dromara.system.mapper;

import com.mybatisflex.core.query.QueryWrapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.system.domain.SysDictData;
import org.dromara.system.domain.vo.SysDictDataVo;

import java.util.List;

import static org.dromara.system.domain.table.SysDictDataTableDef.SYS_DICT_DATA;

/**
 * 字典表 数据层
 *
 * @author Lion Li
 */
public interface SysDictDataMapper extends BaseMapperPlus<SysDictData> {

    default List<SysDictDataVo> selectDictDataByType(String dictType) {
        return selectListByQueryAs(
            QueryWrapper.create().select().from(SYS_DICT_DATA)
                .where(SYS_DICT_DATA.DICT_TYPE.eq(dictType))
                .orderBy(SYS_DICT_DATA.DICT_SORT, true),
            SysDictDataVo.class);
    }
}
