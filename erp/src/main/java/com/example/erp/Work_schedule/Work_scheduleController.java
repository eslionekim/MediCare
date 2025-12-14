package com.example.erp.Work_schedule;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.erp.Department.Department;
import com.example.erp.Department.DepartmentRepository;
import com.example.erp.Staff_profile.Staff_profileRepository;
import com.example.erp.User_account.User_account;
import com.example.erp.User_account.User_accountRepository;
import com.example.erp.User_role.User_roleRepository;
import com.example.erp.Work_schedule.Work_scheduleDTO.ScheduleItem;
import com.example.erp.Work_schedule.Work_scheduleDTO.WorkScheduleSaveRequest;
import com.example.erp.Work_type.Work_type;
import com.example.erp.Work_type.Work_typeRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class Work_scheduleController {
	private final User_accountRepository user_accountRepository;
    private final Staff_profileRepository staff_profileRepository;
    private final DepartmentRepository departmentRepository;
    private final Work_scheduleRepository work_scheduleRepository;
    private final Work_typeRepository work_typeRepository;
    private final User_roleRepository user_roleRepository;
    
    @GetMapping("/hr/schedule") // 인사 -> 스케줄 부여-> 날짜 by 은서
    public String getSchedulePage(@RequestParam(value="year",required = false) Integer year,
                                  @RequestParam(value="month",required = false) Integer month,
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
            boolean hasSchedule = work_scheduleRepository.findByUserAndMonth(
                    u.getUser_id(), firstDay, firstDay
            ).size() > 0;

            Map<String, Object> map = new HashMap<>();
            map.put("user", u);
            map.put("hasScheduleOnFirst", hasSchedule);
            userWithFlag.add(map);
        }
        
        model.addAttribute("year", year);
        model.addAttribute("month", month);
        model.addAttribute("userWithFlag", userWithFlag);
        return "hr/scheduleAssignment";
    }
    
    @GetMapping("/work-types")
    @ResponseBody
    public List<Work_scheduleDTO.WorkTypeItem> getWorkTypesByUser(@RequestParam("user_id") String user_id) {
        List<Work_type> types = work_typeRepository.findByUserRole(user_id);
        return types.stream()
                    .map(wt -> new Work_scheduleDTO.WorkTypeItem(wt.getWork_type_code(), wt.getWork_name()))
                    .collect(Collectors.toList());
    }



    @PostMapping("/work-schedule/save")
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

    @GetMapping("/work-schedule/{user_id}/{year}/{month}")
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

    /*
    // 달력 이벤트 API (FullCalendar)
    @GetMapping("/hr/schedule/calender")
    @ResponseBody
    public List<Map<String, Object>> getScheduleEvents(@RequestParam String userId,
                                                       @RequestParam Integer year,
                                                       @RequestParam Integer month) {

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<Work_schedule> schedule = work_scheduleRepository.findByUserAndMonth(userId, start, end);

        List<Map<String, Object>> events = new ArrayList<>();

        for (Work_schedule ws : schedule) {
            Map<String, Object> map = new HashMap<>();
            map.put("work_name", ws.getWork_type().getWork_name()); // 근무 이름
            map.put("start", ws.getWork_date().toString());    // 날짜
            map.put("work_type_code", ws.getWork_type().getWork_type_code());
            map.put("user_id", ws.getUser_account().getUser_id());

            events.add(map);
        }

        return events;
    }


    // 드롭다운 저장 API
    @PostMapping("/hr/schedule/save")
    @ResponseBody
    public ResponseEntity<?> saveSchedule(@RequestBody List<Map<String, Object>> scheduleList) {
    	for (Map<String, Object> m : scheduleList) {

            String userId = (String) m.get("userId");
            String deptCode = (String) m.get("deptCode");
            String dateStr = (String) m.get("date");
            String workTypeCode = (String) m.get("workTypeCode");

            Work_schedule ws = new Work_schedule();
            ws.setUser_account(user_accountRepository.findById(userId).orElseThrow());
            ws.setDepartment(departmentRepository.findById(deptCode).orElseThrow());
            ws.setWork_date(LocalDate.parse(dateStr));
            ws.setWork_type(work_typeRepository.findById(workTypeCode).orElseThrow());

            work_scheduleRepository.save(ws);
        }

        return ResponseEntity.ok("저장 완료");
    }
    
    //달력 생성 API
    @GetMapping("/hr/schedule/calenderAPI")
    public Map<String, Object> getCalendar(@RequestParam(value="year",required = false) Integer year,@RequestParam(value="month",required = false) Integer month) {

        LocalDate today = LocalDate.now();

        // year or month가 null이면 현재 날짜로 설정
        if (year == null || month == null) {
            year = today.getYear();
            month = today.getMonthValue();
        }

        LocalDate firstDay = LocalDate.of(year, month, 1);
        int lengthOfMonth = firstDay.lengthOfMonth(); // 그 달이 며칠까지 있는지

        int startDayOfWeek = firstDay.getDayOfWeek().getValue(); // 1=월 ~ 7=일 요일별 숫자

        List<Map<String, Object>> days = new ArrayList<>();

        for (int day = 1; day <= lengthOfMonth; day++) {
            LocalDate date = LocalDate.of(year, month, day);

            Map<String, Object> info = new HashMap<>();
            info.put("date", date.toString());
            info.put("dayOfMonth", day);
            info.put("dayOfWeek", date.getDayOfWeek().getValue());
            days.add(info);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("year", year);
        result.put("month", month);
        result.put("startDayOfWeek", startDayOfWeek);
        result.put("days", days);

        return result;
    }
    
 // 직원 선택 시 가능한 근무 + 이미 배정된 스케줄 반환
    @GetMapping("/hr/schedule/userSchedule")
    @ResponseBody
    public Map<String, Object> getUserSchedule(@RequestParam String userId,
                                               @RequestParam Integer year,
                                               @RequestParam Integer month) {
        Map<String, Object> result = new HashMap<>();

        User_account user = user_accountRepository.findById(userId).orElseThrow();

        // 직원이 가진 role 기준으로 가능한 근무 타입 가져오기
        List<Work_type> possibleWorks = user.getUser_role().stream()
                .flatMap(ur -> ur.getRole_code().getWork_type().stream())
                .distinct()
                .collect(Collectors.toList());

        result.put("possibleWorks", possibleWorks);

        // 이미 배정된 스케줄
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        List<Work_schedule> assignedSchedules = work_scheduleRepository.findByUserAndMonth(userId, start, end);

        result.put("assignedSchedules", assignedSchedules);

        return result;
    }*/
}
