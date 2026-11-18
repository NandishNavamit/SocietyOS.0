package com.database.societyproject.security.entity;

public enum UserRole {
    ADMIN,
    SOCIETY_ADMIN,
    FLAT_OWNER,
    LOGBOOK_OPERATOR,

    /** @deprecated legacy data only; never granted or authorized in the simplified product. */
    @Deprecated
    SOCIETY_MANAGER,
    /** @deprecated legacy data only; never granted or authorized in the simplified product. */
    @Deprecated
    FINANCE_MANAGER,
    /** @deprecated legacy data only; never granted or authorized in the simplified product. */
    @Deprecated
    SECURITY_MANAGER,
    /** @deprecated legacy data only; never granted or authorized in the simplified product. */
    @Deprecated
    BLOCK_MANAGER,
    /** @deprecated legacy data only; never granted or authorized in the simplified product. */
    @Deprecated
    RESIDENT,
    /** @deprecated legacy data only; never granted or authorized in the simplified product. */
    @Deprecated
    STAFF,
    /** @deprecated legacy data only; never granted or authorized in the simplified product. */
    @Deprecated
    SECURITY;

    public boolean isCurrentProductRole() {
        return this == ADMIN || this == SOCIETY_ADMIN || this == FLAT_OWNER || this == LOGBOOK_OPERATOR;
    }
}
