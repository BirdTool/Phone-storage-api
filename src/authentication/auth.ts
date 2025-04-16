import { Context } from "hono";
import { verify } from "hono/jwt";
import { UserPayload } from "../types/userPayLoad";

const secretKey = process.env.USER_TOKEN!;

export const authMiddleware = async (c: Context, next: Function) => {
  const token = c.req.header("UserAuthorization");

  if (!token) {
    return c.json({ error: "Unauthorized: Token não encontrado" }, 401);
  }

  try {
    const decoded = await verify(token, secretKey) as UserPayload;

    if (!decoded.id) {
      return c.json({ error: "Token inválido: dados incompletos" }, 401);
    }

    c.set("user", decoded);
    await next();
  } catch (error) {
    return c.json({ error: "Token inválido ou expirado" }, 401);
  }
};
