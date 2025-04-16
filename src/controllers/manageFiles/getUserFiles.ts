import { PrismaClient } from '#prisma';
import { Context } from 'hono';
import fs from 'fs';
import path from 'path';
import { createLog } from 'src/utils/log';

const prisma = new PrismaClient();

interface FileNode {
    name: string;
    type: 'file' | 'directory';
    children?: FileNode[];
}

function buildFileTreeFromPath(startPath: string): FileNode[] {
    const entries = fs.readdirSync(startPath, { withFileTypes: true });

    return entries.map(entry => {
        const entryPath = path.join(startPath, entry.name);
        
        if (entry.isDirectory()) {
            return {
                name: entry.name,
                type: 'directory',
                children: buildFileTreeFromPath(entryPath)
            };
        } else {
            return {
                name: entry.name,
                type: 'file'
            };
        }
    });
}

export const getUserFiles = async (c: Context) => {
    try {
        const { id } = c.get("user") as { id: number };
        // Captura o caminho como uma string (ex.: "tests.more")
        let requestedPath = c.req.param('path') || '';

        const { uuid } = await prisma.user.findUnique({
            where: { id },
            select: { uuid: true }
        }) as { uuid: string };

        if (!uuid) {
            return c.text('User not found', 404);
        }

        // Converte o caminho com pontos em um caminho com barras (ex.: "tests.more" -> "tests/more")
        const normalizedPath = requestedPath ? requestedPath.replace(/\./g, '/') : '';

        const storagePath = path.resolve(__dirname, '../../storage', uuid);
        const targetPath = path.join(storagePath, normalizedPath);

        // Security check
        if (!targetPath.startsWith(storagePath)) {
            return c.text('Invalid path', 403);
        }

        if (!fs.existsSync(targetPath)) {
            return c.text('Path not found', 404);
        }

        const stats = fs.statSync(targetPath);

        await createLog({
            title: "Diretório do usuário obtido",
            description: `Foi obtido o diretório ${normalizedPath}`,
            principalId: id,
        })

        if (stats.isDirectory()) {
            const fileTree = buildFileTreeFromPath(targetPath);
            return c.json({
                path: normalizedPath, // Retorna o caminho com barras (ex.: "tests/more")
                contents: fileTree
            });
        } else {
            return c.json({
                path: normalizedPath,
                type: 'file',
                name: path.basename(targetPath),
                size: stats.size,
                modified: stats.mtime
            });
        }
    } catch (error) {
        console.error('Error:', error);
        return c.text('Internal Server Error', 500);
    }
};