import axios from "axios";
import cookie from "react-cookies";
const SERVER_CONTEXT = "/ScoreManagement";
const SERVER = "http://localhost:9090";

export const endpoints = {

  "login": `${SERVER_CONTEXT}/api/login`,
  "registerstudent": `${SERVER_CONTEXT}/api/register/student`,
  "profile": `${SERVER_CONTEXT}/api/profile`,
  "current-user": `${SERVER_CONTEXT}/api/current-user`,
  "teacher-classes": `${SERVER_CONTEXT}/api/teacher/classes`,
  "teacher-subject-teachers": `${SERVER_CONTEXT}/api/subject-teachers`,
  "teacher-class-detail": `${SERVER_CONTEXT}/api/teacher/classes`,  // + /{classId}
  "teacher-class-scores": `${SERVER_CONTEXT}/api/teacher/classes`,  // + /{classId}/scores
  "teacher-save-scores": `${SERVER_CONTEXT}/api/teacher/classes`,
  "scores-type-list": `${SERVER_CONTEXT}/api/scores/score-types/list`,
  "scores-type-by-class": `${SERVER_CONTEXT}/api/scores/score-types/by-class`,
  "scores-weights": `${SERVER_CONTEXT}/api/scores/score-types/weights`,
  "scores-add-type": `${SERVER_CONTEXT}/api/scores/score-types/add`,
  "scores-remove-type": `${SERVER_CONTEXT}/api/scores/score-types/remove`,
  "scores-configure-weights": `${SERVER_CONTEXT}/api/scores/classes`,  // + /{classId}/scores/configure-weights
  "scores-lock": `${SERVER_CONTEXT}/api/scores/lock`,
  "scores-lock-all": `${SERVER_CONTEXT}/api/scores/lock-all`,
  "scores-save": `${SERVER_CONTEXT}/api/scores/save-scores`,
  "scores-notification": `${SERVER_CONTEXT}/api/scores/send-score-notification`,

  "scores-import-form": `${SERVER_CONTEXT}/api/scores/import-scores-form`,
  "scores-import": `${SERVER_CONTEXT}/api/scores/import-scores`,
  "scores-template": `${SERVER_CONTEXT}/api/scores/scores/template`,
  "scores-export-csv": `${SERVER_CONTEXT}/api/scores/classes`,  // + /{classId}/export-csv
  "scores-export-pdf": `${SERVER_CONTEXT}/api/scores/classes`,  // + /{classId}/export-pdf
  "scores-students-assigned": `${SERVER_CONTEXT}/api/scores/students/assigned`,
  "scores-students-search": `${SERVER_CONTEXT}/api/scores/students/search`,
  "scores-student-detail": `${SERVER_CONTEXT}/api/scores/students`,  // + /{studentCode}/detail
  "scores-student-scores": `${SERVER_CONTEXT}/api/scores/students`, // + /{studentId}/scores?schoolYearId=...
  "scores-available-school-years": `${SERVER_CONTEXT}/api/scores/available-school-years`,
  "classes-by-subject": `${SERVER_CONTEXT}/api/scores/classes/by-subject`,

  "forums": `${SERVER_CONTEXT}/api/forums`,
  "forum-detail": `${SERVER_CONTEXT}/api/forums`,  // + /{forumId}
  "forum-comments": `${SERVER_CONTEXT}/api/forums`, // + /{forumId}/comments
  "forum-by-subject": `${SERVER_CONTEXT}/api/forums/by-subject-teacher`, // + /{subjectTeacherId}
  "forum-teacher": `${SERVER_CONTEXT}/api/forums/teacher`,
  "forum-student": `${SERVER_CONTEXT}/api/forums/student`,

  "student-current": `${SERVER_CONTEXT}/api/student/current-student`,
  "student-scores": `${SERVER_CONTEXT}/api/student/scores`,
  "student-class-info": `${SERVER_CONTEXT}/api/student/class-info`,
  "student-subjects": `${SERVER_CONTEXT}/api/student/subjects`,
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

// API User
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
    console.log("Gửi yêu cầu đăng ký:", endpoints["registerstudent"]);

    // Đảm bảo dữ liệu không có giá trị undefined hoặc null
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

  uploadAvatar: (userId, imageFile) => {
    const formData = new FormData();
    formData.append('id', userId);
    formData.append('image', imageFile);

    console.log("Uploading avatar for user ID:", userId);
    console.log("File name:", imageFile.name);

    // Quan trọng: Không thiết lập Content-Type, để browser tự xử lý boundary
    return axios.post(`${SERVER}${endpoints["profile"]}/upload-avatar`, formData, {
      headers: {
        'Authorization': `Bearer ${cookie.load("user")?.token}`
      }
    });
  }
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

  // Lưu điểm
  saveScores: (classId, subjectTeacherId, schoolYearId, saveMode, scoresData, username) => {
    return API.post(
      `${endpoints["teacher-save-scores"]}/${classId}/scores/save?` +
      `subjectTeacherId=${subjectTeacherId}&schoolYearId=${schoolYearId}` +
      `&saveMode=${saveMode}&username=${username}`,
      scoresData
    );
  }
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

  // Khóa/mở khóa điểm của sinh viên
  lockScore: (studentId, subjectTeacherId, schoolYearId, lock) => {
    return API.post(endpoints["scores-lock"], {
      studentId,
      subjectTeacherId,
      schoolYearId,
      lock
    });
  },

  // Khóa/mở khóa tất cả điểm
  lockAllScores: (classId, subjectTeacherId, schoolYearId, lock) => {
    return API.post(endpoints["scores-lock-all"], {
      classId,
      subjectTeacherId,
      schoolYearId,
      lock
    });
  },

  // Lưu điểm
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

  // Gửi thông báo điểm
  sendScoreNotification: (studentId, subjectName) => {
    return API.post(
      `${endpoints["scores-notification"]}?studentId=${studentId}` +
      `&subjectName=${encodeURIComponent(subjectName)}`
    );
  },

  // Lấy dữ liệu form import điểm
  getImportScoresFormData: () => {
    return API.get(endpoints["scores-import-form"]);
  },

  // Import điểm từ file CSV
  importScores: (file, subjectTeacherId, classId, schoolYearId) => {
    console.log("Sending import with:", { subjectTeacherId, classId, schoolYearId });

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

  getAvailableSchoolYears: (subjectTeacherId, classId) => {
    return API.get(
      `${endpoints["scores-available-school-years"]}?subjectTeacherId=${subjectTeacherId}&classId=${classId}`
    );
  },

  // API để lấy các lớp dựa trên môn học
  getClassesBySubject: (subjectId) => {
    return API.get(`${endpoints["classes-by-subject"]}?subjectId=${subjectId}`);
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

  updateForum: (forumData) => API.post(`${endpoints['forums']}/update`, forumData),
  deleteForum: (forumData) => API.post(`${endpoints['forums']}/delete`, forumData),

};

export const studentApis = {
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
  }
};

export default axios.create({
  baseURL: SERVER
})