import { PrismaClient } from "#prisma";

const prisma = new PrismaClient()

interface Log {
    title: string;
    description: string;
    principalId: number;
    involveds?: number[];
    level?: 'info' | 'warn' | 'error' | 'sub'
}

export async function createLog({ title, description, principalId, involveds, level = 'info' }: Log) {
    try {
        await prisma.logs.create({
            data: {
                title,
                description,
                principalId,
                involveds: JSON.stringify(involveds),
                level
            }
        })
    } catch (error) {
        throw new Error(`Error creating log: ${error}`)
    }
}