package com.shop.entity;

public class JPA_엔티티_생명주기_전체흐름 {
/*

JPA 핵심 개념과 엔티티 생명주기를 이해하기 쉽게 텍스트 다이어그램 형태로 자세히 설명.

[엔티티(Entity)]
    ↓
(객체 하나하나: DB 테이블의 한 행(row)을 자바 객체로 표현한 것)
    ↓
[엔티티 매니저 팩토리(EntityManagerFactory)]
    ↓ (create)
[엔티티 매니저(EntityManager)]
    ↓ (운영, CRUD)
[영속성 컨텍스트(Persistence Context)]  <== 엔티티 매니저가 내부에서 관리하는 영역
    ↓ (1차 캐시)
[엔티티 객체들 관리 및 DB와 동기화]




### JPA 주요 구성요소 및 관계 그림 텍스트 모델링

+---------------------------+
|  EntityManagerFactory     |  <-- 애플리케이션 시작 시 1회 생성
+---------------------------+
             |
             | create()
             v
+---------------------------+
|     EntityManager         |  <-- 트랜잭션 단위로 생성, 영속성 컨텍스트 관리
+---------------------------+
             |
             | 내부 관리
             v
+---------------------------+
|  Persistence Context       |  <-- 1차 캐시, 엔티티 객체 저장소
+---------------------------+
             |
             | 키(PK) : 값(엔티티 객체) 매핑
             |-----------------------------------
             |    Map<PrimaryKey, Entity Object>
             |-----------------------------------
             v
+-------------------------------------+
|          Entity (엔티티)             |  <-- DB 테이블 행과 매핑된 객체
+-------------------------------------+



### 각 구성요소 설명

* **EntityManagerFactory**

  * 무거운 객체, 애플리케이션 당 1개만 생성
  * EntityManager를 생성하는 역할
  * DB 연결 설정, 영속성 단위팩팩 구성 등 초기화 담당

* **EntityManager**

  * 실제 엔티티를 관리하는 객체
  * 영속성 컨텍스트(Persistence Context)를 소유
  * 엔티티 저장, 조회, 수정, 삭제 명령을 처리
  * 트랜잭션 범위 안에서 생성, 종료

* **Persistence Context (영속성 컨텍스트)**

  * 엔티티 객체를 저장하는 일종의 메모리 캐시 공간
  * 엔티티 상태(비영속, 영속, 준영속)를 관리
  * 1차 캐시 역할: 동일 트랜잭션 내에서 DB 중복 조회 방지
  * 변경 감지(dirty checking) 기능 제공

* **Entity (엔티티)**

  * DB 테이블과 매핑된 자바 객체
  * 각 객체는 DB 테이블의 한 행(row)과 대응
  * 예: Member, Item, Order 등

---

### 엔티티 생명주기 상태 흐름

[비영속 (New)] --persist()--> [영속 (Managed)] --detach()/clear()--> [준영속 (Detached)] --merge()--> [영속 (Managed)] --remove()--> [삭제 (Removed)]


* **비영속 (New)**: 새로 생성된 엔티티, 아직 영속성 컨텍스트와 무관함
* **영속 (Managed)**: 영속성 컨텍스트에 의해 관리되는 상태, DB와 동기화됨
* **준영속 (Detached)**: 영속성 컨텍스트에서 분리된 상태, DB와 동기화되지 않음
* **삭제 (Removed)**: 삭제 예약 상태, 트랜잭션 커밋 시 DB에서 삭제됨

---

### JPA 생명주기 상세 예시

1. 객체 생성 → 비영속 상태
2. `entityManager.persist(entity)` 호출 → 영속 상태 (1차 캐시에 저장)
3. 영속 상태에서 객체 변경 → 변경 내용은 자동으로 감지(Dirty Checking)
4. `entityManager.detach(entity)` 호출 → 준영속 상태 (1차 캐시에서 분리)
5. 준영속 상태 객체 변경 → DB 반영 안 됨
6. `entityManager.merge(entity)` 호출 → 객체가 다시 영속 상태로 변경
7. `entityManager.remove(entity)` 호출 → 삭제 예약 상태
8. 트랜잭션 커밋 시 DB 반영 (삽입, 수정, 삭제 수행)

---

### 1차 캐시(영속성 컨텍스트 내부 구조)

```
Persistence Context (1차 캐시)
-------------------------------------
| PK (Primary Key) | Entity Object  |
-------------------------------------
| 1                | Member 객체    |
| 5                | Item 객체      |
| 12               | Order 객체     |
-------------------------------------
```

* 트랜잭션 내에서 동일 PK로 조회 시 DB 대신 1차 캐시에서 객체를 반환
* DB 변경은 트랜잭션 커밋 시점에 동기화됨








# 🧭 JPA 엔티티 생명주기 전체 흐름 (텍스트 다이어그램)



## 📌 1단계: EntityManagerFactory 생성 (애플리케이션 시작 시 1회)


Application 시작
      │
      ▼
+------------------------+
| EntityManagerFactory   |  ← JPA 설정 기반 생성 (persistence.xml or 설정클래스)
+------------------------+


* 역할 : DB와 연결 설정, `EntityManager` 생성 역할
* 한 번만 생성 → 공유해서 사용
* @PersistenceUnit 또는 @SpringBootApplication 내부에서 자동 구성됨



## 📌 2단계: EntityManager 생성


EntityManagerFactory
        │
        ├── createEntityManager()
        ▼
+-------------------+
| EntityManager     |  ← 실제 JPA 작업의 핵심 클래스
+-------------------+
        │
        └─ 내부적으로 영속성 컨텍스트 생성


* 역할: 엔티티 관리, CRUD, JPQL, 트랜잭션, flush 등 실행
* 쓰레드마다 새로 생성
* 내부에 영속성 컨텍스트(Persistence Context) 저장 메모리 공간 보유



## 📌 3단계: 비영속 (Transient)

Item item = new Item();  ← 단순 자바 객체 (JPA와 무관)


* 아직 JPA가 전혀 모르는 상태
* DB와 연동 ❌
* 1차 캐시에도 없음



## 📌 4단계: 영속 (Persistent)

em.persist(item);  ← 영속 상태 진입!


EntityManager
     │
     ▼
[영속성 컨텍스트]
  - item 객체 등록
  - 1차 캐시로 관리 시작


* JPA가 엔티티를 **1차 캐시에서 관리**함
* 쓰기 지연 SQL 저장소에 insert SQL 저장됨
* 아직 DB 반영은 ❌ (`flush` 또는 `commit` 시점에 DB에 전송됨)



## 📌 5단계: 조회, 변경 감지 (Dirty Checking)


Item findItem = em.find(Item.class, item.getId());
findItem.setPrice(20000);  ← 변경


* 1차 캐시에서 바로 조회 (DB 접근 X)
* 값이 바뀌면 **트랜잭션 커밋 시점**에 자동으로 update SQL 생성됨
* 변경 감지 (Dirty Checking) 작동



## 📌 6단계: 준영속 (Detached)

em.detach(item);         // 또는 em.clear(), em.close()


[영속성 컨텍스트]에서 제거됨
→ 더 이상 추적 안 함
→ 변경 감지 ❌


* `detach()`하면 관리 종료
* 수동으로 다시 `merge()` 해야 DB에 반영 가능



## 📌 7단계: 병합 (Merge)

em.merge(item);  ← 준영속 → 영속 다시 전환


* 기존 엔티티 상태를 복제해서 영속성 컨텍스트에 등록
* 원래 객체는 여전히 detached 상태
* 복사된 새 엔티티만 관리 대상



## 📌 8단계: 삭제 (Removed)

em.remove(item);


* 삭제 예약 상태
* `commit()` 시 delete SQL 실행됨



## 📌 9단계: 플러시(Flush) → 커밋(Commit)


tx.commit();


1. flush() → 쓰기 지연 SQL 실행 (insert/update/delete)
2. 실제 DB 반영 완료
3. 트랜잭션 종료




# 📦 영속성 컨텍스트 사용 시 이점

| 기능         | 설명                         |
| ---------- | -------------------------- |
| 1차 캐시      | 같은 ID 조회 시 DB 접근 없이 캐시 사용  |
| 쓰기 지연      | SQL 모아서 한번에 전송 → 성능 최적화    |
| 변경 감지      | set만 해도 자동으로 update SQL 발생 |
| 트랜잭션 단위 관리 | 동일 트랜잭션 내 객체 동일성 보장 (==)   |
| 지연 로딩 지원   | 연관 객체를 실제 사용할 때 쿼리 실행      |

---

# ✅ 전체 요약 흐름

[비영속] → persist() → [영속] → detach() → [준영속] → merge() → [영속]
                                           ↓
                                       remove()
                                           ↓
                                     [삭제(removed)]


# 전체 생명주기 요약

비영속 (New)
    ↓ persist()
영속 (Managed)
    ↓ detach() / clear()
준영속 (Detached)
    ↓ merge()
영속 (Managed)
    ↓ remove()
삭제 (Removed)


비영속은 ‘아직 JPA가 모르는 새로운 객체’

영속은 ‘JPA가 관리하며 DB와 연결된 상태’

준영속은 ‘JPA가 관리하다가 관리 안하는 상태’

삭제는 ‘JPA가 삭제 예정으로 표시한 상태’

 */
}
