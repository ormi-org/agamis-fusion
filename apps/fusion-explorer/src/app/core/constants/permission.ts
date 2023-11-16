/* eslint-disable @typescript-eslint/no-unused-vars */
enum User {
    READ   = "io.agamis.fusion.user:read.%SELECTOR%",
    CREATE = "io.agamis.fusion.user:create",
    EDIT   = "io.agamis.fusion.user:edit.%SELECTOR%",
    DELETE = "io.agamis.fusion.user:delete.%SELECTOR%",
}

enum Organization {
    READ   = "io.agamis.fusion.organization:read.%SELECTOR%",
    CREATE = "io.agamis.fusion.organization:create",
    EDIT   = "io.agamis.fusion.organization:edit.%SELECTOR%",
    DELETE = "io.agamis.fusion.organization:delete.%SELECTOR%",
}

enum Profile {
    READ   = "io.agamis.fusion.organization#%ORG_ID%.profile:read.%SELECTOR%",
    CREATE = "io.agamis.fusion.organization#%ORG_ID%.profile:create",
    EDIT   = "io.agamis.fusion.organization#%ORG_ID%.profile:edit.%SELECTOR%",
    DELETE = "io.agamis.fusion.organization#%ORG_ID%.profile:delete.%SELECTOR%",
}

enum Application {
    USE    = "io.agamis.fusion.app#%APP_ID%:use",
    LAUNCH = "io.agamis.fusion.app#%APP_ID%:launch",
}

export enum Permission {
    User,
    Organization,
    Profile,
    Application,
}