import { baseEnvironment } from "./environment.base"
import { Env } from "./typed/environment"
import '../mocks/server/rest'

export const environment: Env = {
    ...baseEnvironment,
    enableMock: true
}
