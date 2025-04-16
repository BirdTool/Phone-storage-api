import { PrismaClient } from "#prisma";
import { Hono } from "hono";
import { sign } from "hono/jwt";
import { createLog } from "src/utils/log";

const prisma = new PrismaClient();
const secretKey = process.env.USER_TOKEN!;

const login = new Hono();

login.post("/", async (c) => {
    const { email, password } = await c.req.json();

    if (!email || !password) {
        return c.json({ error: "Missing email or password" }, 400);
    }

    const user = await prisma.user.findUnique({
        where: { email },
    });

    if (!user) {
        return c.json({ error: "Invalid email" }, 401);
    }

    if (user.password !== password) {
        await createLog({
            principalId: user.id,
            title: "Invalid login",
            description: "Usuário tentou logar com senha incorreta",
        });
        return c.json({ error: "Invalid password" }, 401);
    }

    // Gera o token JWT
    const token = await sign(
        { id: user.id },
        secretKey
    );

    await createLog({
        principalId: user.id,
        title: "Login",
        description: "Usuário logou na aplicação",
    });

    // Retorna o token no HEADER da resposta
    return c.body(null, 200, {
        "UserAuthorization": token, // ou "Authorization": `Bearer ${token}`
    });
});

export default login;
