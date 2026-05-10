-- Contraseña para todos los usuarios: MiPassword123!

-- 1. Insertamos un Apoderado
INSERT INTO usuarios (rut, nombres, apellidos, email, password, rol)
VALUES ('11111111-1', 'Juan', 'Perez', 'juan@gmail.cl', 'MiPassword123!.', 'APODERADO');

-- 2. Insertamos Estudiantes (Asignándoles el apoderado_id = 1)
INSERT INTO usuarios (rut, nombres, apellidos, email, password, rol)
VALUES ('22222222-2', 'Pedrito', 'Perez', 'pedrito@alumno.colegioohiggins.cl', 'MiPassword123!.', 'ESTUDIANTE');

INSERT INTO usuarios (rut, nombres, apellidos, email, password, rol)
VALUES ('33333333-3', 'Maria', 'Perez', 'maria@alumno.colegioohiggins.cl', 'MiPassword123!.', 'ESTUDIANTE');

-- 3. Insertamos un Docente
INSERT INTO usuarios (rut, nombres, apellidos, email, password, rol)
VALUES ('44444444-4', 'Carlos', 'Abarzua', 'carlosab@colegioohiggins.cl', 'MiPassword123!.', 'DOCENTE');