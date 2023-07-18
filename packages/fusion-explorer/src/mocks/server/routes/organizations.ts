import { rest } from "msw";
import { default as organizations } from "../../data/dist/organizations.json";

const organizationsRoutes = [
    rest.get(`/api/organizations/:orgId`, (req, res, ctx) => {
        const { orgId } = req.params;
        const org = organizations.organizations_sample.find(org => org.id === orgId);
        if (org === undefined) {
            return res(
                ctx.status(404),
                ctx.json({
                    erros: [`organization not found with id:#${orgId}`]
                })
            )
        }
        return res(
            ctx.status(200),
            ctx.json(org)
        )
    })
];

export default organizationsRoutes;