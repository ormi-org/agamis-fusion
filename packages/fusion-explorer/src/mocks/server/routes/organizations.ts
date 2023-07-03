import { Response, Server } from "miragejs";
import { default as organizations } from "../../data/dist/organizations.json";

const organizationsRoutes = (server: Server) => {[
    server.get(`/api/v1/organizations/:orgId`, (_, request) => {
        const id = request.params["orgId"];
        const org = organizations.organizations_sample.find(org => org.id === id);
        if (org === undefined) {
            return new Response(404, {}, { errors: [`organization not found with id:#${id}`] });
        }
        return org;
    }, { timing: 1000 })
]};

export default organizationsRoutes;