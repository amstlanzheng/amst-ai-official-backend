

package com.amst.api.common.enums;

/**
 * 超级管理员枚举
 */
@SuppressWarnings("all")
public enum SuperAdminEnum {
    YES(1),
    NO(0);

    private int value;

    SuperAdminEnum(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }
}