package com.sc.whorl.authorization.annotate;

/**
 * <一句话功能简述> <功能详细描述>
 *
 * @auth:senssic
 * @see: [相关类/方法]（可选）
 */

public enum AuthorizeEnum {
    //验证用户是否具备某权限
    HAS_PERMI("hasPermi"),
    //验证用户是否不具备某权限，与 hasPermi逻辑相反
    LACKS_PERMI("lacksPermi"),
    //验证用户是否具有以下任意一个权限
    HAS_ANY_PERMI("hasAnyPermi"),
    //判断用户是否拥有某个角色
    HAS_ROLE("hasRole"),
    //验证用户是否不具备某角色，与 isRole逻辑相反。
    LACKS_ROLE("lacksRole"),
    //验证用户是否具有以下任意一个角色
    HAS_ANY_ROLES("hasAnyRoles");
    private String method;

    AuthorizeEnum(String method) {
        this.method = method;
    }
}
