import { baseEnvironment } from "./environment.base";
import { Env } from "./typed/environment";

export const environment: Env = {
    ...baseEnvironment,
    production: true,
    baseUrl: '/app/native/fusion'
};
