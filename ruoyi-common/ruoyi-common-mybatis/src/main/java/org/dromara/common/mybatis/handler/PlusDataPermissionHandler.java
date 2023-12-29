package org.dromara.common.mybatis.handler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.dto.RoleDTO;
import org.dromara.common.core.domain.model.LoginUser;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.core.utils.StreamUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.annotation.DataColumn;
import org.dromara.common.mybatis.annotation.DataPermission;
import org.dromara.common.mybatis.enums.DataScopeType;
import org.dromara.common.mybatis.helper.DataPermissionHelper;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.BeanResolver;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * 数据权限处理器
 *
 * @author yhan219
 * @see <a href="https://gitee.com/dromara/RuoYi-Vue-Plus/blob/5.X/ruoyi-common/ruoyi-common-mybatis/src/main/java/org/dromara/common/mybatis/handler/PlusDataPermissionHandler.java">参考</a>
 */
@Slf4j
@Component
public class PlusDataPermissionHandler {

    /**
     * spel 解析器
     */
    private final ExpressionParser parser = new SpelExpressionParser();
    private final ParserContext parserContext = new TemplateParserContext();
    /**
     * bean解析器 用于处理 spel 表达式中对 bean 的调用
     */
    private final BeanResolver beanResolver = new BeanFactoryResolver(SpringUtils.getBeanFactory());


    public void handlerDataPermission(DataPermission dataPermission, QueryWrapper queryWrapper, boolean isSelect) {
        if (dataPermission == null) {
            return;
        }
        DataColumn[] dataColumns = dataPermission.getValue();
        if (ArrayUtil.isEmpty(dataColumns)) {
            return;
        }
        LoginUser currentUser = DataPermissionHelper.getVariable("user");
        if (ObjectUtil.isNull(currentUser)) {
            currentUser = LoginHelper.getLoginUser();
            DataPermissionHelper.setVariable("user", currentUser);
        }
        // 如果是超级管理员或租户管理员，则不过滤数据
        if (LoginHelper.isSuperAdmin() || LoginHelper.isTenantAdmin()) {
            return;
        }
        String dataFilterSql = buildDataFilter(dataColumns, isSelect);
        if (StringUtils.isBlank(dataFilterSql)) {
            return;
        }
        if (queryWrapper == null) {
            queryWrapper = new QueryWrapper();
        }
        queryWrapper.and(dataFilterSql);
    }


    public String getSQL(DataPermission dataPermission, boolean isSelect) {
        return buildDataFilter(dataPermission.getValue(), isSelect);
    }

    /**
     * 构造数据过滤sql
     */
    private String buildDataFilter(DataColumn[] dataColumns, boolean isSelect) {
        // 更新或删除需满足所有条件
        String joinStr = isSelect ? " OR " : " AND ";
        LoginUser user = DataPermissionHelper.getVariable("user");
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setBeanResolver(beanResolver);
        DataPermissionHelper.getContext().forEach(context::setVariable);
        Set<String> conditions = new HashSet<>();
        for (RoleDTO role : user.getRoles()) {
            user.setRoleId(role.getRoleId());
            // 获取角色权限泛型
            DataScopeType type = DataScopeType.findCode(role.getDataScope());
            if (ObjectUtil.isNull(type)) {
                throw new ServiceException("角色数据范围异常 => " + role.getDataScope());
            }
            // 全部数据权限直接返回
            if (type == DataScopeType.ALL) {
                return "";
            }
            boolean isSuccess = false;
            for (DataColumn dataColumn : dataColumns) {
                if (dataColumn.getKey().length != dataColumn.getValue().length) {
                    throw new ServiceException("角色数据范围异常 => getKey与getValue长度不匹配");
                }
                // 不包含 getKey 变量 则不处理
                if (!StringUtils.containsAny(type.getSqlTemplate(),
                    Arrays.stream(dataColumn.getKey()).map(getKey -> "#" + getKey).toArray(String[]::new)
                )) {
                    continue;
                }
                // 设置注解变量 getKey 为表达式变量 getValue 为变量值
                for (int i = 0; i < dataColumn.getKey().length; i++) {
                    context.setVariable(dataColumn.getKey()[i], dataColumn.getValue()[i]);
                }

                // 解析sql模板并填充
                String sql = parser.parseExpression(type.getSqlTemplate(), parserContext).getValue(context, String.class);
                conditions.add(joinStr + sql);
                isSuccess = true;
            }
            // 未处理成功则填充兜底方案
            if (!isSuccess && StringUtils.isNotBlank(type.getElseSql())) {
                conditions.add(joinStr + type.getElseSql());
            }
        }

        if (CollUtil.isNotEmpty(conditions)) {
            String sql = StreamUtils.join(conditions, Function.identity(), "");
            return sql.substring(joinStr.length());
        }
        return "";
    }


}
