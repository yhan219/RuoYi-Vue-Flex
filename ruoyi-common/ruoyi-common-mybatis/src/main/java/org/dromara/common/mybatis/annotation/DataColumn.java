package org.dromara.common.mybatis.annotation;

import com.mybatisflex.core.query.QueryColumn;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据权限
 * <p>
 * 一个注解只能对应一个模板
 *
 * @author yhan219
 * @version 3.5.0
 */

@AllArgsConstructor
@Getter
public class DataColumn {

    /**
     * 占位符关键字
     */
    private String[] key;

    /**
     * 占位符替换值
     */
    private String[] value;

    public static DataColumn of(String[] key, String[] value) {
        return new DataColumn(key, value);
    }


    public static DataColumn of(String key, String value) {
        return new DataColumn(new String[]{key}, new String[]{value});
    }

    public static DataColumn of(String key, QueryColumn value) {
        return new DataColumn(new String[]{key}, new String[]{"`" + value.getTable().getName() + "`.`" + value.getName() + "`"});
    }

}
