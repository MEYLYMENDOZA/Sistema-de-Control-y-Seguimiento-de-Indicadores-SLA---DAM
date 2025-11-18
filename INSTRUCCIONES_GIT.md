# Instrucciones para aplicar .gitignore y limpiar el repositorio

## 1. Ver el estado actual del repositorio
```powershell
cd "D:\REPOS\Sistema de control"
git status
```

## 2. Añadir el archivo .gitignore
```powershell
git add .gitignore
```

## 3. Remover archivos que ahora deben ser ignorados del índice de Git
**Nota:** Estos comandos NO borran los archivos de tu disco, solo los quitan del control de versiones.

```powershell
# Remover carpeta .idea (configuración de Android Studio)
git rm -r --cached .idea 2>$null

# Remover archivo google-services.json si no quieres que esté en el repo
# (OPCIONAL - comenta esta línea si quieres mantenerlo en el repo)
git rm --cached app/google-services.json 2>$null

# Remover carpeta build (archivos generados)
git rm -r --cached build 2>$null
git rm -r --cached app/build 2>$null
git rm -r --cached .gradle 2>$null

# Remover archivos locales
git rm --cached local.properties 2>$null
```

## 4. Hacer commit de los cambios
```powershell
git add .
git commit -m "Add .gitignore and remove build artifacts from version control"
```

## 5. Push al repositorio remoto (si tienes uno configurado)
```powershell
git push
```

## Pregunta frecuente: ¿Se borrarán archivos del historial?

**Respuesta:** NO. Los comandos anteriores solo quitan los archivos del índice **a partir de ahora**. 
Si esos archivos ya fueron comiteados antes, seguirán en el historial de Git.

### Para limpiar el historial completamente (AVANZADO - usa con precaución):
Si necesitas eliminar archivos sensibles (como `google-services.json`) del historial completo:

```powershell
# ADVERTENCIA: Esto reescribe el historial. Solo hazlo si es necesario.
# Todos los colaboradores tendrán que hacer un fresh clone.

# Opción 1: Usar git filter-repo (recomendado, requiere instalación)
# Instalar: pip install git-filter-repo
git filter-repo --path app/google-services.json --invert-paths

# Opción 2: Usar BFG Repo-Cleaner (más fácil)
# Descargar de: https://reclaimtheweb.org/bfg-repo-cleaner/
java -jar bfg.jar --delete-files google-services.json
git reflog expire --expire=now --all
git gc --prune=now --aggressive

# Después de limpiar el historial:
git push --force
```

**IMPORTANTE:** Solo ejecuta la limpieza del historial si tienes archivos sensibles que fueron comiteados por error. 
Para uso normal, los pasos 1-5 son suficientes.

