# Sports_Menagement_Platform

**Sports Management Platform** — современное веб-приложение для организации, управления и проведения спортивных турниров, команд и событий.

## Основные возможности

- Регистрация и аутентификация пользователей
- Профили игроков и организаторов
- Создание и управление командами и оргкомитетами
- Проведение турниров по разным видам спорта (футбол, баскетбол, хоккей, волейбол)
- Гибкая система этапов, групп, плей-офф
- Управление матчами, результатами, судейством
- Рейтинг команд, заявки на участие, автоматизация расписания
- Современный дизайн на Bootstrap 5

## Технологии

- Java 17+, Spring Boot, Spring Security, Spring Data JPA
- PostgreSQL
- Thymeleaf (шаблоны)
- Bootstrap 5, Bootstrap Icons
- Docker, Docker Compose

## Быстрый старт

### 1. Клонируйте репозиторий

```bash
git clone https://github.com/your-username/Sports_Menagement_Platform.git
cd Sports_Menagement_Platform
```

### 2. Запустите проект через Docker Compose

```bash
docker-compose up --build
```

Это поднимет:
- Базу данных PostgreSQL
- Само приложение Spring Boot

> **Все параметры подключения к базе данных и другим сервисам уже указаны в `src/main/resources/application.properties`.**

### 3. Откройте в браузере

Перейдите по адресу: [http://localhost:8080](http://localhost:8080)

## Локальный запуск без Docker

1. Установите PostgreSQL и создайте базу данных.
2. Убедитесь, что параметры подключения к БД указаны в `application.properties`.
3. Соберите и запустите проект:

```bash
./mvnw spring-boot:run
```

## Структура проекта

- `src/main/java` — основной код (контроллеры, сервисы, модели)
- `src/main/resources/templates` — HTML-шаблоны (Thymeleaf)
- `src/main/resources/static` — статика (css, js, изображения)
- `docker-compose.yml` — запуск через Docker
- `README.md` — этот файл

---

