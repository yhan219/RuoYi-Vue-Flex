package org.dromara.workflow.flowable.config;

import com.mybatisflex.core.keygen.impl.FlexIDKeyGenerator;
import org.apache.ibatis.session.SqlSessionFactory;
import org.dromara.workflow.flowable.handler.TaskTimeoutJobHandler;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Collections;


/**
 * flowable配置
 *
 * @author may
 */
@Configuration
public class FlowableConfig implements EngineConfigurationConfigurer<SpringProcessEngineConfiguration> {

    @Autowired
    private GlobalFlowableListener globalFlowableListener;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Override
    public void configure(SpringProcessEngineConfiguration processEngineConfiguration) {
        processEngineConfiguration.setIdGenerator(() -> new FlexIDKeyGenerator().generate(null, null).toString());
        processEngineConfiguration.setEventListeners(Collections.singletonList(globalFlowableListener));
        processEngineConfiguration.addCustomJobHandler(new TaskTimeoutJobHandler());

        // 指定 MyBatis-Flex  数据源
        processEngineConfiguration.setDataSource(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource());

        // 配置 MyBatis-Flex  的事务管理器
        processEngineConfiguration.setTransactionManager(transactionManager);
    }
}
