# Intrusion Detection System - Server

<b>연구과제</b> 

1. 자바 소켓 서버로 연결된 클라이언트에서 측정된 데이터를 전송받고 클라이언트들을 관리
2. 데이터베이스와 연결하고 클라이언트가 보낸 데이터 저장
3. 데이터베이스에 저장된 데이터를 웹 페이지에 출력하여 관리자에게 보고

```
지도교수
경성대학교 컴퓨터공학과 최재원
경성대학교 정보통신공학과 신광호

참여학생
경성대학교 컴퓨터공학과 이상호
경성대학교 컴퓨터공학과 임주형
```

#

+ 전체 시스템 계획 및 개발 - 2020.09 ~ 2021.01
+ 코드 리뷰 및 향후 계획 수립 - 2021.02 ~ 2021.06
+ 전체 시스템 모듈화(진행중) 2021.07 ~

#

#### Web Application Server
+ JSP를 통해 관리자가 실시간 보고를 받을 수 있도록 설계
+ JDBC를 이용하여 아두이노가 보내온 정보들을 데이터베이스에 쓰고 읽는 역할 수행

#### Java Socket Server
+ 클라이언트와 통신하기 위한 서버를 열고 클라이언트에서 보내온 메시지를 수신
+ 제어 메시지를 송신하여 클라이언트 제어

#

### 경성대학교 산학협력단 지원