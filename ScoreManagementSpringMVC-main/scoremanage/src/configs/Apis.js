import axios from "axios";
import cookie from "react-cookies";
const SERVER_CONTEXT = "/ScoreManagement";
const SERVER = "http://localhost:9090";

export const endpoints = {

    "login": `${SERVER_CONTEXT}/api/login`,
    "registerstudent": `${SERVER_CONTEXT}/api/register/student`,
    "profile": `${SERVER_CONTEXT}/api/profile`,

    "current-user": `${SERVER_CONTEXT}/api/current-user`,
    "register": `${SERVER_CONTEXT}/api/users/`,
    "schoolyear": `${SERVER_CONTEXT}/api/schoolyear`,
    "listsubject": `${SERVER_CONTEXT}/api/listsubject`,
    "liststudents": `${SERVER_CONTEXT}/api/listsubject/liststudents`,
    "listscore": `${SERVER_CONTEXT}/api/listscore`,
    "savelistscore": `${SERVER_CONTEXT}/api/savelistscore`,
    "listoldclass": `${SERVER_CONTEXT}/api/listoldclass`,
    "listscoreofstudent": `${SERVER_CONTEXT}/api/listscoreofstudent`,
    "student-info": `${SERVER_CONTEXT}/api/student/info`,
    "teacher-info": `${SERVER_CONTEXT}/api/teacher/info`,
    "teacher-classes": `${SERVER_CONTEXT}/api/teacher/classes`,
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
        return API.post(endpoints["profile"], profileData);
    },
    // Đổi mật khẩu
    changePassword: (passwordData) => {
        return API.post(`${endpoints["profile"]}/change-password`, passwordData);
    },
    // Lấy thông tin chi tiết theo vai trò
    getRoleSpecificInfo: (userId, role) => {
        return API.get(`${endpoints["profile"]}/${userId}/${role}`);
    },
    // Lấy thông tin người dùng hiện tại
    getCurrentUser: () => {
        return API.get(endpoints["current-user"]);
    }
};

export default axios.create({
    baseURL: SERVER
})