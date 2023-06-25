import { Response, Server } from "miragejs";
import { default as organizations } from "../../data/dist/organizations.json";

const DEFAULT_ORGANIZATION_ID = '958b761d-abe5-f6d0-069d-c102cf310a16';

const organizationsRoutes = (server: Server) => {[
    server.get(`/api/v1/organizations/:orgId`, (_, request) => {
        let id = request.params["orgId"];
        const org = organizations.organizations_sample.find(org => org.id === id);
        if (org === undefined) {
            return new Response(404, {}, { errors: [`organization not found with id:#${id}`] });
        }
        return org;
    }, { timing: 1000 })
]};

export default organizationsRoutes;