import { Server } from 'miragejs';
import { default as profiles } from '../../data/dist/profiles.json';

const DEFAULT_ORGANIZATION_ID = '958b761d-abe5-f6d0-069d-c102cf310a16';

const EXCLUDED_FIELDS = ['emails', 'permissions', 'organization', 'user'];

const profilesRoutes = (server: Server) => {
  [
    server.get(
      `/api/v1/organizations/${DEFAULT_ORGANIZATION_ID}/profiles`,
      (schema, request) => {
        const { offset, limit, order_by, include } = request.queryParams;
        const splitOrdered = order_by.split(',');
        const order = [splitOrdered[0], splitOrdered[1]];
        const includedFields = include.split(',');
        const excluded = EXCLUDED_FIELDS.filter(
          (f) => !includedFields.includes(f)
        );
        return profiles.org_profiles_sample_with_user
          .sort((a, b) => {
            switch (order[0]) {
              case 'lastLogin':
                return (
                  (new Date(a[order[0]]).getTime() -
                    new Date(b[order[0]]).getTime()) *
                  Number.parseInt(order[1])
                );
              default:
                return a[order[0]] - b[order[0]] * Number.parseInt(order[1]);
            }
          })
          .slice(Number.parseInt(offset), Number.parseInt(limit))
          .map((p) => {
            excluded.forEach((exc) => {
              p[exc] = undefined;
            });
            return p;
          });
      }
    ),
  ];
};

export default profilesRoutes;
