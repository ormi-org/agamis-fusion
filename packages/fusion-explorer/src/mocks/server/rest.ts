import { createServer } from 'miragejs';
import authRoutes from './routes/auth';
import organizationsRoutes from './routes/organizations';
import profilesRoutes from './routes/profiles';

export function v1() {
    createServer({
        logging: true,
        routes() {
            // this.get(`/app/native/fusion-explorer/assets/**`, () => (
            //     new Response(404)
            // )),
            profilesRoutes(this),
            organizationsRoutes(this),
            authRoutes(this),
            this.passthrough("**")
        }
    });
}