import { Server } from "miragejs";
import { default as profiles } from "../../data/dist/profiles.json";

const DEFAULT_ORGANIZATION_ID = '958b761d-abe5-f6d0-069d-c102cf310a16';

const profilesRoutes = (server: Server) => {[
    server.get(`/api/v1/organizations/${DEFAULT_ORGANIZATION_ID}/profiles`, () => (
        profiles.org_profiles_sample_with_user
    ))
]};

export default profilesRoutes;