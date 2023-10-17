package org.dromara.common.tenant.config;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.hutool.core.util.ObjectUtil;
import com.mybatisflex.core.tenant.TenantFactory;
import org.dromara.common.core.utils.reflect.ReflectUtils;
import org.dromara.common.mybatis.config.MybatisFlexConfig;
import org.dromara.common.redis.config.RedisConfig;
import org.dromara.common.redis.config.properties.RedissonProperties;
import org.dromara.common.tenant.core.TenantSaTokenDao;
import org.dromara.common.tenant.handle.PlusTenantFactory;
import org.dromara.common.tenant.handle.TenantKeyPrefixHandler;
import org.dromara.common.tenant.manager.TenantSpringCacheManager;
import org.dromara.common.tenant.properties.TenantProperties;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.SingleServerConfig;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * 租户配置类
 *
 * @author Lion Li
 */
@EnableConfigurationProperties(TenantProperties.class)
@AutoConfiguration(after = {RedisConfig.class, MybatisFlexConfig.class})
@ConditionalOnProperty(value = "tenant.enable", havingValue = "true")
public class TenantConfig {


    /**
     * 多租户插件
     */
    @Bean
    public TenantFactory tenantFactory() {
        return new PlusTenantFactory();
    }

    @Bean
    public RedissonAutoConfigurationCustomizer tenantRedissonCustomizer(RedissonProperties redissonProperties) {
        return config -> {
            TenantKeyPrefixHandler nameMapper = new TenantKeyPrefixHandler(redissonProperties.getKeyPrefix());
            SingleServerConfig singleServerConfig = ReflectUtils.invokeGetter(config, "singleServerConfig");
            if (ObjectUtil.isNotNull(singleServerConfig)) {
                // 使用单机模式
                // 设置多租户 redis key前缀
                singleServerConfig.setNameMapper(nameMapper);
                ReflectUtils.invokeSetter(config, "singleServerConfig", singleServerConfig);
            }
            ClusterServersConfig clusterServersConfig = ReflectUtils.invokeGetter(config, "clusterServersConfig");
            // 集群配置方式 参考下方注释
            if (ObjectUtil.isNotNull(clusterServersConfig)) {
                // 设置多租户 redis key前缀
                clusterServersConfig.setNameMapper(nameMapper);
                ReflectUtils.invokeSetter(config, "clusterServersConfig", clusterServersConfig);
            }
        };
    }

    /**
     * 多租户缓存管理器
     */
    @Primary
    @Bean
    public CacheManager tenantCacheManager() {
        return new TenantSpringCacheManager();
    }

    /**
     * 多租户鉴权dao实现
     */
    @Primary
    @Bean
    public SaTokenDao tenantSaTokenDao() {
        return new TenantSaTokenDao();
    }

}
