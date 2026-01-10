-- V1__init_schema.sql
-- Postgres. Идемпотентно: create if not exists + add constraints через проверки pg_constraint.

-- 1) базовые таблицы справочников

create table if not exists school_class (
    id bigserial primary key,
    name varchar(20) not null
);

create unique index if not exists uk_school_class_name on school_class(name);

create table if not exists subject (
    id bigserial primary key,
    name varchar(100) not null
);

create unique index if not exists uk_subject_name on subject(name);

create table if not exists teacher (
    id bigserial primary key,
    full_name varchar(120) not null
);

-- намеренно НЕ делаем unique на full_name (чтобы безрисково не упасть на возможных дублях)
create index if not exists ix_teacher_full_name on teacher(full_name);

create table if not exists room (
    id bigserial primary key,
    number varchar(20) not null
);

create unique index if not exists uk_room_number on room(number);

create table if not exists time_slots (
    id bigserial primary key,
    number int not null,
    start_time time not null,
    end_time time not null
);

create unique index if not exists uk_time_slot_number on time_slots(number);
create index if not exists ix_time_slot_number on time_slots(number);

-- 2) пользователи

create table if not exists app_user (
    id bigserial primary key,
    username varchar(64) not null,
    password_hash varchar(100) not null,
    role varchar(16) not null,
    enabled boolean not null default true
);

create unique index if not exists uk_app_user_username on app_user(username);
create index if not exists ix_app_user_username on app_user(username);

-- 3) уроки (главная таблица)

create table if not exists lessons (
    id bigserial primary key,
    school_class_id bigint not null,
    week_day varchar(16) not null,
    time_slot_id bigint not null,
    subject_id bigint not null,
    teacher_id bigint not null,
    room_id bigint not null
);

-- 4) чистка мусора (если вдруг был создан "битый" ряд)
delete from lessons
where school_class_id is null
   or week_day is null
   or time_slot_id is null
   or subject_id is null
   or teacher_id is null
   or room_id is null;

-- 5) NOT NULL (на случай, если раньше было без них)
alter table lessons alter column school_class_id set not null;
alter table lessons alter column week_day set not null;
alter table lessons alter column time_slot_id set not null;
alter table lessons alter column subject_id set not null;
alter table lessons alter column teacher_id set not null;
alter table lessons alter column room_id set not null;

-- 6) внешние ключи (FK) + конфликты (UNIQUE) — через безопасные DO-блоки

do $$
begin
  if not exists (select 1 from pg_constraint where conname = 'fk_lessons_class') then
    alter table lessons
      add constraint fk_lessons_class
      foreign key (school_class_id) references school_class(id)
      on delete restrict;
  end if;

  if not exists (select 1 from pg_constraint where conname = 'fk_lessons_time_slot') then
    alter table lessons
      add constraint fk_lessons_time_slot
      foreign key (time_slot_id) references time_slots(id)
      on delete restrict;
  end if;

  if not exists (select 1 from pg_constraint where conname = 'fk_lessons_subject') then
    alter table lessons
      add constraint fk_lessons_subject
      foreign key (subject_id) references subject(id)
      on delete restrict;
  end if;

  if not exists (select 1 from pg_constraint where conname = 'fk_lessons_teacher') then
    alter table lessons
      add constraint fk_lessons_teacher
      foreign key (teacher_id) references teacher(id)
      on delete restrict;
  end if;

  if not exists (select 1 from pg_constraint where conname = 'fk_lessons_room') then
    alter table lessons
      add constraint fk_lessons_room
      foreign key (room_id) references room(id)
      on delete restrict;
  end if;

  -- уникальности (конфликты)
  if not exists (select 1 from pg_constraint where conname = 'uk_lesson_class_day_slot') then
    alter table lessons
      add constraint uk_lesson_class_day_slot
      unique (school_class_id, week_day, time_slot_id);
  end if;

  if not exists (select 1 from pg_constraint where conname = 'uk_lesson_teacher_day_slot') then
    alter table lessons
      add constraint uk_lesson_teacher_day_slot
      unique (teacher_id, week_day, time_slot_id);
  end if;

  if not exists (select 1 from pg_constraint where conname = 'uk_lesson_room_day_slot') then
    alter table lessons
      add constraint uk_lesson_room_day_slot
      unique (room_id, week_day, time_slot_id);
  end if;
end $$;

-- 7) индексы под реальные запросы приложения

create index if not exists ix_lessons_class_day
  on lessons (school_class_id, week_day);

create index if not exists ix_lessons_class_day_slot
  on lessons (school_class_id, week_day, time_slot_id);

create index if not exists ix_lessons_teacher_day_slot
  on lessons (teacher_id, week_day, time_slot_id);

create index if not exists ix_lessons_room_day_slot
  on lessons (room_id, week_day, time_slot_id);
