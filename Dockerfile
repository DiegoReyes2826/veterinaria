FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY . .

# ✅ Dar permisos de ejecución al script mvnw
RUN chmod +x mvnw

RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/veterinaria-1.jar app.jar
ENV PORT=8085
EXPOSE 8085
CMD ["java", "-jar", "app.jar"]
