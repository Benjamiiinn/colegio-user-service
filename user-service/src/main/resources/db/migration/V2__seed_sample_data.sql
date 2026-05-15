-- 1. Insertamos un Administrador
-- Password: Admin123$
INSERT INTO usuarios (rut, nombres, apellidos, email, password, rol)
VALUES ('25041654-7', 'Admin', 'Administrador', 'admin@colegioohiggins.cl', '$2a$12$5LAX0oDiSiXgVmvEzi7tDOQDukLUq2DJzGhBjPdqu7XLckNbg1Qse', 'ADMIN');

-- 2. Insertamos un Apoderado
-- Password: Password123!
INSERT INTO usuarios (rut, nombres, apellidos, email, password, rol)
VALUES ('9829566-6', 'Juan', 'Perez', 'juan@gmail.cl', '$2a$12$gRoqq1kP3cbM7MYa8Jvy0uXFnUkNsCnd9KNwNr/e1GCBMyHC1nsGu', 'APODERADO');

-- 3. Insertamos Estudiantes
-- Password: Estudiante456@
INSERT INTO usuarios (rut, nombres, apellidos, email, password, rol)
VALUES ('21719226-9', 'Benjamin', 'Gonzalez', 'benja@alumnos.colegioohiggins.cl', '$2a$12$eqqE0/bSoGsSa1upWUH7D.FiDPrxJ0IApWsuJcssyCnWFn74sk7za', 'ESTUDIANTE');

-- Password: Estudiante789@
INSERT INTO usuarios (rut, nombres, apellidos, email, password, rol)
VALUES ('21624212-2', 'Susana', 'Castle', 'susana@alumnos.colegioohiggins.cl', '$2a$12$H0QcbUhTxfclPmMuW9.YfeYGpP5KpoGVegPqW6Tj9UgZzuN/Jlque', 'ESTUDIANTE');

-- 4. Insertamos un Docente
-- Password: Docente321$
INSERT INTO usuarios (rut, nombres, apellidos, email, password, rol)
VALUES ('18221856-1', 'Carlos', 'Abarzua', 'carlosab@colegioohiggins.cl', '$2a$12$hKQFYKrETfBPdrhnwZbN/eT6N9FCo6MuDkhIfK4.UrD5voUtwYqNi', 'DOCENTE');