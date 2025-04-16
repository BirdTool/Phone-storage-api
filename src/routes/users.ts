import { Hono } from "hono";
import { postUser } from "src/controllers/users/postUser";

const users = new Hono();

users.get('/'); // retorna os usuários
users.get('/:id'); // retorna um usuário
users.post('/', postUser); // cria um usuário
users.put('/:id'); // atualiza um usuário
users.delete('/:id'); // deleta um usuário

export default users;