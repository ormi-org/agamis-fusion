import { Server } from 'miragejs';
import { default as profiles } from '../../data/dist/profiles.json';

const DEFAULT_ORGANIZATION_ID = '958b761d-abe5-f6d0-069d-c102cf310a16';

const EXCLUDED_FIELDS = ['emails', 'permissions', 'organization', 'user'];

const profilesRoutes = (server: Server) => {
  [
    server.get(
      `/api/v1/organizations/${DEFAULT_ORGANIZATION_ID}/profiles`,
      (_, request) => {
        const { offset, limit, order_by, include } = request.queryParams;
        const splitOrdered = order_by.split(',');
        const order = [splitOrdered[0], splitOrdered[1]];
        const includedFields = include.split(',');
        const excluded = EXCLUDED_FIELDS.filter(
          (f) => !includedFields.includes(f)
        );
        let sortingFn: (a, b) => number;
        switch (order[0]) {
          case 'lastLogin':
            sortingFn = (a, b) => {
              return (new Date(a[order[0]]).getTime() -
                new Date(b[order[0]]).getTime()) *
              Number.parseInt(order[1])
            };
            break;
          case 'username':
            sortingFn = (a, b) => {
              const nameA = a['alias'] ? a['alias'] : a['user'][order[0]];
              const nameB = b['alias'] ? b['alias'] : b['user'][order[0]];
              if (nameA < nameB) return -1 * Number.parseInt(order[1]);
              if (nameA > nameB) return 1 * Number.parseInt(order[1]);
              return 0;
            }
            break;
          case 'lastName':
          case 'firstName':
            sortingFn = (a, b) => {
              if (a[order[0]] < b[order[0]]) return -1 * Number.parseInt(order[1]);
              if (a[order[0]] > b[order[0]]) return 1 * Number.parseInt(order[1]);
              return 0;
            }
            break;
          default:
            sortingFn = (a, b) => a[order[0]] - b[order[0]] * Number.parseInt(order[1]);
        }
        // return dynamic result
        return profiles.org_profiles_sample_with_user
        .sort(sortingFn)
        .slice(Number.parseInt(offset), Number.parseInt(limit))
        .map((p) => {
          excluded.forEach((exc) => {
            p[exc] = undefined;
          });
          return p;
        });
      }, { timing: 1000 }
    ),
  ];
};

export default profilesRoutes;
