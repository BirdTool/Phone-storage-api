import { PrismaClient } from '#prisma'
import { Context } from 'hono'

const prisma = new PrismaClient()

export const getLogs = async (c: Context) => {
    try {
        const targets = c.req.param('principalid');
        const involveds = c.req.param('involvedsids')

        if (targets === 'all') {
            const logs = await prisma.logs.findMany();

            return c.json(logs)
        } else if (targets) {
            const id = Number(targets)

            const logs = await prisma.logs.findMany({
                where: { principalId: id }
            })

            return c.json(logs)
        } else if (involveds) {
            const targetsIds = involveds.split(',').map(Number)

            const logs = await prisma.logs.findMany({
                where: { involveds }
            })

            const filteredLogs = logs.map(log => ({
                ...log,
                involveds: JSON.parse(log.involveds as string)
            })).filter(log => 
                log.involveds.some((id: number) => targetsIds.includes(id))
            );

            if (filteredLogs.length === 0) {
                return c.json({error: "logs not found"}, 404)
            }

            return c.json(filteredLogs);
        } else {
            return c.json({}, 402)
        }
    } catch (error) {
        console.error(error)
        return c.text('Internal Server Error', 500)
    }
}