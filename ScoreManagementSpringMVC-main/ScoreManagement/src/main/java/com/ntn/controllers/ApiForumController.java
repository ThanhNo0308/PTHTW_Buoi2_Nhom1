package com.ntn.controllers;

import com.ntn.pojo.Forum;
import com.ntn.pojo.Forumcomment;
import com.ntn.pojo.Schoolyear;
import com.ntn.pojo.Student;
import com.ntn.pojo.Subjectteacher;
import com.ntn.pojo.Teacher;
import com.ntn.pojo.User;
import com.ntn.service.ForumCommentService;
import com.ntn.service.ForumService;
import com.ntn.service.SchoolYearService;
import com.ntn.service.StudentService;
import com.ntn.service.SubjectTeacherService;
import com.ntn.service.TeacherService;
import com.ntn.service.UserService;
import java.util.ArrayList;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ApiForumController {

    @Autowired
    private ForumService forumService;

    @Autowired
    private ForumCommentService forumCommentService;

    @Autowired
    private UserService userService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private SubjectTeacherService subjectTeacherService;

    @Autowired
    private SchoolYearService schoolYearService;

    @Autowired
    private StudentService studentService;

    // Lấy danh sách tất cả diễn đàn
    @GetMapping("/forums")
    public ResponseEntity<Map<String, Object>> getAllForums() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Forum> forums = forumService.getForums();
            response.put("success", true);
            response.put("forums", forums);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Lỗi khi lấy danh sách diễn đàn: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Lấy diễn đàn theo môn học của giáo viên
    @GetMapping("/forums/by-subject-teacher/{subjectTeacherId}")
    public ResponseEntity<Map<String, Object>> getForumsBySubjectTeacher(@PathVariable Integer subjectTeacherId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Forum> forums = forumService.getForumBySubjectTeacher(subjectTeacherId);
            response.put("success", true);
            response.put("forums", forums);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Lỗi khi lấy diễn đàn theo môn học: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Lấy chi tiết diễn đàn và các comment
    @GetMapping("/forums/{forumId}")
    public ResponseEntity<Map<String, Object>> getForumDetail(@PathVariable Integer forumId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Forum forum = forumService.getForumById(forumId);
            if (forum == null) {
                response.put("success", false);
                response.put("error", "Không tìm thấy diễn đàn");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            List<Forumcomment> comments = forumCommentService.getCommentsByForumId(forumId);

            response.put("success", true);
            response.put("forum", forum);
            response.put("comments", comments);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Lỗi khi lấy chi tiết diễn đàn: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Thêm diễn đàn mới
    @PostMapping("/forums/add")
    public ResponseEntity<Map<String, Object>> addForum(@RequestBody Map<String, Object> payload, Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (authentication == null) {
                response.put("success", false);
                response.put("error", "Bạn cần đăng nhập để thêm diễn đàn");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String title = (String) payload.get("title");
            String description = (String) payload.get("description");
            String content = (String) payload.get("content");
            Integer subjectTeacherId = (Integer) payload.get("subjectTeacherId");

            if (title == null || description == null || content == null || subjectTeacherId == null) {
                response.put("success", false);
                response.put("error", "Thiếu thông tin cần thiết");
                return ResponseEntity.badRequest().body(response);
            }

            String username;
            if (authentication.getPrincipal() instanceof UserDetails) {
                username = ((UserDetails) authentication.getPrincipal()).getUsername();
            } else {
                username = authentication.getPrincipal().toString();
            }

            User currentUser = userService.getUserByUn(username);

            Forum forum = new Forum();
            forum.setTitle(title);
            forum.setDescription(description);
            forum.setContent(content);
            forum.setCreatedAt(new Date());
            forum.setUserId(currentUser);

            Subjectteacher subjectTeacher = subjectTeacherService.getSubjectTeacherById(subjectTeacherId);
            if (subjectTeacher == null) {
                response.put("success", false);
                response.put("error", "Không tìm thấy thông tin giáo viên-môn học");
                return ResponseEntity.badRequest().body(response);
            }

            forum.setSubjectTeacherId(subjectTeacher);

            boolean result = forumService.addForum(forum);
            if (result) {
                response.put("success", true);
                response.put("message", "Thêm diễn đàn thành công");
                response.put("forumId", forum.getId());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("error", "Có lỗi xảy ra khi thêm diễn đàn");
                return ResponseEntity.internalServerError().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Lỗi khi thêm diễn đàn: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Thêm bình luận mới
    @PostMapping("/forums/{forumId}/comments")
    public ResponseEntity<Map<String, Object>> addComment(@PathVariable Integer forumId, @RequestBody Map<String, Object> payload, Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (authentication == null) {
                response.put("success", false);
                response.put("error", "Bạn cần đăng nhập để bình luận");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String title = (String) payload.get("title");
            String content = (String) payload.get("content");
            Integer parentCommentId = (Integer) payload.get("parentCommentId"); // Có thể null nếu là comment gốc

            if (title == null || content == null) {
                response.put("success", false);
                response.put("error", "Thiếu tiêu đề hoặc nội dung bình luận");
                return ResponseEntity.badRequest().body(response);
            }

            String username;
            if (authentication.getPrincipal() instanceof UserDetails) {
                username = ((UserDetails) authentication.getPrincipal()).getUsername();
            } else {
                username = authentication.getPrincipal().toString();
            }
            User currentUser = userService.getUserByUn(username);

            // Kiểm tra forum tồn tại
            Forum forum = forumService.getForumById(forumId);
            if (forum == null) {
                response.put("success", false);
                response.put("error", "Không tìm thấy diễn đàn");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Forumcomment comment = new Forumcomment();
            comment.setTitle(title);
            comment.setContent(content);
            comment.setCreatedAt(new Date());
            comment.setForumId(forum);
            comment.setUserId(currentUser);

            // Nếu là phản hồi comment, thiết lập parentCommentId
            if (parentCommentId != null) {
                Forumcomment parentComment = forumCommentService.getCommentById(parentCommentId);
                if (parentComment == null) {
                    response.put("success", false);
                    response.put("error", "Không tìm thấy bình luận gốc");
                    return ResponseEntity.badRequest().body(response);
                }
                comment.setParentCommentId(parentComment);
            }

            boolean result = forumCommentService.addComment(comment);
            if (result) {
                response.put("success", true);
                response.put("message", "Thêm bình luận thành công");
                response.put("commentId", comment.getId());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("error", "Có lỗi xảy ra khi thêm bình luận");
                return ResponseEntity.internalServerError().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Lỗi khi thêm bình luận: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Xóa bình luận
    @DeleteMapping("/forums/comments/{commentId}")
    public ResponseEntity<Map<String, Object>> deleteComment(@PathVariable Integer commentId, Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (authentication == null) {
                response.put("success", false);
                response.put("error", "Bạn cần đăng nhập để xóa bình luận");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String username;
            if (authentication.getPrincipal() instanceof UserDetails) {
                username = ((UserDetails) authentication.getPrincipal()).getUsername();
            } else {
                username = authentication.getPrincipal().toString();
            }
            User currentUser = userService.getUserByUn(username);

            // Lấy comment cần xóa
            Forumcomment comment = forumCommentService.getCommentById(commentId);
            if (comment == null) {
                response.put("success", false);
                response.put("error", "Không tìm thấy bình luận");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Kiểm tra quyền xóa (chỉ người tạo comment hoặc admin mới được xóa)
            if (!comment.getUserId().getId().equals(currentUser.getId())
                    && !currentUser.getRole().toString().equals("Admin")) {
                response.put("success", false);
                response.put("error", "Bạn không có quyền xóa bình luận này");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            boolean result = forumCommentService.deleteComment(commentId);
            if (result) {
                response.put("success", true);
                response.put("message", "Xóa bình luận thành công");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("error", "Có lỗi xảy ra khi xóa bình luận");
                return ResponseEntity.internalServerError().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Lỗi khi xóa bình luận: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Lấy danh sách các diễn đàn cho giáo viên (theo các môn đang dạy)
    @GetMapping("/forums/teacher")
    public ResponseEntity<Map<String, Object>> getTeacherForums(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Xác thực người dùng
            String username;
            if (authentication.getPrincipal() instanceof UserDetails) {
                username = ((UserDetails) authentication.getPrincipal()).getUsername();
            } else {
                username = authentication.getPrincipal().toString();
            }

            // Lấy thông tin User trước
            User user = userService.getUserByUn(username);
            if (user == null) {
                response.put("success", false);
                response.put("error", "Không tìm thấy thông tin người dùng");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Sau đó lấy Teacher từ email của User
            Teacher teacher = teacherService.getTeacherByEmail(user.getEmail());
            if (teacher == null) {
                response.put("success", false);
                response.put("error", "Không tìm thấy thông tin giáo viên");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Lấy diễn đàn cho giáo viên
            List<Forum> forums = forumService.getForumByTeacher(teacher.getId());

            response.put("success", true);
            response.put("forums", forums);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace(); // In chi tiết lỗi
            response.put("success", false);
            response.put("error", "Lỗi khi lấy danh sách diễn đàn: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Lấy danh sách các diễn đàn cho sinh viên (theo các môn đã đăng ký)
    @GetMapping("/forums/student")
    public ResponseEntity<Map<String, Object>> getStudentForums(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (authentication == null) {
                response.put("success", false);
                response.put("error", "Bạn cần đăng nhập để xem diễn đàn");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String username;
            if (authentication.getPrincipal() instanceof UserDetails) {
                username = ((UserDetails) authentication.getPrincipal()).getUsername();
            } else {
                username = authentication.getPrincipal().toString();
            }
            User currentUser = userService.getUserByUn(username);

            // Lấy diễn đàn cho sinh viên
            List<Forum> forums = forumService.getForumByStudent(currentUser.getId());

            response.put("success", true);
            response.put("forums", forums);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Lỗi khi lấy danh sách diễn đàn: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Lấy danh sách phân công môn học của giảng viên ở DIỄN ĐÀN
    @GetMapping("/forums/subject-teachers")
    public ResponseEntity<?> getTeacherSubjects(
            @RequestParam String username,
            @RequestParam(required = false) Integer schoolYearId) {
        try {
            // Lấy User trước
            User user = userService.getUserByUn(username);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "error", "Không tìm thấy thông tin người dùng"));
            }

            // Sau đó lấy Teacher từ email của User
            Teacher teacher = teacherService.getTeacherByEmail(user.getEmail());

            if (teacher == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "error", "Không tìm thấy thông tin giáo viên"));
            }

            // Lấy danh sách phân công môn học của giảng viên - lọc theo học kỳ nếu có
            List<Subjectteacher> subjectTeachers;
            if (schoolYearId != null) {
                subjectTeachers = subjectTeacherService.getSubjectTeachersByTeacherIdAndSchoolYearId(teacher.getId(), schoolYearId);
            } else {
                subjectTeachers = subjectTeacherService.getSubjectTeachersByTeacherId(teacher.getId());
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "subjectTeachers", subjectTeachers
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    // Xóa diễn đàn
    @PostMapping("/forums/delete")
    public ResponseEntity<Map<String, Object>> deleteForum(@RequestBody Map<String, Object> payload, Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Kiểm tra đăng nhập
            if (authentication == null) {
                response.put("success", false);
                response.put("error", "Bạn cần đăng nhập để xóa diễn đàn");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            Integer forumId = (Integer) payload.get("forumId");
            if (forumId == null) {
                response.put("success", false);
                response.put("error", "Thiếu ID diễn đàn");
                return ResponseEntity.badRequest().body(response);
            }

            // Lấy forum cần xóa
            Forum forum = forumService.getForumById(forumId);
            if (forum == null) {
                response.put("success", false);
                response.put("error", "Không tìm thấy diễn đàn");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Lấy thông tin người dùng hiện tại
            String username;
            if (authentication.getPrincipal() instanceof UserDetails) {
                username = ((UserDetails) authentication.getPrincipal()).getUsername();
            } else {
                username = authentication.getPrincipal().toString();
            }
            User currentUser = userService.getUserByUn(username);

            // Kiểm tra quyền xóa (chỉ người tạo, admin, hoặc giáo viên môn học)
            boolean isOwner = forum.getUserId().getId().equals(currentUser.getId());
            boolean isAdmin = "Admin".equals(currentUser.getRole().toString());

            boolean isTeacherOfSubject = false;
            if ("Teacher".equals(currentUser.getRole().toString())) {
                // Xác định giáo viên bằng email thay vì userid
                Teacher teacher = teacherService.getTeacherByEmail(currentUser.getEmail());
                if (teacher != null && forum.getSubjectTeacherId() != null
                        && forum.getSubjectTeacherId().getTeacherId() != null) {
                    isTeacherOfSubject = teacher.getId().equals(forum.getSubjectTeacherId().getTeacherId().getId());
                }
            }

            if (!isOwner && !isAdmin && !isTeacherOfSubject) {
                response.put("success", false);
                response.put("error", "Bạn không có quyền xóa diễn đàn này");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            boolean result = forumService.deleteForum(forumId);
            if (result) {
                response.put("success", true);
                response.put("message", "Xóa diễn đàn thành công");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("error", "Có lỗi xảy ra khi xóa diễn đàn");
                return ResponseEntity.internalServerError().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Lỗi khi xóa diễn đàn: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Cập nhật diễn đàn
    @PostMapping("/forums/update")
    public ResponseEntity<Map<String, Object>> updateForum(@RequestBody Map<String, Object> payload, Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (authentication == null) {
                response.put("success", false);
                response.put("error", "Bạn cần đăng nhập để cập nhật diễn đàn");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            Integer id = (Integer) payload.get("id");
            String title = (String) payload.get("title");
            String description = (String) payload.get("description");
            String content = (String) payload.get("content");
            Integer subjectTeacherId = (Integer) payload.get("subjectTeacherId");

            if (id == null || title == null || description == null || content == null) {
                response.put("success", false);
                response.put("error", "Thiếu thông tin cần thiết");
                return ResponseEntity.badRequest().body(response);
            }

            // Lấy forum hiện tại
            Forum existingForum = forumService.getForumById(id);
            if (existingForum == null) {
                response.put("success", false);
                response.put("error", "Không tìm thấy diễn đàn");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Lấy thông tin người dùng hiện tại
            String username;
            if (authentication.getPrincipal() instanceof UserDetails) {
                username = ((UserDetails) authentication.getPrincipal()).getUsername();
            } else {
                username = authentication.getPrincipal().toString();
            }
            User currentUser = userService.getUserByUn(username);

            // Kiểm tra quyền chỉnh sửa (chỉ người tạo, admin, hoặc giáo viên môn học)
            boolean isOwner = existingForum.getUserId().getId().equals(currentUser.getId());
            boolean isAdmin = "Admin".equals(currentUser.getRole().toString());

            boolean isTeacherOfSubject = false;
            if ("Teacher".equals(currentUser.getRole().toString())) {
                // Xác định giáo viên bằng email thay vì userid
                Teacher teacher = teacherService.getTeacherByEmail(currentUser.getEmail());
                if (teacher != null && existingForum.getSubjectTeacherId() != null
                        && existingForum.getSubjectTeacherId().getTeacherId() != null) {
                    isTeacherOfSubject = teacher.getId().equals(existingForum.getSubjectTeacherId().getTeacherId().getId());
                }
            }

            if (!isOwner && !isAdmin && !isTeacherOfSubject) {
                response.put("success", false);
                response.put("error", "Bạn không có quyền chỉnh sửa diễn đàn này");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            // Cập nhật thông tin diễn đàn
            existingForum.setTitle(title);
            existingForum.setDescription(description);
            existingForum.setContent(content);

            // Nếu là admin và muốn đổi subjectTeacher
            if (isAdmin && subjectTeacherId != null) {
                Subjectteacher subjectTeacher = subjectTeacherService.getSubjectTeacherById(subjectTeacherId);
                if (subjectTeacher == null) {
                    response.put("success", false);
                    response.put("error", "Không tìm thấy thông tin giáo viên-môn học");
                    return ResponseEntity.badRequest().body(response);
                }
                existingForum.setSubjectTeacherId(subjectTeacher);
            }

            boolean result = forumService.updateForum(existingForum);
            if (result) {
                response.put("success", true);
                response.put("message", "Cập nhật diễn đàn thành công");
                response.put("forumId", existingForum.getId());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("error", "Có lỗi xảy ra khi cập nhật diễn đàn");
                return ResponseEntity.internalServerError().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Lỗi khi cập nhật diễn đàn: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Lấy danh sách học kỳ
    @GetMapping("/forums/school-years")
    public ResponseEntity<Map<String, Object>> getAvailableSchoolYears(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Lấy thông tin người dùng hiện tại
            String username;
            if (authentication.getPrincipal() instanceof UserDetails) {
                username = ((UserDetails) authentication.getPrincipal()).getUsername();
            } else {
                username = authentication.getPrincipal().toString();
            }
            User currentUser = userService.getUserByUn(username);

            List<Schoolyear> availableSchoolYears;

            // Phân biệt xử lý giữa giáo viên và sinh viên
            if ("Teacher".equals(currentUser.getRole().toString())) {
                // Sửa lỗi: Lấy teacher từ email thay vì username
                Teacher teacher = teacherService.getTeacherByEmail(currentUser.getEmail());
                if (teacher == null) {
                    response.put("success", false);
                    response.put("error", "Không tìm thấy thông tin giáo viên");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }

                // Lấy các học kỳ mà giáo viên có dạy môn học
                availableSchoolYears = schoolYearService.getSchoolYearsByTeacher(teacher.getId());
            } else if ("Student".equals(currentUser.getRole().toString())) {
                // Sửa lỗi: Lấy student từ email thay vì username
                List<Student> students = studentService.getStudentbyEmail(currentUser.getEmail());
                Student student = students != null && !students.isEmpty() ? students.get(0) : null;
                if (student == null) {
                    response.put("success", false);
                    response.put("error", "Không tìm thấy thông tin sinh viên");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }

                availableSchoolYears = schoolYearService.getSchoolYearsByStudent(student.getId());
            } else {
                // Admin xem tất cả học kỳ
                availableSchoolYears = schoolYearService.getAllSchoolYears();
            }

            // Đánh dấu học kỳ hiện tại
            int currentSchoolYearId = schoolYearService.getCurrentSchoolYearId();
            List<Map<String, Object>> enhancedSchoolYears = new ArrayList<>();

            for (Schoolyear sy : availableSchoolYears) {
                Map<String, Object> syInfo = new HashMap<>();
                syInfo.put("id", sy.getId());
                syInfo.put("nameYear", sy.getNameYear());
                syInfo.put("semesterName", sy.getSemesterName());
                syInfo.put("yearStart", sy.getYearStart());
                syInfo.put("yearEnd", sy.getYearEnd());
                syInfo.put("isCurrent", sy.getId() == currentSchoolYearId);
                enhancedSchoolYears.add(syInfo);
            }

            response.put("success", true);
            response.put("schoolYears", enhancedSchoolYears);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Lỗi khi lấy danh sách học kỳ: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Lọc diễn đàn theo học kỳ và môn học
    @GetMapping("/forums/filter")
    public ResponseEntity<Map<String, Object>> getFilteredForums(
            @RequestParam(required = false) Integer subjectTeacherId,
            @RequestParam(required = false) Integer schoolYearId,
            Authentication authentication) {

        Map<String, Object> response = new HashMap<>();
        try {
            // Lấy thông tin người dùng hiện tại
            String username;
            if (authentication.getPrincipal() instanceof UserDetails) {
                username = ((UserDetails) authentication.getPrincipal()).getUsername();
            } else {
                username = authentication.getPrincipal().toString();
            }
            User currentUser = userService.getUserByUn(username);

            List<Forum> forums;

            // Phân biệt xử lý giữa giáo viên và sinh viên
            if ("Teacher".equals(currentUser.getRole().toString())) {
                // Sửa lỗi: Lấy teacher từ email thay vì username
                Teacher teacher = teacherService.getTeacherByEmail(currentUser.getEmail());
                if (teacher == null) {
                    response.put("success", false);
                    response.put("error", "Không tìm thấy thông tin giáo viên");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }

                // Lọc theo môn học và học kỳ
                if (subjectTeacherId != null) {
                    forums = forumService.getForumBySubjectTeacher(subjectTeacherId);
                } else if (schoolYearId != null) {
                    forums = forumService.getForumByTeacherAndSchoolYear(teacher.getId(), schoolYearId);
                } else {
                    forums = forumService.getForumByTeacher(teacher.getId());
                }
            } else if ("Student".equals(currentUser.getRole().toString())) {
                // Sửa lỗi: Lấy student từ email thay vì username
                List<Student> students = studentService.getStudentbyEmail(currentUser.getEmail());
                Student student = students != null && !students.isEmpty() ? students.get(0) : null;
                if (student == null) {
                    response.put("success", false);
                    response.put("error", "Không tìm thấy thông tin sinh viên");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }

                // Lọc theo môn học và học kỳ
                if (subjectTeacherId != null) {
                    forums = forumService.getForumBySubjectTeacher(subjectTeacherId);
                } else if (schoolYearId != null) {
                    forums = forumService.getForumByStudentAndSchoolYear(student.getId(), schoolYearId);
                } else {
                    forums = forumService.getForumByStudent(student.getId());
                }
            } else {
                // Admin xem tất cả diễn đàn, với bộ lọc nếu có
                if (subjectTeacherId != null) {
                    forums = forumService.getForumBySubjectTeacher(subjectTeacherId);
                } else if (schoolYearId != null) {
                    forums = forumService.getForumBySchoolYear(schoolYearId);
                } else {
                    forums = forumService.getForums();
                }
            }

            response.put("success", true);
            response.put("forums", forums);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Lỗi khi lấy danh sách diễn đàn: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
