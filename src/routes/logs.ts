import { Hono } from "hono";
import { getLogs } from "src/controllers/logs/getLogs";

const logs = new Hono();

logs.get('/:principalid', getLogs)
logs.get('/involveds/:involvedsids', getLogs)

export default logs;