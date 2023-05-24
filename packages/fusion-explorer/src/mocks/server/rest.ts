import { createServer } from 'miragejs';
import profilesRoutes from './routes/profiles';

export function v1() {
    createServer({
        logging: true,
        routes() {
            this.passthrough("/app/native/fusion-explorer/assets/**"),
            profilesRoutes(this)
        }
    });
}