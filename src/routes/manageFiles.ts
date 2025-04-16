import { Hono } from "hono";
import { getContentFile } from "src/controllers/manageFiles/getContentFile";
import { getUserFiles } from "src/controllers/manageFiles/getUserFiles";

const manageFiles = new Hono();

// Rota para arquivos do usuário com caminho dinâmico (ex.: tests.more)
manageFiles.get("/:path", getUserFiles);

manageFiles.get("/", getUserFiles);

manageFiles.get("/content/:path", getContentFile)

manageFiles.post("/:path"); // Adiciona arquivo

manageFiles.put("/:path"); // Atualiza arquivo

manageFiles.delete("/:path"); // Deleta arquivo

export default manageFiles;