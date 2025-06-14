-- 카테고리 테스트 데이터 (ID 자동 생성)
INSERT INTO categories (name, type)
VALUES ('소비관리', 'SPENDING'),
       ('저축계획', 'SAVINGS'),
       ('투자전략', 'INVESTMENT'),
       ('부채관리', 'DEBT'),
       ('기타상담', 'ETC');

-- 전문가 테스트 데이터 (ID 자동 생성)
INSERT INTO advisors (user_id, name, bio, certification_file, price, available, is_online,
                      created_at, updated_at)
VALUES (101, '김투자', '10년 경력의 투자 전문가입니다. 주식, 펀드, 부동산 투자에 대한 깊은 지식을 보유하고 있습니다.', 'cert1.pdf',
        50000.00, true, true, NOW(), NOW()),
       (102, '박저축', '안전한 저축 전략을 제안하는 전문가입니다. 예적금, 연금 상품에 대한 전문 지식이 있습니다.', 'cert2.pdf', 30000.00,
        true, false, NOW(), NOW()),
       (103, '이부채', '부채 관리 및 대출 상담 전문가입니다. 신용관리와 부채 정리 전략을 제공합니다.', 'cert3.pdf', 40000.00, true,
        true, NOW(), NOW()),
       (104, '최소비', '가계부 작성과 소비 패턴 분석 전문가입니다. 합리적인 소비 계획을 도와드립니다.', 'cert4.pdf', 25000.00, false,
        false, NOW(), NOW());

-- 전문가-카테고리 연결 테스트 데이터 (ID 자동 생성)
INSERT INTO advisor_tags (advisor_id, category_id)
VALUES (1, 3), -- 김투자 - 투자전략
       (2, 2), -- 박저축 - 저축계획
       (3, 4), -- 이부채 - 부채관리
       (4, 1), -- 최소비 - 소비관리
       (1, 5), -- 김투자 - 기타상담 (다중 태그 예시)
       (2, 5); -- 박저축 - 기타상담 (다중 태그 예시)
