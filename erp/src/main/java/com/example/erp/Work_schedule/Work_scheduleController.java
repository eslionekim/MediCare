package com.example.erp.Work_schedule;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.erp.Department.Department;
import com.example.erp.Department.DepartmentDTO;
import com.example.erp.Department.DepartmentRepository;
import com.example.erp.Staff_profile.Staff_profileRepository;
import com.example.erp.Status_code.Status_code;
import com.example.erp.Status_code.Status_codeDTO;
import com.example.erp.Status_code.Status_codeRepository;
import com.example.erp.User_account.User_account;
import com.example.erp.User_account.User_accountRepository;
import com.example.erp.User_role.User_roleRepository;
import com.example.erp.Vacation.Vacation;
import com.example.erp.Vacation.VacationDTO;
import com.example.erp.Vacation.VacationRepository;
import com.example.erp.Vacation_type.Vacation_typeDTO;
import com.example.erp.Vacation_type.Vacation_typeRepository;
import com.example.erp.Work_schedule.Work_scheduleDTO.ScheduleItem;
import com.example.erp.Work_schedule.Work_scheduleDTO.WorkScheduleSaveRequest;
import com.example.erp.Work_schedule.ScheduleCalendarDTO;
import com.example.erp.Work_type.Work_type;
import com.example.erp.Work_type.Work_typeDTO;
import com.example.erp.Work_type.Work_typeRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class Work_scheduleController {

    private final VacationRepository vacationRepository;
    private final Vacation_typeRepository vacation_typeRepository;
	private final User_accountRepository user_accountRepository;
    private final Staff_profileRepository staff_profileRepository;
    private final DepartmentRepository departmentRepository;
    private final Work_scheduleRepository work_scheduleRepository;
    private final Work_typeRepository work_typeRepository;
    private final User_roleRepository user_roleRepository;
    private final Status_codeRepository status_codeRepository;
    private final Work_scheduleService work_scheduleService;
    
    @GetMapping("/hr/scheduleAssignment") // 인사 -> 스케줄 부여-> 날짜 by 은서
    public String getSchedulePage(@RequestParam(value="year",required = false) Integer year,
                                  @RequestParam(value="month",required = false) Integer month,
                                  @RequestParam(value="departmentCode", required = false) String departmentCode,
                                  @RequestParam(value="keyword", required = false) String keyword,
                                  @RequestParam(value="status", required = false) String status,
                                  Model model) {
        // 기본값: 현재 연도·월 (기본 달력 날짜용)
        LocalDate now = LocalDate.now();
        year = (year == null) ? now.getYear() : year;
        month = (month == null) ? now.getMonthValue() : month;

        // 직원 리스트 + 상태 계산
        List<User_account> user = user_accountRepository.findAll(); // 필요시 진료과 join fetch
        
        // 1일 스케줄 여부 계산
        LocalDate firstDay = LocalDate.of(year, month, 1);
        List<Map<String, Object>> userWithFlag = new ArrayList<>();
        for (User_account u : user) {
        	boolean hasSchedule =work_scheduleRepository
        	                .findByUserAndMonth(u.getUser_id(), firstDay, firstDay)
        	                .size() > 0;

    		// ======================
            // 🔍 필터링 시작
            // ======================

            // 1️ 진료과 필터
            if (departmentCode != null && !departmentCode.isBlank()) {
                if (u.getStaff_profile().isEmpty()) continue;
                if (!departmentCode.equals(
                        u.getStaff_profile().get(0).getDepartment().getDepartment_code()
                )) continue;
            }

            // 2️ 키워드 (ID or 이름)
            if (keyword != null && !keyword.isBlank()) {
                String userId = u.getUser_id();
                String name = u.getName();

                boolean matchId = userId != null && userId.contains(keyword);
                boolean matchName = name != null && name.contains(keyword);

                if (!(matchId || matchName)) continue;
            }


            // 3️ 상태 필터
            if ("Y".equals(status) && !hasSchedule) continue;
            if ("N".equals(status) && hasSchedule) continue;
		
            		
            Map<String, Object> map = new HashMap<>();
            map.put("user", u);
            map.put("hasScheduleOnFirst", hasSchedule);
            userWithFlag.add(map);
        }
        List<Department> departments = departmentRepository.findActive();
        model.addAttribute("departments", departments);
        model.addAttribute("year", year);
        model.addAttribute("month", month);
        model.addAttribute("userWithFlag", userWithFlag);
        return "hr/scheduleAssignment";
    }
    
    
    @GetMapping("/doctor/mySchedule") // 의사 -> 스케줄 조회 by 은서
    public String getMySchedulePage() {
        return "doctor/mySchedule";
    }
    
    @GetMapping("/doctor/mySchedule/events") //의사 -> 스케줄 조회 -> 근무 스케줄 달력 by 은서
    @ResponseBody
    public List<Map<String, Object>> getMyScheduleEvents(
            @RequestParam(value="year",required = false) int year,
            @RequestParam(value="month",required = false) int month
    ) {
        String userId = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        List<ScheduleCalendarDTO> list =
                work_scheduleService.getDoctorMonthlySchedule(userId, year, month);

        List<Map<String, Object>> events = new ArrayList<>();

        for (ScheduleCalendarDTO item : list) {
            Map<String, Object> event = new HashMap<>();
            
            StringBuilder title = new StringBuilder();
            title.append(item.getWorkName());

            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            if (item.getStartTime() != null) {
                title.append("\n출근 ")
                     .append(item.getStartTime().format(timeFormatter));
            }

            if (item.getEndTime() != null) {
                title.append("\n퇴근 ")
                     .append(item.getEndTime().format(timeFormatter));
            }

            if (item.getStatusName() != null) {
                title.append(item.getStatusName());
            }

            
            event.put("title", title.toString());                 // ⭐ work_name
            event.put("start", item.getWorkDate().toString());     // ⭐ yyyy-MM-dd
            event.put("allDay", true);

            events.add(event);
        }

        return events;
    }
    
    @GetMapping("/doctor/mySchedule/vacations") //의사 -> 스케줄 조회 -> 휴가 리스트 by 은서
    @ResponseBody
    public List<VacationDTO> getMyVacations() {
    	String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        return vacationRepository.findVacationByUserId(userId)
            .stream()
            .map(v -> new VacationDTO(
                v.getVacation_id(),
                v.getVacation_type().getType_name(),
                v.getStart_date().toString(),
                v.getEnd_date().toString(),
                v.getStatus_code().getName(),
                v.getStatus_code().getStatus_code()
            ))
            .collect(Collectors.toList());
    }
    
    @GetMapping("/doctor/mySchedule/vacation-types") //의사-> 스케줄 조회->휴가리스트 -> 검색창-> 분류 by 은서
    @ResponseBody
    public List<Vacation_typeDTO> getVacationTypes() {
        return vacation_typeRepository.findByIsActiveTrue()
            .stream()
            .map(v -> new Vacation_typeDTO(
                v.getVacation_type_code(),
                v.getType_name()
            ))
            .toList();
    }
    
    @GetMapping("/doctor/mySchedule/vacation-status") //의사-> 스케줄 조회-> 휴가리스트-> 검색창-> 승인여부 by 은서
    @ResponseBody
    public List<Status_codeDTO> getVacationStatus() {
        return status_codeRepository.findByCategoryAndIsActiveTrue("vacation")
            .stream()
            .map(s -> new Status_codeDTO(
                s.getStatus_code(),
                s.getName()
            ))
            .toList();
    }

    @GetMapping("/doctor/mySchedule/vacations/search") //의사-> 스케줄 조회-> 휴가리스트-> 검색창 by 은서
    @ResponseBody
    public List<VacationDTO> searchVacations(
        @RequestParam(value="typeCode",required = false) String typeCode,
        @RequestParam(value="statusCode",required = false) String statusCode,
        @RequestParam(value="date",required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        return vacationRepository.searchVacations(userId,
                (typeCode == null || typeCode.isBlank()) ? null : typeCode,
                (statusCode == null || statusCode.isBlank()) ? null : statusCode,
                date)
            .stream()
            .map(v -> new VacationDTO(
        		v.getVacation_id(),
                v.getVacation_type().getType_name(),
                v.getStart_date().toString(),
                v.getEnd_date().toString(),
                v.getStatus_code().getName(),
                v.getStatus_code().getStatus_code()
            ))
            .collect(Collectors.toList());
    }

    @PostMapping("/doctor/mySchedule/vacation/{vacationId}/cancel") //의사-> 스케줄 조회-> 휴가리스트 -> 휴가취소 by 은서
    @ResponseBody
    public void cancelVacation(@PathVariable("vacationId") Long vacationId) {
    	Vacation vacation = vacationRepository.findById(vacationId)
                .orElseThrow(() -> new RuntimeException("휴가 없음"));

        Status_code cancelStatus = status_codeRepository.findByCode("VAC_CANCEL_REQUESTED")
                .orElseThrow(() -> new RuntimeException("상태코드 없음"));

        vacation.setStatus_code(cancelStatus);
        vacationRepository.save(vacation);
    }


    
    @GetMapping("/work-types") // 인사 -> 스케줄 부여 -> 근무종류 조회 by 은서
    @ResponseBody
    public List<ScheduleCalendarDTO> getWorkTypesByUser(@RequestParam("user_id") String user_id) {
        List<Work_type> types = work_typeRepository.findByUserRole(user_id);
        return types.stream()
                    .map(wt -> new ScheduleCalendarDTO(wt.getWork_type_code(), wt.getWork_name()))
                    .collect(Collectors.toList());
    }
    
    @PostMapping("/work-schedule/save") //인사 -> 스케줄 부여 -> 저장 by 은서
    public ResponseEntity<?> saveSchedule(@RequestBody WorkScheduleSaveRequest req) {

        User_account user_account = user_accountRepository.findById(req.getUser_id()).orElseThrow();
        Department department = departmentRepository.findById(req.getDepartment_code()).orElseThrow();

        // 1. 기존 스케줄 삭제 (user + month 기준)
        LocalDate firstDay = req.getSchedules().stream()
                                 .map(ScheduleItem::getWorkDate)
                                 .min(LocalDate::compareTo)
                                 .orElse(LocalDate.now());
        LocalDate lastDay = req.getSchedules().stream()
                                .map(ScheduleItem::getWorkDate)
                                .max(LocalDate::compareTo)
                                .orElse(LocalDate.now());

        List<Work_schedule> existing = work_scheduleRepository.findByUserAndMonth(
            user_account.getUser_id(), firstDay, lastDay
        );
        work_scheduleRepository.deleteAll(existing);
        
        for (ScheduleItem item : req.getSchedules()) {
            Work_schedule ws = new Work_schedule();
            ws.setUser_account(user_account);
            ws.setDepartment(department);
            ws.setWork_date(item.getWorkDate());
            ws.setWork_type(
                work_typeRepository.findById(item.getWorkTypeCode()).orElseThrow()
            );

            work_scheduleRepository.save(ws);
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/work-schedule/{user_id}/{year}/{month}") // 인사 -> 스케줄 부여 -> 저장하고 나서 새로고침 by 은서
    @ResponseBody
    public List<Work_scheduleDTO.ScheduleItem> getMonthlySchedule(
            @PathVariable("user_id") String user_id,
            @PathVariable("year") int year,
            @PathVariable("month") int month) {

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<Work_schedule> schedules = work_scheduleRepository.findByUserAndMonth(user_id, start, end);

        return schedules.stream()
                .map(ws -> {
                    Work_scheduleDTO.ScheduleItem item = new Work_scheduleDTO.ScheduleItem();
                    item.setWorkDate(ws.getWork_date());
                    item.setWorkTypeCode(ws.getWork_type().getWork_type_code());
                    return item;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/hr/allSchedule") // 인사 -> 전체 스케줄 조회 by 은서
    public String allSchedule() {
		return "hr/allSchedule";
    }
    
    @GetMapping("/hr/allSchedule/byDate") // 인사 -> 전체 스케줄 조회 -> 날짜 선택시 리스트 by 은서
    @ResponseBody
    public List<Work_scheduleDTO.HrDailyScheduleItem> getScheduleByDate(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return work_scheduleRepository.findDailySchedule(date);
    }
    
    @GetMapping("/hr/allSchedule/departments") // 인사 -> 전체 스케줄 조회 -> 진료과 드롭다운 by 은서
    @ResponseBody
    public List<DepartmentDTO> getDepartments() {
        return departmentRepository.findActive()
                .stream()
                .map(d -> new DepartmentDTO(
                        d.getDepartment_code(),
                        d.getName()
                ))
                .toList();
    }
    
    @GetMapping("/hr/allSchedule/work-types")// 인사 -> 전체 스케줄 조회 -> 근무형태 드롭다운 by 은서
    @ResponseBody
    public List<Work_typeDTO> getWorkTypes() {
        return work_typeRepository.findAll()
                .stream()
                .map(w -> new Work_typeDTO(
                        w.getWork_type_code(),
                        w.getWork_name(),
                        w.getRole_code().getRole_code()
                ))
                .toList();
    }


    
    // 출근 by 은서
    @PostMapping("/work/time-in")
    public ResponseEntity<?> timeIn() {
        String userId = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        LocalDate today = LocalDate.now();

        Work_schedule ws = work_scheduleRepository
                .findByUser_account_UserIdAndWork_date(userId, today)
                .orElseThrow(() -> new RuntimeException("오늘 스케줄 없음"));

        if (ws.getStart_time() != null) {
            return ResponseEntity
                    .badRequest()
                    .body("이미 출근 처리되었습니다.");
        }
        
        ws.setStart_time(LocalTime.now());
        work_scheduleRepository.save(ws); //insert아니고 update

        return ResponseEntity.ok("출근 처리 완료"); //alert 띄우기
    }

    // 퇴근 가능 여부 체크 by 은서
    @GetMapping("/work/time-out/check")
    public ResponseEntity<?> checkTimeOut() {
        String userId = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        LocalDate today = LocalDate.now();

        Work_schedule ws = work_scheduleRepository
                .findByUser_account_UserIdAndWork_date(userId, today)
                .orElseThrow(() -> new RuntimeException("오늘 스케줄 없음"));

        if (ws.getEnd_time() != null) {
            return ResponseEntity.badRequest().body("이미 퇴근 처리되었습니다.");
        }

        if (ws.getStart_time() == null) {
            return ResponseEntity.badRequest().body("출근 기록이 없어 퇴근할 수 없습니다.");
        }

        return ResponseEntity.ok("퇴근 가능");
    }

    
    // 퇴근 by 은서
    @PostMapping("/work/time-out")
    public ResponseEntity<?> timeOut() {
        String userId = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        LocalDate today = LocalDate.now();

        Work_schedule ws = work_scheduleRepository
                .findByUser_account_UserIdAndWork_date(userId, today)
                .orElseThrow(() -> new RuntimeException("오늘 스케줄 없음"));

        ws.setEnd_time(LocalTime.now());
        work_scheduleRepository.save(ws);

        return ResponseEntity.ok("퇴근 처리 완료");
    }


    // 인사 -> 근태 조회 by 은서
    @GetMapping("/hr/allWork") 
    public String allWork(Model model) {
    	List<User_account> user = user_accountRepository.findAll();
    	model.addAttribute("user", user);
    	return "hr/allWork";
    }
    
    // 인사 -> 근태 조회 by 은서
    @GetMapping("/hr/allWork/schedule")
    @ResponseBody
    public List<Map<String, Object>> getDoctorScheduleEvents(
            @RequestParam(value="userId", required=false) String userId,
            @RequestParam(value="year", required=false) int year,
            @RequestParam(value="month", required=false) int month
    ) {
        List<ScheduleCalendarDTO> list =
                work_scheduleService.getDoctorMonthlySchedule(userId, year, month);

        List<Map<String, Object>> events = new ArrayList<>();

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        for (ScheduleCalendarDTO item : list) {
            Map<String, Object> event = new HashMap<>();

            StringBuilder title = new StringBuilder();
            title.append(item.getWorkName());

            if (item.getStartTime() != null) {
                title.append("\n출근 ")
                     .append(item.getStartTime().format(timeFormatter));
            }

            if (item.getEndTime() != null) {
                title.append("\n퇴근 ")
                     .append(item.getEndTime().format(timeFormatter));
            }
            
            if (item.getStatusName() != null) {
                title.append(item.getStatusName());
            }


            event.put("title", title.toString());
            event.put("start", item.getWorkDate().toString());
            event.put("allDay", true);

            events.add(event);
        }

        return events;
    }


    
    
}
