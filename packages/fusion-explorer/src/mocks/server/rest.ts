import { createServer } from 'miragejs';
import profilesRoutes from './routes/profiles';

export function v1() {
    createServer({
        logging: true,
        namespace: '/api/v1',
        routes() {
            profilesRoutes(this)
        }
    });
}