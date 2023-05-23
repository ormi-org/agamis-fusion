export interface AppConfig {
    urls: {
        rest: {
            endpoints: {
                [key: string]: string
            }
        }
    }
}