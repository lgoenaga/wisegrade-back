# Wisegrade: acceso por red (LAN) y cambio de red

Este documento explica cómo exponer Wisegrade a otros equipos en la misma red local y qué cambiar cuando estés en otra red (por ejemplo, otra subred con los estudiantes).

## 1) Datos que necesitas (servidor)

En la máquina servidor (Linux Mint):

1. Obtén la IP LAN actual:

   ```bash
   ip a
   ```

   Busca algo como `inet 192.168.X.Y/24` en tu interfaz Wi‑Fi/Ethernet.

2. Identifica la subred (CIDR). Ejemplos comunes:

- Si tu IP es `192.168.1.8/24`, la subred es `192.168.1.0/24`.
- Si tu IP es `192.168.10.25/24`, la subred es `192.168.10.0/24`.

## 2) Puertos que usa Wisegrade

- Frontend (Vite dev): `5173/tcp`
- Backend (Spring Boot): `8080/tcp`

## 3) Firewall (UFW) en Linux Mint

Si `ufw` está activo, permite los puertos solo desde la subred donde estarán los estudiantes.

1. Verifica estado:

```bash
sudo ufw status numbered
```

2. Permite acceso desde la subred de los estudiantes (reemplaza `192.168.1.0/24` por la subred real):

```bash
sudo ufw allow from 192.168.1.0/24 to any port 5173 proto tcp
sudo ufw allow from 192.168.1.0/24 to any port 8080 proto tcp
```

3. Confirma:

```bash
sudo ufw status numbered
```

## 4) Frontend: apuntar al backend correcto (evitar localhost del cliente)

Si el frontend apunta a `http://localhost:8080`, desde el equipo remoto va a intentar pegarle al backend del propio equipo remoto (no al servidor).

### Opción recomendada (archivo local)

En el servidor, crea/edita el archivo `frontend/.env.local` con:

```dotenv
VITE_API_BASE_URL=http://<IP_DEL_SERVIDOR>:8080
```

Ejemplo:

```dotenv
VITE_API_BASE_URL=http://192.168.1.8:8080
```

Luego reinicia el frontend (`npm run dev`) para que tome la variable.

## 5) Backend: permitir CORS para el origin del frontend

Como el frontend corre en `http://<IP_DEL_SERVIDOR>:5173`, el backend debe permitir ese origin (CORS) para que el navegador no bloquee las llamadas.

### Opción recomendada (sin tocar YAML): variable de entorno

Define `APP_CORS_ALLOWED_ORIGINS` al levantar el backend, por ejemplo:

```bash
export APP_CORS_ALLOWED_ORIGINS='http://localhost:5173,http://<IP_DEL_SERVIDOR>:5173'
./scripts/run-dev.sh
```

Ejemplo:

```bash
export APP_CORS_ALLOWED_ORIGINS='http://localhost:5173,http://192.168.1.8:5173'
./scripts/run-dev.sh
```

Alternativa: agregar esa variable en `backend/.env.local` (el script `./scripts/run-dev.sh` lo carga automáticamente). Asegúrate de NO borrar tus variables de DB.

## 6) Levantar los servidores

### Backend (Spring Boot)

Desde el servidor:

```bash
cd backend
./scripts/run-dev.sh
```

- Corre en `http://<IP_DEL_SERVIDOR>:8080`
- Endpoint de salud: `http://<IP_DEL_SERVIDOR>:8080/api/health`

### Frontend (Vite)

Desde el servidor:

```bash
cd frontend
npm install
npm run dev
```

El Vite debe mostrar algo como:

- `Local:   http://localhost:5173/`
- `Network: http://<IP_DEL_SERVIDOR>:5173/`

## 7) Pruebas rápidas (desde otro equipo)

En el equipo del estudiante (misma red):

1. Abre el frontend:

- `http://<IP_DEL_SERVIDOR>:5173`

2. Prueba el backend:

- `http://<IP_DEL_SERVIDOR>:8080/api/health`

Si `api/health` responde pero el frontend no carga, suele ser:

- Vite no está escuchando en red
- Firewall bloqueando `5173`

Si el frontend carga pero el login o llamadas fallan, suele ser:

- `VITE_API_BASE_URL` apunta a `localhost`
- CORS no incluye `http://<IP_DEL_SERVIDOR>:5173`

## 8) Cuando cambies de red

Cada vez que cambies a otra red, revisa y ajusta:

1. IP del servidor (puede cambiar): `ip a`
2. Subred nueva para UFW (por ejemplo `192.168.10.0/24`)
3. `VITE_API_BASE_URL` en `frontend/.env.local` con la nueva IP
4. `APP_CORS_ALLOWED_ORIGINS` con la nueva IP del frontend

Luego reinicia:

- Backend: detener y volver a correr `./scripts/run-dev.sh`
- Frontend: detener y volver a correr `npm run dev`
