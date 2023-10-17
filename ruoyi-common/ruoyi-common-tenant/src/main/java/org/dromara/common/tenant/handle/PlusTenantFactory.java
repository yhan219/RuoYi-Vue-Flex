package org.dromara.common.tenant.handle;

import com.mybatisflex.core.tenant.TenantFactory;
import lombok.AllArgsConstructor;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.tenant.helper.TenantHelper;

/**
 * 自定义租户处理器
 *
 * @author Lion Li
 */
@AllArgsConstructor
public class PlusTenantFactory implements TenantFactory {



    @Override
    public Object[] getTenantIds() {
        String tenantId = LoginHelper.getTenantId();
        if (StringUtils.isBlank(tenantId)) {
            return null;
        }
        String dynamicTenantId = TenantHelper.getDynamic();
        if (StringUtils.isNotBlank(dynamicTenantId)) {
            // 返回动态租户
            return new Object[]{dynamicTenantId};
        }
        // 返回固定租户
        return new Object[]{tenantId};
    }

}
