# HRMS Backend - Système Professionnel de Gestion des Employés

## 📋 Table des matières
- [Vue d'ensemble](#vue-densemble)
- [Technologies utilisées](#technologies-utilisées)
- [Architecture](#architecture)
- [Fonctionnalités](#fonctionnalités)
- [Prérequis](#prérequis)
- [Installation](#installation)
- [Configuration](#configuration)
- [Exécution](#exécution)
- [API Documentation](#api-documentation)
- [Sécurité](#sécurité)
- [Tests](#tests)
- [Bonnes pratiques](#bonnes-pratiques)

## 🎯 Vue d'ensemble

HRMS Backend est un système professionnel de gestion des ressources humaines construit avec Spring Boot 3.2 et PostgreSQL. Il offre une solution complète pour gérer les employés, les départements, les utilisateurs, les rôles et les permissions avec une sécurité robuste basée sur JWT.

### Points forts
- ✅ Architecture propre et modulaire
- ✅ Sécurité JWT avec gestion des rôles et permissions (RBAC)
- ✅ Gestion des erreurs globale et professionnelle
- ✅ Audit trail complet
- ✅ Soft delete pour toutes les entités
- ✅ Optimistic locking pour la concurrence
- ✅ Utilisation avancée de PostgreSQL (JSONB, arrays, full-text search)
- ✅ Cache avec Spring Cache
- ✅ Documentation Swagger/OpenAPI
- ✅ Validation complète des données
- ✅ Pagination et tri
- ✅ Recherche avancée

## 🛠 Technologies utilisées

### Backend
- **Java 17** - Langage de programmation
- **Spring Boot 3.2.1** - Framework principal
- **Spring Security** - Authentification et autorisation
- **Spring Data JPA** - Accès aux données
- **PostgreSQL** - Base de données relationnelle
- **JWT (jjwt 0.12.3)** - Gestion des tokens
- **Lombok** - Réduction du boilerplate
- **MapStruct** - Mapping DTO/Entity
- **SpringDoc OpenAPI** - Documentation API

### Base de données
- **PostgreSQL 14+** avec fonctionnalités avancées :
  - JSONB pour les champs flexibles
  - Arrays pour les listes
  - Full-text search
  - Indexes optimisés
  - Triggers

## 🏗 Architecture

```
src/main/java/com/company/hrms/
├── config/              # Configuration (Security, JPA, OpenAPI)
│   ├── SecurityConfig.java
│   ├── JpaConfig.java
│   ├── AuditorAwareImpl.java
│   └── OpenApiConfig.java
├── controller/          # REST Controllers
│   ├── AuthController.java
│   ├── EmployeeController.java
│   ├── DepartmentController.java
│   └── UserController.java
├── dto/                 # Data Transfer Objects
│   ├── auth/
│   ├── employee/
│   ├── department/
│   └── common/
├── entity/              # Entités JPA
│   ├── BaseEntity.java
│   ├── User.java
│   ├── Employee.java
│   ├── Department.java
│   ├── Role.java
│   ├── Permission.java
│   └── AuditLog.java
├── repository/          # Repositories Spring Data JPA
│   ├── UserRepository.java
│   ├── EmployeeRepository.java
│   ├── DepartmentRepository.java
│   ├── RoleRepository.java
│   ├── PermissionRepository.java
│   └── AuditLogRepository.java
├── service/             # Business Logic
│   ├── AuthService.java
│   ├── EmployeeService.java
│   ├── DepartmentService.java
│   ├── UserService.java
│   └── audit/
│       └── AuditService.java
├── security/            # Sécurité JWT
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   ├── JwtAuthenticationEntryPoint.java
│   └── CustomUserDetailsService.java
├── exception/           # Gestion des exceptions
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   ├── DuplicateResourceException.java
│   ├── BadRequestException.java
│   └── UnauthorizedException.java
├── enums/               # Énumérations
│   ├── RoleType.java
│   ├── PermissionType.java
│   └── EmployeeStatus.java
└── HrmsBackendApplication.java
```

## ⚙️ Fonctionnalités

### 1. Authentification et Autorisation
- ✅ Enregistrement utilisateur
- ✅ Connexion avec JWT
- ✅ Gestion des rôles (SUPER_ADMIN, ADMIN, HR_MANAGER, MANAGER, EMPLOYEE)
- ✅ Permissions granulaires
- ✅ Verrouillage de compte après échecs de connexion
- ✅ Expiration de token configurable

### 2. Gestion des Employés
- ✅ CRUD complet
- ✅ Recherche avancée (multi-critères)
- ✅ Full-text search (PostgreSQL)
- ✅ Filtrage par département, statut, manager
- ✅ Pagination et tri
- ✅ Soft delete
- ✅ Gestion du statut (ACTIVE, INACTIVE, ON_LEAVE, TERMINATED, SUSPENDED)
- ✅ Historique des modifications
- ✅ Champs flexibles (JSONB) : address, emergency_contact, education, work_history
- ✅ Skills (PostgreSQL array)

### 3. Gestion des Départements
- ✅ CRUD complet
- ✅ Attribution de manager
- ✅ Gestion du budget
- ✅ Statistiques (nombre d'employés)

### 4. Audit et Traçabilité
- ✅ Enregistrement de toutes les actions
- ✅ Tracking des modifications (old/new values)
- ✅ Capture IP et User-Agent
- ✅ Historique complet par entité
- ✅ Logs de connexion/déconnexion

### 5. Sécurité
- ✅ Authentification JWT
- ✅ RBAC (Role-Based Access Control)
- ✅ Hachage de mots de passe (BCrypt)
- ✅ Protection CSRF
- ✅ CORS configuré
- ✅ Validation des entrées
- ✅ Gestion des erreurs sécurisée

### 6. Performance
- ✅ Cache Spring Cache
- ✅ Indexes PostgreSQL optimisés
- ✅ Optimistic locking
- ✅ Pagination
- ✅ Lazy loading

## 📦 Prérequis

- **Java 17** ou supérieur
- **Maven 3.8+**
- **PostgreSQL 14+**
- **Git**

## 🚀 Installation

### 1. Cloner le repository
```bash
git clone <repository-url>
cd hrms-backend
```

### 2. Configurer PostgreSQL

#### Installer PostgreSQL
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install postgresql postgresql-contrib

# macOS
brew install postgresql@14

# Windows
# Télécharger depuis https://www.postgresql.org/download/windows/
```

#### Créer la base de données
```bash
sudo -u postgres psql

# Dans psql
CREATE DATABASE hrms_db;
CREATE USER hrms_user WITH ENCRYPTED PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE hrms_db TO hrms_user;
\q
```

#### Exécuter le script d'initialisation
```bash
psql -U hrms_user -d hrms_db -f src/main/resources/db-init.sql
```

### 3. Configurer l'application

Éditer `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/hrms_db
    username: hrms_user
    password: your_password

jwt:
  secret: your-secret-key-min-256-bits-please-change-this-in-production
  expiration: 86400000 # 24 heures
```

⚠️ **Important** : En production, utilisez des variables d'environnement :
```bash
export JWT_SECRET=your-very-long-secret-key
export DB_PASSWORD=your-db-password
```

### 4. Compiler le projet
```bash
mvn clean install
```

## ▶️ Exécution

### Mode développement
```bash
mvn spring-boot:run
```

### Mode production
```bash
# Compiler le JAR
mvn clean package -DskipTests

# Exécuter
java -jar target/hrms-backend-1.0.0.jar
```

L'application démarre sur `http://localhost:8080`

## 📚 API Documentation

### Swagger UI
Une fois l'application démarrée, accédez à :
```
http://localhost:8080/swagger-ui.html
```

### Endpoints principaux

#### Authentication
```
POST /api/v1/auth/register    - Enregistrement
POST /api/v1/auth/login       - Connexion
POST /api/v1/auth/logout      - Déconnexion
```

#### Employees
```
GET    /api/v1/employees              - Liste paginée
GET    /api/v1/employees/{id}         - Détails d'un employé
GET    /api/v1/employees/search       - Recherche avancée
GET    /api/v1/employees/fulltext-search - Full-text search
POST   /api/v1/employees              - Créer un employé
PUT    /api/v1/employees/{id}         - Mettre à jour
PATCH  /api/v1/employees/{id}/status  - Changer le statut
DELETE /api/v1/employees/{id}         - Supprimer (soft delete)
```

#### Departments
```
GET    /api/v1/departments            - Liste des départements
GET    /api/v1/departments/{id}       - Détails d'un département
POST   /api/v1/departments            - Créer un département
PUT    /api/v1/departments/{id}       - Mettre à jour
DELETE /api/v1/departments/{id}       - Supprimer
```

### Exemples d'utilisation

#### 1. Enregistrement
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "email": "admin@company.com",
    "password": "Admin@123",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890",
    "roles": ["ROLE_ADMIN"]
  }'
```

#### 2. Connexion
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "Admin@123"
  }'
```

Réponse :
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "type": "Bearer",
    "id": 1,
    "username": "admin",
    "email": "admin@company.com",
    "firstName": "John",
    "lastName": "Doe",
    "roles": ["ROLE_ADMIN"],
    "permissions": ["EMPLOYEE_READ", "EMPLOYEE_CREATE", ...]
  }
}
```

#### 3. Créer un employé
```bash
curl -X POST http://localhost:8080/api/v1/employees \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "employeeNumber": "EMP001",
    "firstName": "Jane",
    "lastName": "Smith",
    "email": "jane.smith@company.com",
    "phoneNumber": "+1234567891",
    "dateOfBirth": "1990-05-15",
    "gender": "Female",
    "jobTitle": "Software Engineer",
    "departmentId": 1,
    "status": "ACTIVE",
    "hireDate": "2024-01-01",
    "salary": 75000.00,
    "currency": "USD",
    "employmentType": "Full-time",
    "skills": ["Java", "Spring Boot", "PostgreSQL"]
  }'
```

#### 4. Recherche avancée
```bash
curl -X GET "http://localhost:8080/api/v1/employees/search?firstName=Jane&status=ACTIVE&page=0&size=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### 5. Full-text search
```bash
curl -X GET "http://localhost:8080/api/v1/employees/fulltext-search?q=software+engineer&page=0&size=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 🔒 Sécurité

### Rôles et Permissions

#### ROLE_SUPER_ADMIN
- Toutes les permissions
- Gestion des rôles et permissions
- Accès système complet

#### ROLE_ADMIN
- Gestion des employés (CRUD)
- Gestion des départements (CRUD)
- Gestion des utilisateurs (lecture, création, mise à jour)
- Lecture des logs d'audit

#### ROLE_HR_MANAGER
- Gestion complète des employés
- Lecture des départements
- Lecture des logs d'audit

#### ROLE_MANAGER
- Lecture des employés
- Lecture des départements

#### ROLE_EMPLOYEE
- Lecture limitée

### Meilleures pratiques de sécurité

1. **Mots de passe forts** : Minimum 8 caractères, incluant majuscules, minuscules, chiffres et caractères spéciaux
2. **JWT Secret** : Utilisez un secret d'au moins 256 bits
3. **HTTPS** : En production, utilisez toujours HTTPS
4. **Variables d'environnement** : Ne jamais commiter les secrets
5. **Verrouillage de compte** : Après 5 tentatives échouées
6. **Expiration des tokens** : Configurable (défaut 24h)

## 🧪 Tests

### Exécuter les tests
```bash
mvn test
```

### Couverture des tests
```bash
mvn jacoco:report
```

## 📝 Bonnes pratiques implémentées

### 1. Architecture
- ✅ Séparation des couches (Controller, Service, Repository)
- ✅ DTOs pour les transferts de données
- ✅ Entities pour la persistance
- ✅ Exceptions personnalisées

### 2. Base de données
- ✅ Indexes optimisés
- ✅ Soft delete
- ✅ Audit trail
- ✅ Optimistic locking
- ✅ JSONB pour flexibilité
- ✅ Full-text search
- ✅ Triggers PostgreSQL

### 3. Sécurité
- ✅ JWT authentication
- ✅ RBAC (Role-Based Access Control)
- ✅ Validation des entrées
- ✅ Gestion sécurisée des erreurs
- ✅ Protection contre les injections SQL

### 4. Performance
- ✅ Cache
- ✅ Pagination
- ✅ Lazy loading
- ✅ Connection pooling

### 5. Observabilité
- ✅ Logging structuré
- ✅ Audit trail
- ✅ Actuator endpoints

## 🐛 Dépannage

### Problème de connexion à PostgreSQL
```bash
# Vérifier que PostgreSQL est démarré
sudo systemctl status postgresql

# Démarrer PostgreSQL
sudo systemctl start postgresql
```

### Erreur de port déjà utilisé
```bash
# Changer le port dans application.yml
server:
  port: 8081
```

### Problème de JWT
Vérifiez que votre JWT_SECRET fait au moins 256 bits (32 caractères)

## 📧 Support

Pour toute question ou problème :
- Email: support@yourcompany.com
- Documentation: [URL de la documentation]

## 📄 License

Ce projet est sous licence Apache 2.0

---

**Développé avec ❤️ par votre équipe**
