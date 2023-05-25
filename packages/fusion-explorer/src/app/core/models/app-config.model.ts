export interface AppConfig {
    urls: {
        rest: {
            endpoints: {
                AUTH: string,
                USERS: string,
                PROFILES: string,
                ORGANIZATIONS: string,
                [key: string]: string
            }
        }
    }
}