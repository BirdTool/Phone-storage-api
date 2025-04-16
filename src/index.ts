import { Hono } from 'hono'
import manageFiles from './routes/manageFiles'
import users from './routes/users'
import { authMiddleware } from './authentication/auth'
import login from './routes/login'
import logs from './routes/logs'

const app = new Hono()

app.get('/', (c) => {
  return c.text('Hello Hono!')
})

const privateRoutes = new Hono()

privateRoutes.use("*", authMiddleware);
privateRoutes.route('/files', manageFiles)

app.route("/private", privateRoutes);
app.route('/users', users)
app.route('/login', login)
app.route('/logs', logs)

export default app
