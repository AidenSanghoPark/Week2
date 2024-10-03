# Lecture Management System

## 소개
이 프로젝트는 강의를 관리하고 강의 신청을 처리하기 위한 시스템입니다. 회원가입 기능이 필요하지 않으며, 강의 신청 및 강의 정보를 관리하는 두 개의 테이블로 구성되어 있습니다.

## 데이터베이스 구조

### 테이블

#### Lecture
강의 목록을 관리하는 테이블입니다. 각 강의의 상세 정보 및 수강생 수를 추적합니다.

- **필드:**
    - `idx`: 강의의 고유 ID (PRIMARY KEY)
    - `title`: 강의 제목 (NOT NULL)
    - `description`: 강의 설명
    - `instructor`: 강의 진행자
    - `start_Date`: 강의 시작일
    - `end_Date`: 강의 종료일
    - `max_Participants`: 최대 수강생 수
    - `current_Participants`: 현재 등록된 수강생 수 (DEFAULT: 0)
    - `status`: 강의 상태 (DEFAULT: 'ACTIVE', 값: 'ACTIVE', 'INACTIVE')
    - `created_At`: 강의 생성일 (DEFAULT: CURRENT_TIMESTAMP)

#### Lecture_Apply
강의 수강 정보를 관리하는 테이블입니다. 각 신청의 세부 정보가 포함됩니다.

- **필드:**
    - `idx`: 신청의 고유 ID (PRIMARY KEY)
    - `lecture_Id`: 신청한 강의의 ID (FOREIGN KEY, REFERENCES Lecture(idx))
    - `user_Id`: 신청한 사용자 ID
    - `applied_At`: 신청일 (DEFAULT: CURRENT_TIMESTAMP)

## ERD 설명
이 시스템은 두 개의 테이블, Lecture와 Lecture_Apply로 구성되어 있습니다.
강의 신청 시 Lecture_Apply 테이블에 신청 정보를 추가합니다.
신청이 완료되면, 콜백 함수를 통해 Lecture 테이블의 current_Participants 필드를 업데이트합니다.
이는 Lecture_Apply 테이블의 신청 개수를 조회하여 현재 등록된 수강생 수를 반영합니다.
