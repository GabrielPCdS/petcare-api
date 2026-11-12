-- V3: triggers e funcao em PL/pgSQL

CREATE OR REPLACE FUNCTION fn_relatorios_atualizado_em()
RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    NEW.atualizado_em := (now() AT TIME ZONE 'America/Sao_Paulo');
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_relatorios_atualizado_em
    BEFORE UPDATE ON relatorios
    FOR EACH ROW EXECUTE FUNCTION fn_relatorios_atualizado_em();

CREATE OR REPLACE FUNCTION fn_consultas_valida_veterinario()
RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE v_perfil varchar(20);
BEGIN
    SELECT perfil INTO v_perfil FROM usuarios WHERE id = NEW.veterinario_id;
    IF v_perfil IS DISTINCT FROM 'Veterinario' THEN
        RAISE EXCEPTION 'veterinario_id % nao pertence a um usuario com perfil Veterinario', NEW.veterinario_id
            USING ERRCODE = '23514';
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_consultas_valida_veterinario
    BEFORE INSERT OR UPDATE OF veterinario_id ON consultas
    FOR EACH ROW EXECUTE FUNCTION fn_consultas_valida_veterinario();

CREATE OR REPLACE FUNCTION fn_pets_valida_tutor()
RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE v_perfil varchar(20);
BEGIN
    SELECT perfil INTO v_perfil FROM usuarios WHERE id = NEW.tutor_id;
    IF v_perfil IS DISTINCT FROM 'Tutor' THEN
        RAISE EXCEPTION 'tutor_id % nao pertence a um usuario com perfil Tutor', NEW.tutor_id
            USING ERRCODE = '23514';
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_pets_valida_tutor
    BEFORE INSERT OR UPDATE OF tutor_id ON pets
    FOR EACH ROW EXECUTE FUNCTION fn_pets_valida_tutor();

CREATE OR REPLACE FUNCTION fn_consultas_realizada_historico()
RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    INSERT INTO historico (pet_id, descricao, data_evento)
    VALUES (NEW.pet_id,
            'Consulta realizada em ' || to_char(NEW.data, 'DD/MM/YYYY')
                || ' as ' || to_char(NEW.hora, 'HH24:MI') || '.',
            NEW.data);
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_consultas_realizada_historico
    AFTER UPDATE OF status ON consultas
    FOR EACH ROW
    WHEN (OLD.status IS DISTINCT FROM NEW.status AND NEW.status = 'Realizada')
    EXECUTE FUNCTION fn_consultas_realizada_historico();

CREATE OR REPLACE FUNCTION fn_horarios_livres(p_veterinario_id bigint, p_data date)
RETURNS TABLE (horario time) LANGUAGE sql STABLE AS $$
    SELECT g::time AS horario
    FROM generate_series(timestamp '2000-01-01 08:00', timestamp '2000-01-01 17:30',
                         interval '30 minutes') AS g
    WHERE NOT EXISTS (
        SELECT 1 FROM consultas c
        WHERE c.veterinario_id = p_veterinario_id
          AND c.data = p_data
          AND c.hora = g::time
          AND c.status <> 'Cancelada')
    ORDER BY 1;
$$;

ALTER TABLE pets ADD CONSTRAINT ck_pets_idade CHECK (idade >= 0);
ALTER TABLE pets ADD CONSTRAINT ck_pets_peso  CHECK (peso >= 0);