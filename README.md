## 25.12.01 ~ 25.12.08 김은서 : [의사] 금일 진료 리스트, 차트 작성 및 수정, 차트 조회, 전체 진료 리스트 
금일 진료 리스트 http://localhost:8080/doctor/todayVisits
(진료 시작 버튼 클릭 시) 차트 작성 및 수정 http://localhost:8080/doctor/chartWrite?visit_id=방문id&patient_id=환자id
(차트 저장 및 전체 진료 리스트에서 클릭 시) 차트 조회 http://localhost:8080/doctor/chartView?visit_id=방문id&patient_id==환자id
전체 진료 리스트 http://localhost:8080/doctor/allVisits

## 25.12.08 김은서 : [인사] 휴가 리스트 
휴가 리스트 http://localhost:8080/hr/vacationList
(선택) 팝업창 뜸

## 25.12.09 김은서 : 로그인 구현
로그인 페이지 http://localhost:8080/login

### role_code별 로그인 후 접속 페이지
com.example.erp.security 폴더 CustomLoginSuccessHandler.java
컨트롤러에서 설정한 url매핑값으로 바꾸기

// 의사일 경우
if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR"))) {
     redirectURL += "/doctor/todayVisits";
} 
// 인사일 경우
else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_HR"))) {
    redirectURL += "/hr/vacationList";
} 
// 원무일 경우
else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_STAFF"))) {
     redirectURL += "/patients";
}
