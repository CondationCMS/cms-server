import { cp, mkdir } from "node:fs/promises";
import path from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));

const source = path.resolve(__dirname, "../dist/bootstrap-condation.css");
const targetDir = path.resolve(__dirname, "../../resources/manager/bootstrap");
const target = path.join(targetDir, "bootstrap-condation.min.css");

await mkdir(targetDir, { recursive: true });
await cp(source, target);

console.log(`Copied ${source} to ${target}`);
