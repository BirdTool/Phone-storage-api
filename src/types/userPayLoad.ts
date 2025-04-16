export type UserPayload = {
    id: number;
    name: string;
    password: string;
    type: 'root' | 'user' | 'guest';
};