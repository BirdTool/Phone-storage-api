import { PrismaClient } from '#prisma'
import { randomUUIDv7 } from 'bun';
import { Context } from 'hono'
import { createLog } from 'src/utils/log';
import fs from 'fs'
import path from 'path'

const prisma = new PrismaClient()
const STORAGE_PATH = path.join(process.cwd(), 'src', 'storage');

export const postUser = async (c: Context) => {
    try {
        const { username, email, password } = await c.req.json();

        if (!username || !email || !password) {
            return c.text('Bad Request', 400)
        }
        
        const existingUser = await prisma.user.findUnique({
            where: { email }
        })

        if (existingUser) {
            return c.text('User already exists', 409)
        }

        const uuid = randomUUIDv7();
        
        const user = await prisma.user.create({
            data: {
                name: username,
                email,
                password,
                uuid
            }
        })

        if (!fs.existsSync(STORAGE_PATH)) {
            fs.mkdirSync(STORAGE_PATH, { recursive: true });
        }

        try {
            fs.mkdirSync(path.join(STORAGE_PATH, uuid), { recursive: true });
        } catch (err) {
            console.error("Erro ao criar diretório:", err);
            await prisma.user.delete({ where: { id: user.id } });
            return c.text('Failed to create user directory', 500);
        }

        await createLog({
            title: "Criação de conta",
            description: `Usuário ${username} criado com sucesso`,
            principalId: user.id,
        })
        
        return c.json(user, 201)

    } catch (error) {
        console.error(error)
        return c.text('Internal Server Error', 500)
    }
}