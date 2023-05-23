import { Server } from "miragejs";
import { default as profiles } from "../../data/dist/profiles.json";

const DEFAULT_ORGANIZATION_ID = '649454cb-a859-4ac4-bbce-c0c7c43a999e';

const profilesRoutes = (server: Server) => {[
    server.get(`/organization/${DEFAULT_ORGANIZATION_ID}/profiles?include=relatedUser`, () => (
        profiles.org_profiles_sample_with_user
    ))
]};

export default profilesRoutes;