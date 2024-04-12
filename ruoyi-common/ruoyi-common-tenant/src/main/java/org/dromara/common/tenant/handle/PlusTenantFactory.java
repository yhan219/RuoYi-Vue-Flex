package org.dromara.common.tenant.handle;

import com.mybatisflex.core.tenant.TenantFactory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.tenant.helper.TenantHelper;

/**
 * 自定义租户处理器
 *
 * @author Lion Li
 */
@Slf4j
@AllArgsConstructor
public class PlusTenantFactory implements TenantFactory {



    @Override
    public Object[] getTenantIds() {
        String tenantId = TenantHelper.getTenantId();
        if (StringUtils.isBlank(tenantId)) {
            // 返回动态租户
            log.error("无法获取有效的租户id -> Null");
            return null;
        }
        // 返回固定租户
        return new Object[]{tenantId};
    }

}
