# ---------- Estagio 1: build do front (repositorio separado) ----------
FROM node:22 AS front
ARG FRONT_REPO_URL=https://github.com/GabrielPCdS/petcare-connect-web.git
ARG FRONT_REF=main
WORKDIR /web
RUN git clone --depth 1 --branch ${FRONT_REF} ${FRONT_REPO_URL} .
WORKDIR /web/petcare-web
RUN npm ci && npm run build

# ---------- Estagio 2: build da API com o front dentro ----------
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw -B -q dependency:go-offline
COPY src src
COPY --from=front /web/petcare-web/dist src/main/resources/static
RUN ./mvnw -B -q -DskipTests package

# ---------- Estagio 3: imagem final ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd -r -u 1001 app
COPY --from=build /app/target/petcare-api-*.jar app.jar
USER app
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 5000
ENTRYPOINT ["java", "-Duser.timezone=America/Sao_Paulo", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]