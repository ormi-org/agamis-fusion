import { Response, Server, createServer } from 'miragejs';
import profilesRoutes from './routes/profiles';
import authRoutes from './routes/auth';

export function v1() {
    createServer({
        logging: true,
        routes() {
            // this.get(`/app/native/fusion-explorer/assets/**`, () => (
            //     new Response(404)
            // )),
            profilesRoutes(this),
            authRoutes(this),
            this.passthrough("**")
        }
    });
}