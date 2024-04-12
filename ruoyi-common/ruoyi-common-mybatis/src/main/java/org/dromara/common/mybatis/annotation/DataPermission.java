package org.dromara.common.mybatis.annotation;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.mybatis.handler.PlusDataPermissionHandler;

/**
 * 数据权限组
 *
 * @author Lion Li
 * @version 3.5.0
 */
@AllArgsConstructor
@Getter
public class DataPermission {

    private static final PlusDataPermissionHandler DATA_PERMISSION_HANDLER = SpringUtils.getBean(PlusDataPermissionHandler.class);

    DataColumn[] value;


    public static DataPermission of(DataColumn... value) {
        return new DataPermission(value);
    }

    public void handler(QueryWrapper queryWrapper) {
        DATA_PERMISSION_HANDLER.handlerDataPermission(this, queryWrapper, true);
    }

    public String toSQL(boolean isSelect) {
        return DATA_PERMISSION_HANDLER.getSQL(this, isSelect);
    }

    public String toSQL() {
        return toSQL(true);
    }

}
