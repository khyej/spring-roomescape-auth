-- 매장 (2개)
INSERT INTO store (name) VALUES ('잠실점');
INSERT INTO store (name) VALUES ('강남점');

-- 예약 시간 (5개씩 각 매장)
INSERT INTO reservation_time (start_at, store_id) VALUES ('10:00', 1);
INSERT INTO reservation_time (start_at, store_id) VALUES ('11:00', 1);
INSERT INTO reservation_time (start_at, store_id) VALUES ('14:00', 1);
INSERT INTO reservation_time (start_at, store_id) VALUES ('16:00', 1);
INSERT INTO reservation_time (start_at, store_id) VALUES ('19:00', 1);

INSERT INTO reservation_time (start_at, store_id) VALUES ('10:00', 2);
INSERT INTO reservation_time (start_at, store_id) VALUES ('11:00', 2);
INSERT INTO reservation_time (start_at, store_id) VALUES ('14:00', 2);
INSERT INTO reservation_time (start_at, store_id) VALUES ('16:00', 2);
INSERT INTO reservation_time (start_at, store_id) VALUES ('19:00', 2);

-- 테마 (5개)
INSERT INTO theme (name, description, thumbnail, store_id)
VALUES ('공포의 방', '심장 약한 사람은 들어오지 마세요. 공포 등급 최상.', 'https://picsum.photos/id/1060/500/500', 1);
INSERT INTO theme (name, description, thumbnail, store_id)
VALUES ('미스터리 추리', '셜록이 되어 사건을 해결해보세요.', 'https://picsum.photos/id/1060/500/500', 1);
INSERT INTO theme (name, description, thumbnail, store_id)
VALUES ('우주 탈출', '고장난 우주선에서 1시간 안에 탈출하라.', 'https://picsum.photos/id/1060/500/500', 2);
INSERT INTO theme (name, description, thumbnail, store_id)
VALUES ('조선시대 암행어사', '암행어사가 되어 부패한 사또를 잡아라.', 'https://picsum.photos/id/1060/500/500', 2);
INSERT INTO theme (name, description, thumbnail, store_id)
VALUES ('초보자 방', '방탈출이 처음이신 분들을 위한 입문 테마.', 'https://picsum.photos/id/1060/500/500', 1);

-- 예약
-- 잠실점 (store_id=1)
INSERT INTO reservation (user_name, theme_id, date, time_id, store_id)
VALUES ('동키', 1, '2026-05-10', 1, 1);
INSERT INTO reservation (user_name, theme_id, date, time_id, store_id)
VALUES ('그해', 1, '2026-05-10', 3, 1);
INSERT INTO reservation (user_name, theme_id, date, time_id, store_id)
VALUES ('아루', 1, '2026-05-10', 5, 1);

-- 강남점 (store_id=2)
INSERT INTO reservation (user_name, theme_id, date, time_id, store_id)
VALUES ('매트', 3, '2026-05-10', 2, 2);
INSERT INTO reservation (user_name, theme_id, date, time_id, store_id)
VALUES ('동키', 4, '2026-05-10', 4, 2);

-- 사용자, 매니저, 최고 관리자
INSERT INTO "user" (name, username, password, role, store_id)
VALUES ('동키', 'donkey', 'password1', 'USER', NULL);
INSERT INTO "user" (name, username, password, role, store_id)
VALUES ('잠실매니저', 'jamsil_manager', 'password123', 'MANAGER', 1);
INSERT INTO "user" (name, username, password, role, store_id)
VALUES ('강남매니저', 'gangnam_manager', 'password123', 'MANAGER', 2);
INSERT INTO "user" (name, username, password, role, store_id)
VALUES ('최고관리자', 'admin', 'admin123', 'ADMIN', NULL);
