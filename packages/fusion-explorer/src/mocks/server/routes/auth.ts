import { Server } from "miragejs";
import { UserInfo } from "@core/models/user-info.model";

const MOCK_USER: UserInfo = {
    id: "8ff8dbf7-e175-46f0-af28-e702732565a9",
    username: "chauncey.vonsnuffles",
    firstname: "Chauncey",
    lastname: "Von Snuffles",
    email: "chauncey.vonsnuffles@divinitysreach.tyria"
};

const authRoutes = (server: Server) => {[
    server.get(`/auth/userinfo`, () => (
        MOCK_USER
    ))
]};

export default authRoutes;