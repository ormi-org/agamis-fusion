import { from, lastValueFrom, of, switchMap } from 'rxjs';
import { default as profiles } from '../../data/dist/profiles.json';
import { rest } from 'msw';

const collection = profiles.org_profiles_sample_with_user

const DEFAULT_ORGANIZATION_ID = '958b761d-abe5-f6d0-069d-c102cf310a16'

const EXCLUDED_FIELDS = ['emails', 'permissions', 'organization', 'user']

const profilesRoutes = [
    rest.get(
      `/api/v1/organizations/${DEFAULT_ORGANIZATION_ID}/profiles`,
      (req, res, ctx) => {
        const limit = req.url.searchParams.get('limit');
        const offset = req.url.searchParams.get('offset');
        const order_by = req.url.searchParams.get('order_by');
        const include = req.url.searchParams.get('include');
        const splitOrdered = order_by ? order_by.split(',') : [];
        const order = [splitOrdered[0], splitOrdered[1]];
        const includedFields = include ? include.split(',') : [];
        const excluded = EXCLUDED_FIELDS.filter(
          (f) => !includedFields.includes(f)
        );
        if (!offset || !limit || !order_by || order_by.length < 1) {
          return res(
            ctx.status(400)
          )
        }
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
        return res(
          ctx.delay(200),
          ctx.status(200),
          ctx.json(
            collection
              .sort(sortingFn)
              .slice(Number.parseInt(offset), Number.parseInt(limit))
              .map((p) => {
                excluded.forEach((exc) => {
                  p[exc] = undefined;
                });
                return p;
            })
          )
        )
      }
    ),
    rest.get(
      `/api/v1/organizations/${DEFAULT_ORGANIZATION_ID}/profile/:id`,
      (req, res, ctx) => {
        const { id } = req.params;
        // return dynamic result
        const fetched = collection
        .find(p => p.id === id);

        if (!fetched) {
          return res(
            ctx.delay(200),
            ctx.status(404)
          );
        }
        return res(
          ctx.delay(200),
          ctx.status(200),
          ctx.json(fetched)
        )
      }
    ),
    rest.put(
      `/api/v1/organizations/${DEFAULT_ORGANIZATION_ID}/profile/:id`,
      async (req, res, ctx) => {
        const { id } = req.params;
        return await lastValueFrom(from(req.json<{
          alias: string,
          lastName: string,
          firstName: string,
          isActive: boolean
        }>())
        .pipe(
          switchMap((body) => {
            const {
              alias,
              lastName,
              firstName,
              isActive
            } = body;
            const index = collection.findIndex((p) => p.id === id)
            if (index === -1) {
              return of(res(
                ctx.status(404)
              ))
            }
            const updated = {
              ...collection[index],
              alias,
              lastName,
              firstName,
              isActive,
              updatedAt: new Date().toISOString()
            }
            collection[index] = updated
            return of(res(
              ctx.delay(200),
              ctx.status(200),
              ctx.json(updated)
            ))
          })
        ))
      }
    ),
]

export default profilesRoutes
