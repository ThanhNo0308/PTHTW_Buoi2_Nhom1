import axios from "axios";
import cookie from "react-cookies";
const SERVER_CONTEXT = "/ScoreManagement";
const SERVER = "http://localhost:9090";

export const endpoints = {
  // API User
  "login": `${SERVER_CONTEXT}/api/login`,
  "registerstudent": `${SERVER_CONTEXT}/api/register/student`,
  "profile": `${SERVER_CONTEXT}/api/profile`,
  "current-user": `${SERVER_CONTEXT}/api/current-user`,

  // API TeacherClasses
  "teacher-classes": `${SERVER_CONTEXT}/api/teacherclass/classes`,
  "teacher-class-detail": `${SERVER_CONTEXT}/api/teacherclass/classes`,  // + /{classId}
  "teacher-class-scores": `${SERVER_CONTEXT}/api/teacherclass/classes`,  // + /{classId}/scores

  // API TypeScores
  "scores-type-list": `${SERVER_CONTEXT}/api/typescores/score-types/list`,
  "scores-type-by-class": `${SERVER_CONTEXT}/api/typescores/score-types/by-class`,
  "scores-weights": `${SERVER_CONTEXT}/api/typescores/score-types/weights`,
  "scores-add-type": `${SERVER_CONTEXT}/api/typescores/score-types/add`,
  "scores-remove-type": `${SERVER_CONTEXT}/api/typescores/score-types/remove`,

  // API Scores
  "scores-configure-weights": `${SERVER_CONTEXT}/api/scores/classes`,  // + /{classId}/scores/configure-weights
  "scores-save-draft": `${SERVER_CONTEXT}/api/scores/save-scores-draft`,
  "scores-save": `${SERVER_CONTEXT}/api/scores/save-scores`,
  "scores-available-school-years": `${SERVER_CONTEXT}/api/scores/available-school-years`,
  "scores-import-form": `${SERVER_CONTEXT}/api/scores/import-scores-form`,
  "scores-template": `${SERVER_CONTEXT}/api/scores/scores/template`,
  "scores-import": `${SERVER_CONTEXT}/api/scores/import-scores`,
  "scores-export-csv": `${SERVER_CONTEXT}/api/scores/classes`,  // + /{classId}/export-csv
  "scores-export-pdf": `${SERVER_CONTEXT}/api/scores/classes`,  // + /{classId}/export-pdf

  //API Teacher 
  "scores-students-assigned": `${SERVER_CONTEXT}/api/teacher/students/assigned`,
  "scores-students-search": `${SERVER_CONTEXT}/api/teacher/students/search`,
  "scores-student-detail": `${SERVER_CONTEXT}/api/teacher/students`,  // + /{studentCode}/detail
  "scores-student-scores": `${SERVER_CONTEXT}/api/teacher/students`, // + /{studentId}/scores?schoolYearId=...
  "admin-users": `${SERVER_CONTEXT}/api/teacher/admin-users`,
  "send-unlock-request": `${SERVER_CONTEXT}/api/teacher/send-unlock-request`,

  // API Forum
  "forums": `${SERVER_CONTEXT}/api/forums`,
  "forum-detail": `${SERVER_CONTEXT}/api/forums`,  // + /{forumId}
  "forum-comments": `${SERVER_CONTEXT}/api/forums`, // + /{forumId}/comments
  "forum-by-subject": `${SERVER_CONTEXT}/api/forums/by-subject-teacher`, // + /{subjectTeacherId}
  "forum-teacher": `${SERVER_CONTEXT}/api/forums/teacher`,
  // API ForumStudent
  "forum-student": `${SERVER_CONTEXT}/api/forums/student`,
  "teacher-subject-teachers": `${SERVER_CONTEXT}/api/forums/subject-teachers`,

  // API Student
  "student-current": `${SERVER_CONTEXT}/api/student/current-student`,
  "student-scores": `${SERVER_CONTEXT}/api/student/scores`,
  "student-class-info": `${SERVER_CONTEXT}/api/student/class-info`,
  "student-subjects": `${SERVER_CONTEXT}/api/student/subjects`,
  "student-course-registration": `${SERVER_CONTEXT}/api/student/course-registration`,
  "student-register-course": `${SERVER_CONTEXT}/api/student/course-registration/register`,
  "student-drop-course": `${SERVER_CONTEXT}/api/student/course-registration/drop`,
}

// Cấu hình axios với token
export const authApi = () => {
  return axios.create({
    baseURL: SERVER,
    headers: {
      "Authorization": `Bearer ${cookie.load("user")?.token}`
    }
  })
}

// API với interceptor tự động gắn token
export const API = axios.create({
  baseURL: SERVER
});

// Thêm interceptor để gắn token xác thực vào mỗi request nếu có
API.interceptors.request.use((config) => {
  const user = cookie.load("user");
  if (user && user.token) {
    config.headers.Authorization = `Bearer ${user.token}`;
  }
  return config;
});

export const userApis = {
  // Đăng nhập người dùng
  login: (username, password, role) => {
    return axios.post(`${SERVER}${endpoints["login"]}`, {
      username,
      password,
      role
    }, {
      headers: {
        'Content-Type': 'application/json'
      }
    });
  },

  // Đăng ký sinh viên
  registerStudent: (data) => {
    const cleanData = Object.entries(data).reduce((acc, [key, value]) => {
      if (value !== null && value !== undefined) {
        acc[key] = value;
      }
      return acc;
    }, {});

    return API.post(endpoints["registerstudent"], cleanData, {
      headers: {
        'Content-Type': 'application/json'
      }
    });
  },

  // Cập nhật thông tin cá nhân
  updateProfile: (profileData) => {
    return API.post(endpoints["profile"], profileData, {
      headers: {
        'Content-Type': 'application/json'
      }
    });
  },

  // Đổi mật khẩu
  changePassword: (passwordData) => {
    return API.post(`${endpoints["profile"]}/change-password`, passwordData);
  },

  // Lấy thông tin người dùng hiện tại
  getCurrentUser: () => {
    return API.get(endpoints["current-user"]);
  },

  // Cập nhật avatar
  uploadAvatar: (userId, imageFile) => {
    const formData = new FormData();
    formData.append('id', userId);
    formData.append('image', imageFile);

    // Không thiết lập Content-Type, để browser tự xử lý boundary
    return axios.post(`${SERVER}${endpoints["profile"]}/upload-avatar`, formData, {
      headers: {
        'Authorization': `Bearer ${cookie.load("user")?.token}`
      }
    });
  },
};

export const teacherClassApis = {
  // Lấy danh sách lớp học của giáo viên
  getTeacherClasses: (username) => {
    return API.get(`${endpoints["teacher-classes"]}?username=${username}`);
  },

  // Lấy thông tin phân công môn học
  getTeacherSubjects: (username) => {
    return API.get(`${endpoints["teacher-subject-teachers"]}?username=${username}`);
  },

  // Lấy thông tin chi tiết lớp học
  getClassDetail: (classId, username) => {
    return API.get(`${endpoints["teacher-class-detail"]}/${classId}?username=${username}`);
  },

  // Lấy điểm của lớp
  getClassScores: (classId, subjectTeacherId, schoolYearId, username) => {
    return API.get(
      `${endpoints["teacher-class-scores"]}/${classId}/scores?` +
      `subjectTeacherId=${subjectTeacherId}&schoolYearId=${schoolYearId}&username=${username}`
    );
  },

};

export const scoreApis = {
  // Lấy danh sách loại điểm
  getScoreTypeList: () => {
    return API.get(`${endpoints["scores-type-list"]}?_=${Date.now()}`); // Thêm timestamp để tránh cache
  },

  // Lấy danh sách loại điểm theo lớp
  getScoreTypesByClass: (classId, subjectTeacherId, schoolYearId) => {
    return API.get(
      `${endpoints["scores-type-by-class"]}?classId=${classId}` +
      `&subjectTeacherId=${subjectTeacherId}&schoolYearId=${schoolYearId}`
    );
  },

  // Lấy trọng số điểm
  getScoreWeights: (classId, subjectTeacherId, schoolYearId) => {
    return API.get(
      `${endpoints["scores-weights"]}?classId=${classId}` +
      `&subjectTeacherId=${subjectTeacherId}&schoolYearId=${schoolYearId}`
    );
  },

  // Thêm loại điểm mới
  addScoreType: (scoreType, weight, classId, subjectTeacherId, schoolYearId) => {
    return API.post(endpoints["scores-add-type"], {
      scoreType,
      weight,
      classId,
      subjectTeacherId,
      schoolYearId
    });
  },

  // Xóa loại điểm
  removeScoreType: (scoreType, classId, subjectTeacherId, schoolYearId) => {
    return API.post(endpoints["scores-remove-type"], {
      scoreType,
      classId,
      subjectTeacherId,
      schoolYearId
    });
  },

  // Cấu hình trọng số điểm
  configureWeights: (classId, subjectTeacherId, schoolYearId, weights) => {
    return API.post(
      `${endpoints["scores-configure-weights"]}/${classId}/scores/configure-weights?` +
      `subjectTeacherId=${subjectTeacherId}&schoolYearId=${schoolYearId}`,
      weights
    );
  },

  // Lưu điểm nháp 
  saveScoresDraft: (subjectTeacherId, schoolYearId, scores) => {
    return API.post(
      endpoints["scores-save-draft"],
      {
        subjectTeacherId,
        schoolYearId,
        scores
      }
    );
  },

  // Lưu điểm chính thức
  saveScores: (subjectTeacherId, schoolYearId, scores, locked = false) => {
    return API.post(
      endpoints["scores-save"],
      {
        subjectTeacherId,
        schoolYearId,
        scores,
        locked
      }
    );
  },

  // Lấy dữ liệu form import điểm
  getImportScoresFormData: () => {
    return API.get(endpoints["scores-import-form"]);
  },

  // Import điểm từ file CSV
  importScores: (file, subjectTeacherId, classId, schoolYearId) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('subjectTeacherId', subjectTeacherId);
    formData.append('classId', classId);
    formData.append('schoolYearId', schoolYearId);

    return axios.post(`${SERVER}${SERVER_CONTEXT}/api/scores/import-scores`, formData, {
      headers: {
        'Authorization': `Bearer ${cookie.load("user")?.token}`,
        'Content-Type': 'multipart/form-data'
      }
    });
  },

  // Tải file mẫu nhập điểm
  getScoreTemplate: () => {
    return API.get(endpoints["scores-template"]);
  },

  // Xuất điểm ra PDF
  exportScoresToPdf: (classId, subjectTeacherId, schoolYearId) => {
    return API.get(
      `${endpoints["scores-export-pdf"]}/${classId}/export-pdf?subjectTeacherId=${subjectTeacherId}&schoolYearId=${schoolYearId}`
    );
  },

  // Xuất điểm ra CSV
  exportScoresToCsv: (classId, subjectTeacherId, schoolYearId) => {
    return API.get(
      `${endpoints["scores-export-csv"]}/${classId}/export-csv?subjectTeacherId=${subjectTeacherId}&schoolYearId=${schoolYearId}`
    );
  },

  // Tìm kiếm sinh viên
  searchStudents: (query, type = 'name') => {
    return API.get(
      `${endpoints["scores-students-search"]}?query=${encodeURIComponent(query)}&type=${type}`
    );
  },

  // Lấy danh sách sinh viên được phân công
  getAssignedStudents: () => {
    return API.get(`${endpoints["scores-students-assigned"]}`);
  },

  // Xem chi tiết sinh viên
  getStudentDetail: (studentCode) => {
    return API.get(`${endpoints["scores-student-detail"]}/${studentCode}/detail`);
  },

  // Xem điểm của sinh viên
  getStudentScores: (studentCode, schoolYearId = null) => {
    let url = `${endpoints["scores-student-scores"]}/${studentCode}/scores`;
    if (schoolYearId) {
      url += `?schoolYearId=${schoolYearId}`;
    }
    return API.get(url);
  },

  // Lấy danh sách năm học-học kì khi import điểm
  getAvailableSchoolYears: (subjectTeacherId, classId) => {
    return API.get(
      `${endpoints["scores-available-school-years"]}?subjectTeacherId=${subjectTeacherId}&classId=${classId}`
    );
  },
};

export const forumApis = {
  // Lấy tất cả diễn đàn
  getAllForums: () => {
    return API.get(endpoints["forums"]);
  },

  // Lấy diễn đàn theo giáo viên (dành cho giáo viên đăng nhập)
  getTeacherForums: () => {
    return API.get(endpoints["forum-teacher"]);
  },

  // Lấy diễn đàn theo sinh viên (dành cho sinh viên đăng nhập)
  getStudentForums: () => {
    return API.get(endpoints["forum-student"]);
  },

  // Lấy diễn đàn theo môn học
  getForumsBySubjectTeacher: (subjectTeacherId) => {
    return API.get(`${endpoints["forum-by-subject"]}/${subjectTeacherId}`);
  },

  // Lấy chi tiết diễn đàn và các bình luận
  getForumDetail: (forumId) => {
    return API.get(`${endpoints["forum-detail"]}/${forumId}`);
  },

  // Thêm diễn đàn mới
  addForum: (forumData) => {
    return API.post(endpoints["forums"] + "/add", forumData);
  },

  // Thêm bình luận mới
  addComment: (forumId, commentData) => {
    return API.post(`${endpoints["forum-comments"]}/${forumId}/comments`, commentData);
  },

  // Xóa bình luận
  deleteComment: (commentId) => {
    return API.delete(`${endpoints["forums"]}/comments/${commentId}`);
  },

  // Cập nhật diễn đàn
  updateForum: (forumData) => API.post(`${endpoints['forums']}/update`, forumData),

  // Xóa diễn đàn
  deleteForum: (forumData) => API.post(`${endpoints['forums']}/delete`, forumData),

};

export const studentApis = {
  // Lấy thông tin sinh viên hiện tại
  getCurrentStudent: () => {
    return API.get(endpoints["current-student"]);
  },

  // Lấy điểm của sinh viên đăng nhập
  getStudentScores: (schoolYearId = null) => {
    let url = endpoints["student-scores"];
    if (schoolYearId) {
      url += `?schoolYearId=${schoolYearId}`;
    }
    return API.get(url);
  },

  // Lấy thông tin lớp và danh sách sinh viên trong lớp
  getClassInfo: () => {
    return API.get(endpoints["student-class-info"]);
  },

  // Lấy thông tin môn học đã đăng ký
  getEnrolledSubjects: (schoolYearId = null) => {
    let url = endpoints["student-subjects"];
    if (schoolYearId) {
      url += `?schoolYearId=${schoolYearId}`;
    }
    return API.get(url);
  },

  getAvailableCourses: () => {
    return API.get(endpoints["student-course-registration"]);
  },

  registerCourse: (subjectTeacherId) => {
    return API.post(`${endpoints["student-register-course"]}?subjectTeacherId=${subjectTeacherId}`);
  },

  dropCourse: (enrollmentId) => {
    return API.delete(`${endpoints["student-drop-course"]}?enrollmentId=${enrollmentId}`);
  }
};

export default axios.create({
  baseURL: SERVER
})