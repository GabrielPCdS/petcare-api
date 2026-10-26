CREATE TABLE usuarios (
    id              BIGSERIAL PRIMARY KEY,
    nome            VARCHAR(120) NOT NULL,
    login           VARCHAR(60)  NOT NULL,
    email           VARCHAR(150) NOT NULL,
    senha_hash      VARCHAR(100) NOT NULL,
    perfil          VARCHAR(20)  NOT NULL,
    telefone        VARCHAR(30),
    endereco        VARCHAR(255),
    convenio_status VARCHAR(50),
    registro        VARCHAR(50),
    ativo           BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_usuarios_login UNIQUE (login),
    CONSTRAINT ck_usuarios_perfil CHECK (perfil IN ('Tutor', 'Veterinario', 'Admin'))
);

CREATE TABLE pets (
    id        BIGSERIAL PRIMARY KEY,
    nome      VARCHAR(100) NOT NULL,
    tipo      VARCHAR(50)  NOT NULL,
    raca      VARCHAR(100),
    idade     INTEGER,
    peso      NUMERIC(6,2),
    tutor_id  BIGINT       NOT NULL REFERENCES usuarios (id),
    criado_em TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX ix_pets_tutor ON pets (tutor_id);

CREATE TABLE consultas (
    id             BIGSERIAL PRIMARY KEY,
    pet_id         BIGINT      NOT NULL REFERENCES pets (id) ON DELETE CASCADE,
    veterinario_id BIGINT      NOT NULL REFERENCES usuarios (id),
    data           DATE        NOT NULL,
    hora           TIME        NOT NULL,
    status         VARCHAR(20) NOT NULL DEFAULT 'Marcada',
    observacoes    TEXT,
    CONSTRAINT ck_consultas_status CHECK (status IN ('Marcada', 'Realizada', 'Cancelada', 'NaoCompareceu'))
);
CREATE INDEX ix_consultas_pet ON consultas (pet_id);
CREATE INDEX ix_consultas_vet ON consultas (veterinario_id);
CREATE INDEX ix_consultas_vet_data_hora ON consultas (veterinario_id, data, hora);
CREATE UNIQUE INDEX uk_consultas_horario_ativo
    ON consultas (veterinario_id, data, hora)
    WHERE status <> 'Cancelada';

CREATE TABLE vacinas (
    id             BIGSERIAL PRIMARY KEY,
    pet_id         BIGINT       NOT NULL REFERENCES pets (id) ON DELETE CASCADE,
    nome           VARCHAR(120) NOT NULL,
    data_aplicacao DATE,
    observacao     TEXT,
    aplicada       BOOLEAN      NOT NULL DEFAULT FALSE
);
CREATE INDEX ix_vacinas_pet ON vacinas (pet_id);

CREATE TABLE historico (
    id          BIGSERIAL PRIMARY KEY,
    pet_id      BIGINT NOT NULL REFERENCES pets (id) ON DELETE CASCADE,
    descricao   TEXT   NOT NULL,
    data_evento DATE   NOT NULL
);
CREATE INDEX ix_historico_pet ON historico (pet_id);

CREATE TABLE relatorios (
    id            BIGSERIAL PRIMARY KEY,
    pet_id        BIGINT      NOT NULL REFERENCES pets (id) ON DELETE CASCADE,
    texto         TEXT        NOT NULL,
    status        VARCHAR(20) NOT NULL DEFAULT 'Disponivel',
    atualizado_em TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_relatorios_pet UNIQUE (pet_id)
);

CREATE TABLE avisos (
    id             BIGSERIAL PRIMARY KEY,
    titulo         VARCHAR(150) NOT NULL,
    texto          TEXT         NOT NULL,
    tipo           VARCHAR(20)  NOT NULL,
    tutor_id       BIGINT REFERENCES usuarios (id),
    pet_id         BIGINT REFERENCES pets (id) ON DELETE SET NULL,
    veterinario_id BIGINT REFERENCES usuarios (id),
    autor_id       BIGINT REFERENCES usuarios (id),
    criado_em      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_avisos_tipo CHECK (tipo IN ('geral', 'consulta', 'pet', 'tutor', 'vet'))
);
CREATE INDEX ix_avisos_tutor ON avisos (tutor_id);
CREATE INDEX ix_avisos_vet ON avisos (veterinario_id);
CREATE INDEX ix_avisos_autor ON avisos (autor_id);

CREATE TABLE aviso_leitura (
    aviso_id   BIGINT NOT NULL REFERENCES avisos (id) ON DELETE CASCADE,
    usuario_id BIGINT NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    PRIMARY KEY (aviso_id, usuario_id)
);
CREATE INDEX ix_aviso_leitura_usuario ON aviso_leitura (usuario_id);