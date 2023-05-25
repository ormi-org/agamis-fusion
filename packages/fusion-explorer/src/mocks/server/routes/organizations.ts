import { Server } from "miragejs";
import { default as organizations } from "../../data/dist/organizations.json";

const DEFAULT_ORGANIZATION_ID = '649454cb-a859-4ac4-bbce-c0c7c43a999e';

const organizationsRoutes = (server: Server) => {[
    server.get(`/organization/:orgId`, (_, request) => {
        let id = request.params["orgId"];
        return organizations.organizations_sample.filter(org => org)
    })
]};

export default organizationsRoutes;