#!/usr/bin/env bash
set -euo pipefail

# Seeds a demo exam configuration and prints IDs to use in the frontend.
# Requirements:
# - Backend running (default: http://localhost:8080)
# - Python 3 available
#
# Optional env vars:
#   API_BASE_URL   (default: http://localhost:8080)
#   ADMIN_DOCUMENTO (default: ADMIN)
#   ADMIN_CLAVE     (default: Wisegrade2026)
#   DEMO_USER_CLAVE (default: Wisegrade2026)
#   MATERIA_NOMBRE (default: Backend I)
#   MOMENTO_NOMBRE (default: Momento 1)
#   PERIODO_ANIO   (default: 2026)
#   PERIODO_NOMBRE (default: Periodo I)
#   BANCO_PREGUNTAS (default: 10)
#   INTENTO_CANTIDAD (default: 5)

API_BASE_URL="${API_BASE_URL:-http://localhost:8080}"
ADMIN_DOCUMENTO="${ADMIN_DOCUMENTO:-ADMIN}"
ADMIN_CLAVE="${ADMIN_CLAVE:-Wisegrade2026}"
DEMO_USER_CLAVE="${DEMO_USER_CLAVE:-Wisegrade2026}"
MATERIA_NOMBRE="${MATERIA_NOMBRE:-Backend I}"
MOMENTO_NOMBRE="${MOMENTO_NOMBRE:-Momento 1}"
PERIODO_ANIO="${PERIODO_ANIO:-2026}"
PERIODO_NOMBRE="${PERIODO_NOMBRE:-Periodo I}"
BANCO_PREGUNTAS="${BANCO_PREGUNTAS:-10}"
INTENTO_CANTIDAD="${INTENTO_CANTIDAD:-5}"

python3 - <<'PY'
import json
import http.cookiejar
import os
import time
import urllib.error
import urllib.request

BASE = os.environ.get('API_BASE_URL', 'http://localhost:8080').rstrip('/')
ADMIN_DOCUMENTO = os.environ.get('ADMIN_DOCUMENTO', 'ADMIN')
ADMIN_CLAVE = os.environ.get('ADMIN_CLAVE', 'Wisegrade2026')
DEMO_USER_CLAVE = os.environ.get('DEMO_USER_CLAVE', 'Wisegrade2026')
MATERIA_NOMBRE = os.environ.get('MATERIA_NOMBRE', 'Backend I')
MOMENTO_NOMBRE = os.environ.get('MOMENTO_NOMBRE', 'Momento 1')
PERIODO_ANIO = int(os.environ.get('PERIODO_ANIO', '2026'))
PERIODO_NOMBRE = os.environ.get('PERIODO_NOMBRE', 'Periodo I')
BANCO_PREGUNTAS = int(os.environ.get('BANCO_PREGUNTAS', '10'))
INTENTO_CANTIDAD = int(os.environ.get('INTENTO_CANTIDAD', '5'))


# Keep a cookie jar so session auth works across requests.
COOKIE_JAR = http.cookiejar.CookieJar()
OPENER = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(COOKIE_JAR))


def http_json(method: str, path: str, payload=None):
    url = BASE + path
    data = None
    headers = {'Accept': 'application/json'}
    if payload is not None:
        data = json.dumps(payload).encode('utf-8')
        headers['Content-Type'] = 'application/json'
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with OPENER.open(req, timeout=8) as resp:
            body = resp.read().decode('utf-8')
            return resp.status, json.loads(body) if body else None
    except urllib.error.HTTPError as e:
        body = e.read().decode('utf-8')
        try:
            parsed = json.loads(body) if body else None
        except Exception:
            parsed = body
        raise RuntimeError(f'{method} {path} -> HTTP {e.code}: {parsed}')
    except Exception as e:
        raise RuntimeError(f'{method} {path} -> {e}')


def pick_id(items, pred, label):
    for it in items:
        if pred(it):
            return int(it['id'])
    raise RuntimeError(f'No se encontró {label}. Respuesta: {items}')

# Login as admin (session cookie)
http_json('POST', '/api/auth/login', {
    'documento': ADMIN_DOCUMENTO,
    'clave': ADMIN_CLAVE,
})


# Resolve seeded IDs
_, periodos = http_json('GET', '/api/periodos')
_, materias = http_json('GET', '/api/materias')
_, momentos = http_json('GET', '/api/momentos')

periodo_id = pick_id(
    periodos,
    lambda p: p.get('anio') == PERIODO_ANIO and p.get('nombre') == PERIODO_NOMBRE,
    f'Periodo {PERIODO_ANIO} {PERIODO_NOMBRE}',
)

materia_id = pick_id(
    materias,
    lambda m: m.get('nombre') == MATERIA_NOMBRE,
    f'Materia {MATERIA_NOMBRE}',
)

momento_id = pick_id(
    momentos,
    lambda m: m.get('nombre') == MOMENTO_NOMBRE,
    f'Momento {MOMENTO_NOMBRE}',
)

suffix = str(int(time.time()))

# Create docente
_, docente = http_json('POST', '/api/docentes', {
    'nombres': 'Profe',
    'apellidos': f'Demo-{suffix}',
    'documento': f'DEMO-DOC-{suffix}',
    'activo': True,
})
docente_id = int(docente['id'])
docente_documento = str(docente['documento'])

# Create estudiante
_, estudiante = http_json('POST', '/api/estudiantes', {
    'nombres': 'Estudiante',
    'apellidos': f'Demo-{suffix}',
    'documento': f'DEMO-EST-{suffix}',
    'activo': True,
})
estudiante_id = int(estudiante['id'])
estudiante_documento = str(estudiante['documento'])

# Create auth users linked to the created personas
http_json('POST', '/api/auth/users', {
    'documento': docente_documento,
    'clave': DEMO_USER_CLAVE,
    'rol': 'DOCENTE',
    'docenteId': docente_id,
    'estudianteId': None,
    'activo': True,
})

http_json('POST', '/api/auth/users', {
    'documento': estudiante_documento,
    'clave': DEMO_USER_CLAVE,
    'rol': 'ESTUDIANTE',
    'docenteId': None,
    'estudianteId': estudiante_id,
    'activo': True,
})

# Associate docente to materia
http_json('PUT', f'/api/materias/{materia_id}/docentes/{docente_id}')

# Load bank with BANCO_PREGUNTAS questions
preguntas = []
for i in range(1, BANCO_PREGUNTAS + 1):
    preguntas.append({
        'enunciado': f'Pregunta {i} (demo seed)',
        'opcionA': 'A',
        'opcionB': 'B',
        'opcionC': 'C',
        'opcionD': 'D',
        'correcta': 'A',
        'explicacion': 'La respuesta correcta es A (demo). Aquí va la sustentación/definición de por qué A es correcta.',
    })

_, bank_res = http_json('POST', '/api/examenes/banco', {
    'periodoId': periodo_id,
    'materiaId': materia_id,
    'momentoId': momento_id,
    'docenteResponsableId': docente_id,
    'preguntas': preguntas,
})

# Start attempt (optional, but helpful to confirm everything works)
_, intento = http_json('POST', '/api/intentos/iniciar', {
    'periodoId': periodo_id,
    'materiaId': materia_id,
    'momentoId': momento_id,
    'docenteResponsableId': docente_id,
    'estudianteId': estudiante_id,
    'cantidad': INTENTO_CANTIDAD,
})

print('OK: configuración creada')
print('API_BASE_URL=', BASE)
print('--- IDs para pegar en el frontend ---')
print('periodoId=', periodo_id)
print('materiaId=', materia_id)
print('momentoId=', momento_id)
print('docenteResponsableId=', docente_id)
print('estudianteId=', estudiante_id)
print('cantidad=', INTENTO_CANTIDAD)
print('--- Credenciales demo ---')
print('ADMIN_DOCUMENTO=', ADMIN_DOCUMENTO)
print('ADMIN_CLAVE=', ADMIN_CLAVE)
print('DOCENTE_DOCUMENTO=', docente_documento)
print('DEMO_USER_CLAVE=', DEMO_USER_CLAVE)
print('ESTUDIANTE_DOCUMENTO=', estudiante_documento)
print('DEMO_USER_CLAVE=', DEMO_USER_CLAVE)
print('--- Debug ---')
print('examenId=', int(bank_res.get('examenId')))
print('intentoId=', int(intento.get('intentoId')))
PY