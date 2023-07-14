import { rest } from 'msw';
import { UserInfo } from "@core/models/user-info.model";

const fakeJwt = "c96dad8b420b2fe59e65ecc83356fed6531d489ed64d132a7141bfd4d84d1153"

const MOCK_USER: UserInfo = {
    id: "8ff8dbf7-e175-46f0-af28-e702732565a9",
    username: "chauncey.vonsnuffles",
    firstname: "Chauncey",
    lastname: "Von Snuffles",
    email: "chauncey.vonsnuffles@divinitysreach.tyria"
};

const authRoutes = [
    rest.get(
        `/auth/userinfo`,
        (req, res, ctx) => {
            const { token } = req.cookies;
            if (token === undefined) {
                return res(
                    ctx.status(401)
                )
            }
            return res(
                ctx.status(200),
                ctx.json(MOCK_USER)
            )
        }
    ),
    rest.get(
        `/auth/refreshtoken`,
        (_req, res, ctx) => {
            const cookieExp = new Date(new Date().getTime() + 60 * 30)
            return res(
                ctx.status(200),
                ctx.cookie("token", fakeJwt, {
                    domain: 'localhost:4200',
                    expires: cookieExp
                }),
                ctx.json({
                    token: fakeJwt  
                })
            )
        }
    )
];

export default authRoutes;