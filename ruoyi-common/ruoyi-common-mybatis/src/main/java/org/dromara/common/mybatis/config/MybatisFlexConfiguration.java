package org.dromara.common.mybatis.config;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpStatus;
import com.mybatisflex.annotation.InsertListener;
import com.mybatisflex.annotation.UpdateListener;
import com.mybatisflex.core.FlexGlobalConfig;
import com.mybatisflex.core.audit.AuditManager;
import com.mybatisflex.core.audit.ConsoleMessageCollector;
import com.mybatisflex.core.audit.MessageCollector;
import com.mybatisflex.core.query.QueryColumnBehavior;
import com.mybatisflex.spring.boot.MyBatisFlexCustomizer;
import com.mybatisflex.spring.boot.MybatisFlexAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.factory.YmlPropertySourceFactory;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.system.api.model.LoginUser;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Date;

/**
 * mybatis-flex配置类(下方注释有插件介绍)
 *
 * @author yhan219
 */
@Slf4j
@AutoConfiguration(before = MybatisFlexAutoConfiguration.class)
@MapperScan("${mybatis-flex.mapperPackage}")
@EnableTransactionManagement(proxyTargetClass = true)
@PropertySource(value = "classpath:common-mybatis.yml", factory = YmlPropertySourceFactory.class)
public class MybatisFlexConfiguration implements MyBatisFlexCustomizer {


    @Value("${mybatis-flex.configuration.audit_enable}")
    private Boolean enableAudit = false;

    @Value("${mybatis-flex.configuration.sql_print}")
    private Boolean sqlPrint = false;


    static {
        QueryColumnBehavior.setIgnoreFunction(QueryColumnBehavior.IGNORE_BLANK);
        QueryColumnBehavior.setSmartConvertInToEquals(true);
    }



    @Override
    public void customize(FlexGlobalConfig globalConfig) {

        AuditManager.setAuditEnable(enableAudit);
        if (sqlPrint) {
            // 开启sql打印默认会开启sql审计
            AuditManager.setAuditEnable(true);
            //设置 SQL 审计收集器
            MessageCollector collector = new ConsoleMessageCollector();
            AuditManager.setMessageCollector(collector);
        }


        //我们可以在这里进行一些列的初始化配置
        InsertListener insertListener = o -> {
            try {
                if (ObjectUtil.isNotNull(o) && o instanceof BaseEntity baseEntity) {
                    Date current = ObjectUtil.isNotNull(baseEntity.getCreateTime())
                        ? baseEntity.getCreateTime() : new Date();
                    baseEntity.setCreateTime(current);
                    baseEntity.setUpdateTime(current);
                    LoginUser loginUser = getLoginUser();
                    if (ObjectUtil.isNotNull(loginUser)) {
                        Long userId = ObjectUtil.isNotNull(baseEntity.getCreateBy())
                            ? baseEntity.getCreateBy() : loginUser.getUserId();
                        // 当前已登录 且 创建人为空 则填充
                        baseEntity.setCreateBy(userId);
                        // 当前已登录 且 更新人为空 则填充
                        baseEntity.setUpdateBy(userId);
                        baseEntity.setCreateDept(ObjectUtil.isNotNull(baseEntity.getCreateDept())
                            ? baseEntity.getCreateDept() : loginUser.getDeptId());
                    }
                }
            } catch (Exception e) {
                throw new ServiceException("自动注入异常 => " + e.getMessage(), HttpStatus.HTTP_UNAUTHORIZED);
            }
        };

        UpdateListener updateListener = o -> {

            try {
                if (ObjectUtil.isNotNull(o) && o instanceof BaseEntity baseEntity) {
                    Date current = new Date();
                    // 更新时间填充(不管为不为空)
                    baseEntity.setUpdateTime(current);
                    LoginUser loginUser = getLoginUser();
                    // 当前已登录 更新人填充(不管为不为空)
                    if (ObjectUtil.isNotNull(loginUser)) {
                        baseEntity.setUpdateBy(loginUser.getUserId());
                    }
                }
            } catch (Exception e) {
                throw new ServiceException("自动注入异常 => " + e.getMessage(), HttpStatus.HTTP_UNAUTHORIZED);
            }
        };
        globalConfig.registerInsertListener(insertListener, BaseEntity.class);
        globalConfig.registerUpdateListener(updateListener, BaseEntity.class);

    }

    private LoginUser getLoginUser() {
        LoginUser loginUser;
        try {
            loginUser = LoginHelper.getLoginUser();
        } catch (Exception e) {
            log.warn("自动注入警告 => 用户未登录");
            return null;
        }
        return loginUser;
    }


    //@Bean
    //public PlusDataPermissionInterceptor plusDataPermissionInterceptor(PlusDataPermissionHandler plusDataPermissionHandler) {
    //    return new PlusDataPermissionInterceptor(plusDataPermissionHandler);
    //}


    //@Bean
    //public PageInterceptor pageInterceptor() {
    //    return new PageInterceptor();
    //}


}
