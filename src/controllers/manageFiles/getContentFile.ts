import { PrismaClient } from '#prisma';
import { Context } from 'hono';
import fs from 'fs';
import path from 'path';
import mime from 'mime-types';
import { createLog } from 'src/utils/log';

const prisma = new PrismaClient();

export const getContentFile = async (c: Context) => {
    try {
        const { id } = c.get('user') as { id: number };
        const { uuid } = await prisma.user.findUnique({
            where: { id },
            select: { uuid: true },
        }) as { uuid: string };

        if (!uuid) {
            return c.text('User does not exist', 404);
        }

        const requestedPath = c.req.param('path') || '';

        // Normaliza o caminho, preservando o último ponto (extensão do arquivo)
        let normalizedPath = '';
        if (requestedPath) {
            // Divide o caminho pelo último ponto para separar a extensão
            const lastDotIndex = requestedPath.lastIndexOf('.');
            if (lastDotIndex !== -1) {
                const pathWithoutExtension = requestedPath.slice(0, lastDotIndex);
                const extension = requestedPath.slice(lastDotIndex);
                // Substitui pontos por barras apenas na parte sem a extensão
                normalizedPath = pathWithoutExtension.replace(/\./g, '/') + extension;
            } else {
                // Caso não haja extensão, substitui todos os pontos por barras
                normalizedPath = requestedPath.replace(/\./g, '/');
            }
        }

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

        // Verifica se é um diretório
        if (stats.isDirectory()) {
            return c.text('Path is a directory, not a file', 400);
        }

        // Determina o tipo MIME do arquivo
        const mimeType = mime.lookup(targetPath) || 'application/octet-stream';

        await createLog({
            title: "Arquivo obtido",
            description: `Foi obtido o arquivo ${targetPath}`,
            principalId: id
        })
        
        // Para arquivos de texto, retorna o conteúdo como string em JSON
        if (mimeType.startsWith('text/') || mimeType === 'application/json') {
            const contentFile = fs.readFileSync(targetPath, 'utf-8');
            
            return c.json({
                path: normalizedPath,
                content: contentFile,
            });
        } else {
            // Para arquivos binários, retorna o Buffer com o tipo MIME correto
            const contentFile = fs.readFileSync(targetPath);
            c.header('Content-Type', mimeType);

            return c.body(contentFile);
        }
    } catch (error) {
        console.error('Error:', error);
        return c.text('Internal Server Error', 500);
    }
};