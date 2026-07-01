import { cp, mkdir, rm } from "node:fs/promises";
import path from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));

const sourceRoot = path.resolve(__dirname, "../dist");
const targetRoot = path.resolve(__dirname, "../../resources/manager");

const folders = ["actions", "js", "public"];

for (const folder of folders) {
  const source = path.join(sourceRoot, folder);
  const target = path.join(targetRoot, folder);

  await rm(target, { recursive: true, force: true });
  await mkdir(targetRoot, { recursive: true });
  await cp(source, target, { recursive: true });

  console.log(`Copied ${source} to ${target}`);
}

// dist erst löschen, wenn alles erfolgreich kopiert wurde
await rm(sourceRoot, { recursive: true, force: true });

console.log(`Deleted ${sourceRoot}`);