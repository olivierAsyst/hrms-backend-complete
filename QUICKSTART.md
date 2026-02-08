# 🚀 Guide de Démarrage Rapide - HRMS Backend

## Option 1: Démarrage avec Docker (Recommandé)

### Prérequis
- Docker et Docker Compose installés

### Étapes

1. **Cloner le projet**
```bash
git clone <repository-url>
cd hrms-backend
```

2. **Démarrer avec Docker Compose**
```bash
docker-compose up -d
```

Cela va :
- Créer et démarrer PostgreSQL
- Créer et démarrer l'application Spring Boot
- Initialiser la base de données avec les rôles et permissions

3. **Vérifier que tout fonctionne**
```bash
curl http://localhost:8080/actuator/health
```

4. **Accéder à Swagger**
Ouvrez votre navigateur : http://localhost:8080/swagger-ui.html

5. **Créer votre premier utilisateur**
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "email": "admin@company.com",
    "password": "Admin@123",
    "firstName": "Admin",
    "lastName": "User",
    "roles": ["ROLE_ADMIN"]
  }'
```

6. **Se connecter**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "Admin@123"
  }'
```

Copiez le token reçu.

7. **Créer un employé**
```bash
curl -X POST http://localhost:8080/api/v1/employees \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer VOTRE_TOKEN_ICI" \
  -d '{
    "employeeNumber": "EMP001",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@company.com",
    "phoneNumber": "+1234567890",
    "jobTitle": "Software Engineer",
    "status": "ACTIVE",
    "hireDate": "2024-01-01",
    "salary": 75000.00,
    "currency": "USD",
    "skills": ["Java", "Spring Boot"]
  }'
```

## Option 2: Démarrage Manuel

### Prérequis
- Java 17+
- Maven 3.8+
- PostgreSQL 14+

### Étapes

1. **Installer PostgreSQL**
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install postgresql postgresql-contrib

# macOS
brew install postgresql@14
```

2. **Créer la base de données**
```bash
sudo -u postgres psql
CREATE DATABASE hrms_db;
CREATE USER hrms_user WITH ENCRYPTED PASSWORD 'postgres';
GRANT ALL PRIVILEGES ON DATABASE hrms_db TO hrms_user;
\q
```

3. **Initialiser la base**
```bash
psql -U hrms_user -d hrms_db -f src/main/resources/db-init.sql
```

4. **Configurer l'application**
Éditer `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/hrms_db
    username: hrms_user
    password: postgres
```

5. **Compiler et démarrer**
```bash
mvn clean install
mvn spring-boot:run
```

6. **Tester**
```bash
curl http://localhost:8080/actuator/health
```

## 🎯 Prochaines étapes

1. **Explorer l'API avec Swagger**
   - URL: http://localhost:8080/swagger-ui.html

2. **Créer un département**
```bash
curl -X POST http://localhost:8080/api/v1/departments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer VOTRE_TOKEN" \
  -d '{
    "name": "Engineering",
    "code": "ENG",
    "description": "Engineering Department",
    "location": "Building A",
    "active": true
  }'
```

3. **Rechercher des employés**
```bash
# Recherche simple
curl -X GET "http://localhost:8080/api/v1/employees?page=0&size=10" \
  -H "Authorization: Bearer VOTRE_TOKEN"

# Recherche avancée
curl -X GET "http://localhost:8080/api/v1/employees/search?firstName=John&status=ACTIVE" \
  -H "Authorization: Bearer VOTRE_TOKEN"

# Full-text search
curl -X GET "http://localhost:8080/api/v1/employees/fulltext-search?q=engineer" \
  -H "Authorization: Bearer VOTRE_TOKEN"
```

4. **Consulter les logs d'audit**
```bash
curl -X GET "http://localhost:8080/api/v1/audit?page=0&size=20" \
  -H "Authorization: Bearer VOTRE_TOKEN"
```

## 🔧 Configuration avancée

### Variables d'environnement

Pour la production, utilisez des variables d'environnement :

```bash
export JWT_SECRET="votre-secret-tres-long-et-securise-256-bits-minimum"
export DB_PASSWORD="votre-mot-de-passe-securise"
export SPRING_PROFILES_ACTIVE="prod"
```

### Profils Spring

Créez `application-prod.yml` pour la production :

```yaml
spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
  jpa:
    show-sql: false
    hibernate:
      ddl-auto: validate

jwt:
  secret: ${JWT_SECRET}
  expiration: 3600000  # 1 heure en prod

logging:
  level:
    root: WARN
    com.company.hrms: INFO
```

Démarrer avec le profil prod :
```bash
java -jar target/hrms-backend-1.0.0.jar --spring.profiles.active=prod
```

## 📊 Monitoring

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

### Métriques
```bash
curl http://localhost:8080/actuator/metrics
```

### Info
```bash
curl http://localhost:8080/actuator/info
```

## 🐛 Dépannage courant

### Port déjà utilisé
```yaml
# Dans application.yml
server:
  port: 8081
```

### Connexion PostgreSQL refusée
```bash
# Vérifier que PostgreSQL est démarré
sudo systemctl status postgresql
sudo systemctl start postgresql

# Vérifier la connexion
psql -U hrms_user -d hrms_db -h localhost
```

### Erreur JWT
Assurez-vous que votre JWT_SECRET fait au moins 256 bits (32 caractères).

## 📚 Ressources

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs JSON**: http://localhost:8080/api-docs
- **Health**: http://localhost:8080/actuator/health

## 🎉 Félicitations !

Vous avez maintenant un système de gestion RH professionnel et sécurisé opérationnel !

Pour plus de détails, consultez le [README.md](README.md) complet.
