import { Response, Server } from 'miragejs';
import { default as profiles } from '../../data/dist/profiles.json';

const DEFAULT_ORGANIZATION_ID = '958b761d-abe5-f6d0-069d-c102cf310a16';

const EXCLUDED_FIELDS = ['emails', 'permissions', 'organization', 'user'];

const profilesRoutes = (server: Server) => {
  [
    server.db.createCollection('profiles', profiles.org_profiles_sample_with_user),
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
        return schema.db['profiles']
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
    server.get(
      `/api/v1/organizations/${DEFAULT_ORGANIZATION_ID}/profile/:id`,
      (_, request) => {
        const { id } = request.params;
        // return dynamic result
        const fetched = profiles
        .org_profiles_sample_with_user
        .find(p => p.id === id);

        if (!fetched) {
          return new Response(404, {});
        }
        return fetched;
      }, { timing: 1000 }
    ),
    server.put(
      `/api/v1/organizations/${DEFAULT_ORGANIZATION_ID}/profile/:id`,
      (schema, request) => {
        const { id } = request.params;
        const body = JSON.parse(request.requestBody);

        const {
          alias,
          lastName,
          firstName,
          isActive
        } = body;

        return schema.db['profiles'].update({ id: id }, {
          alias, lastName, firstName, isActive, updatedAt: new Date().toISOString()
        })[0];
      }, { timing: 1000 }
    ),
  ];
};

export default profilesRoutes;
