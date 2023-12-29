<img src="https://raw.githubusercontent.com/yhan219/blog-image/master/yhan/ruoyivueflex.png" width="50%" height="50%">
<div style="height: 10px; clear: both;"></div>

---

# 简介

[![码云Gitee](https://gitee.com/yhan219/ruoyi-vue-flex/badge/star.svg?theme=blue)](https://gitee.com/yhan219/ruoyi-vue-flex)
[![GitHub](https://img.shields.io/github/stars/yhan219/ruoyi-vue-flex.svg?style=social&label=Stars)](https://github.com/yhan219/ruoyi-vue-flex)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://gitee.com/yhan219/ruoyi-vue-flex/blob/mybatis-flex/LICENSE)
<br>
[![ruoyi-vue-flex](https://img.shields.io/badge/ruoyi_vue_flex-5.1.2-success.svg)](https://gitee.com/yhan219/ruoyi-vue-flex)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1-blue.svg)]()
[![JDK-17](https://img.shields.io/badge/JDK-17-green.svg)]()
[![JDK-21](https://img.shields.io/badge/JDK-21-green.svg)]()


ruoyi-vue-flex是基于[ruoyi-vue-plus 5.X](https://gitee.com/dromara/RuoYi-Vue-Plus/tree/5.X/)分支的一个快速开发框架。

RuoYi-Vue-Plus 是重写 RuoYi-Vue 针对 `分布式集群与多租户` 场景全方位升级(不兼容原框架)

ruoyi-vue-flex将ruoyi-vue-plus中使用的mybatis-plus替换为mybatis-flex,并根据两个ORM框架使用的不同修改了部分逻辑。底层完全重写。

**ruoyi-vue-flex与ruoyi-vue-plus功能完全相同。**

ruoyi-vue-flex将定期同步ruoyi-vue-plus，非冲突功能每天升级，冲突功能最迟不超过一周完成升级。

# 版本
与ruoyi-vue-plus保持一致，当前版本`5.1.2`

# 与ruoyi-vue-plus的差异
## 前端框架差异
默认使用[ruoyi-plus-vben](https://gitee.com/dapppp/ruoyi-plus-vben)，可在配置文件中修改为[plus-ui](https://gitee.com/JavaLionLi/plus-ui)
```
# 代码生成
gen:
  # 使用的模版 默认是vben，原生的是vue
  platform: vben
```

## 数据权限用法差异
数据权限注解，修改为类，原写法：
```java
    @DataPermission({
        @DataColumn(key = "deptName", value = "d.dept_id"),
        @DataColumn(key = "userName", value = "r.create_by")
    })
    Page<SysRoleVo> selectPageRoleList(@Param("page") Page<SysRole> page, @Param(Constants.WRAPPER) Wrapper<SysRole> queryWrapper);
```
现写法：
```java
    Page<SysRoleVo> selectPageRoleList(PageQuery pageQuery, QueryWrapper queryWrapper){
        return paginateAs(pageQuery, queryWrapper, DataColumn.of("deptName", "d.dept_id"), DataColumn.of("userName", "r.create_by"));
    }
```
> 注：尝试写过拦截器以达到用法完全相同的目的，可惜拦截器功能和mybatis flex的插件不兼容，使用了数据权限插件，则无法使用mybatis flex的多租户插件等。如果你有更好的方法，欢迎pr

## 忽略租户写法差异
- 配置中的差异：
  原写法： 在yml中配置忽略的表，mybatis-flex不支持,但mybatis-flex会默认忽略没有多租户字段的表
- 代码中的差异：
  原写法:在mapper中配置注解：
```java
    @InterceptorIgnore(tenantLine = "true")
    SysUserVo selectTenantUserByUserName(@Param("userName") String userName, @Param("tenantId") String tenantId);
```
  现写法：
```java
// mapper中删除InterceptorIgnore注解，在service中手动忽略
TenantHelper.ignore(() -> baseMapper.selectTenantUserByUserName(userName, tenantId));
```

## 特别鸣谢
[ruoyi-vue-plus](https://gitee.com/dromara/RuoYi-Vue-Plus/tree/5.X/)

[mybatis-flex](https://gitee.com/yhan219/mybatis-flex)

[ruoyi-plus-vben](https://gitee.com/dapppp/ruoyi-plus-vben)

## 预览图

![](https://raw.githubusercontent.com/yhan219/blog-image/master/yhan/iShot_2023-12-21_19.42.28.jpg)

![](https://raw.githubusercontent.com/yhan219/blog-image/master/yhan/iShot_2023-12-21_19.42.50.jpg)

![](https://raw.githubusercontent.com/yhan219/blog-image/master/yhan/iShot_2023-12-21_19.42.59.jpg)

![](https://raw.githubusercontent.com/yhan219/blog-image/master/yhan/iShot_2023-12-21_19.43.09.jpg)

![](https://raw.githubusercontent.com/yhan219/blog-image/master/yhan/iShot_2023-12-21_19.43.29.jpg)

![](https://raw.githubusercontent.com/yhan219/blog-image/master/yhan/iShot_2023-12-21_19.43.37.jpg)

![](https://raw.githubusercontent.com/yhan219/blog-image/master/yhan/iShot_2023-12-21_19.43.46.jpg)

![](https://raw.githubusercontent.com/yhan219/blog-image/master/yhan/iShot_2023-12-21_19.44.05.jpg)

![](https://raw.githubusercontent.com/yhan219/blog-image/master/yhan/iShot_2023-12-21_19.44.25.jpg)

![](https://raw.githubusercontent.com/yhan219/blog-image/master/yhan/iShot_2023-12-21_19.44.40.jpg)

![](https://raw.githubusercontent.com/yhan219/blog-image/master/yhan/iShot_2023-12-21_19.44.47.jpg)

![](https://raw.githubusercontent.com/yhan219/blog-image/master/yhan/iShot_2023-12-21_19.45.12.jpg)

![](https://raw.githubusercontent.com/yhan219/blog-image/master/yhan/iShot_2023-12-21_19.45.32.jpg)

![](https://raw.githubusercontent.com/yhan219/blog-image/master/yhan/iShot_2023-12-21_19.45.48.jpg)

![](https://raw.githubusercontent.com/yhan219/blog-image/master/yhan/iShot_2023-12-21_19.46.10.jpg)

![](https://raw.githubusercontent.com/yhan219/blog-image/master/yhan/iShot_2023-12-21_19.55.28.jpg)
