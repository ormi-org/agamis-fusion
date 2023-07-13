import { createServer } from 'miragejs';
import authRoutes from './routes/auth';
import organizationsRoutes from './routes/organizations';
import profilesRoutes from './routes/profiles';

const token = "057c9d098e2282934495051c86c8ed1e1e0f2b3cf605ed819d05bff697451104"
const refreshToken = "eabadef5a27f79422c6b7b45bd5ce890d1a1e4e24af63f821d69ccf42d5099bb"

export function seedLocalStorage() {
    localStorage.setItem('token', token)
    localStorage.setItem('refreshToken', refreshToken)
}

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