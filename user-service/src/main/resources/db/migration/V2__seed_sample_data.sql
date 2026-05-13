-- 1. Insertamos un Apoderado
INSERT INTO usuarios (rut, nombres, apellidos, email, password, rol)
VALUES ('11111111-1', 'Juan', 'Perez', 'juan@gmail.cl', '$2a$10$qu1Z0RcFzSi.9ScxZCwvb.lghWHn1K2b7ph39tD7tHffGBeH5/LT6', 'APODERADO');

-- 2. Insertamos Estudiantes (Asignándoles el apoderado_id = 1)
INSERT INTO usuarios (rut, nombres, apellidos, email, password, rol)
VALUES ('22222222-2', 'Pedrito', 'Perez', 'pedrito@alumno.colegioohiggins.cl', '$2a$10$qBqk61E3/83tld6dcBqNsOiIuzYMeghPFRQo/jYY59JyUXpWY95Ri', 'ESTUDIANTE');

INSERT INTO usuarios (rut, nombres, apellidos, email, password, rol)
VALUES ('33333333-3', 'Maria', 'Perez', 'maria@alumno.colegioohiggins.cl', '$2a$10$GUP1PM37uZtuGwaqF6zt7ue8AmWdkcKq9fcD1hxaAOpyh3cwPY7ZS', 'ESTUDIANTE');

-- 3. Insertamos un Docente
INSERT INTO usuarios (rut, nombres, apellidos, email, password, rol)
VALUES ('44444444-4', 'Carlos', 'Abarzua', 'carlosab@colegioohiggins.cl', '$2a$10$CiF/j9.G6sPnHIQ3d63pI.tjTA/EZRa4WI5omNSq4SRmVJxmaedda', 'DOCENTE');