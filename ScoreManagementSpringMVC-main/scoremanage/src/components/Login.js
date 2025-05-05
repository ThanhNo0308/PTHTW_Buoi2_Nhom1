import React, { useContext, useState } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { MyUserContext } from "../App";
import { userApis } from "../configs/Apis";
import cookie from "react-cookies";
import "../assets/css/login.css";
import logo from '../assets/images/logo.png';

import { FacebookLogin } from '@greatsumini/react-facebook-login';
import { GoogleLogin } from '@react-oauth/google';

const Login = () => {
  const [user, dispatch] = useContext(MyUserContext);
  const navigate = useNavigate();
  const location = useLocation();

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [role, setRole] = useState("Student");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  // Lấy đường dẫn chuyển hướng sau khi đăng nhập từ state, mặc định là trang chủ
  const { from } = location.state || { from: "/" };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!username || !password) {
      setError("Vui lòng nhập đầy đủ thông tin.");
      return;
    }

    try {
      setLoading(true);
      setError("");
      console.log("Đang gửi:", { username, password, role });
      const res = await userApis.login(username, password, role);
      console.log("Response:", res);

      // Kiểm tra dữ liệu trả về có đúng định dạng không
      if (res.data && typeof res.data === 'object') {
        console.log("Parsed response:", res.data);

        if (res.data.status === "success") {
          // Lưu thông tin người dùng vào context
          const userData = res.data.user || {};
          console.log("User data:", userData);

          // Đảm bảo token được lưu trong userData
          if (!userData.token && res.data.token) {
            userData.token = res.data.token;
          }

          // Đảm bảo các trường cần thiết tồn tại
          if (!userData.username) {
            userData.username = username; // Sử dụng username đăng nhập
          }

          userData.role = res.data.role;

          if (!["Student", "Teacher", "Admin"].includes(userData.role)) {
            setError("Vai trò người dùng không hợp lệ.");
            setLoading(false);
            return;
          }

          // Lưu vào context và cookie
          dispatch({
            "type": "login",
            "payload": userData
          });

          // Lưu cookie với token
          cookie.save("user", userData, { path: "/" });

          console.log("Saved user data:", userData);

          // Chuyển hướng dựa vào vai trò
          switch (userData.role) {
            case "Student":
              navigate("/student/dashboard?success=true");
              break;
            case "Teacher":
              navigate("/teacher/dashboard?success=true");
              break;
            case "Admin":
              navigate("/admin/dashboard?success=true");
              break;
            default:
              navigate(from);
          }
        } else {
          setError(res.data.message || "Đăng nhập không thành công");
        }
      } else {
        console.error("Invalid response format:", res.data);
        setError("Định dạng phản hồi không hợp lệ");
      }
    } catch (err) {
      console.error("Lỗi đăng nhập:", err);
      if (err.response && err.response.data) {
        setError(err.response.data.message || "Đăng nhập không thành công");
      } else {
        setError("Không thể kết nối tới máy chủ");
      }
    } finally {
      setLoading(false);
    }
  };

  // Xử lý đăng nhập Google thành công
  const handleGoogleSuccess = async (response) => {
    try {
      setOauthLoading(true);
      setError("");
      console.log("Google login success", response);

      // Gửi ID token về server để xác thực
      const res = await userApis.oauth2Login("google", response.credential);
      handleOAuth2Response(res);
    } catch (err) {
      console.error("Google login error:", err);
      handleOAuth2Error(err);
    } finally {
      setOauthLoading(false);
    }
  };

  // Xử lý đăng nhập Google thất bại
  const handleGoogleFailure = (error) => {
    console.error("Google login failed:", error);
    setError("Đăng nhập Google thất bại. Vui lòng thử lại.");
    setOauthLoading(false);
  };

  // Xử lý đăng nhập Facebook
  const responseFacebook = async (response) => {
    try {
      setOauthLoading(true);
      setError("");
      console.log("Facebook login success", response);

      // Nếu hủy đăng nhập
      if (!response.accessToken) {
        setError("Đăng nhập Facebook bị hủy.");
        setOauthLoading(false);
        return;
      }

      // Gửi access token về server để xác thực
      const res = await userApis.oauth2Login("facebook", response.accessToken);
      handleOAuth2Response(res);
    } catch (err) {
      console.error("Facebook login error:", err);
      handleOAuth2Error(err);
    } finally {
      setOauthLoading(false);
    }
  };

  // Xử lý phản hồi từ API sau khi đăng nhập OAuth2
  const handleOAuth2Response = (res) => {
    if (res.data && res.data.status === "success") {
      const userData = res.data.user || {};

      // Đảm bảo token được lưu trong userData
      if (!userData.token && res.data.token) {
        userData.token = res.data.token;
      }

      userData.role = res.data.role;

      // Lưu vào context và cookie
      dispatch({
        "type": "login",
        "payload": userData
      });

      cookie.save("user", userData, { path: "/" });

      // Nếu cần bổ sung thông tin
      if (res.data.needAdditionalInfo) {
        navigate("/oauth2/additional-info", {
          state: {
            provider: res.data.provider,
            name: res.data.name,
            picture: res.data.picture
          }
        });
        return;
      }

      // Chuyển hướng dựa vào vai trò
      switch (userData.role) {
        case "Student":
          navigate("/student/dashboard?success=true");
          break;
        case "Teacher":
          navigate("/teacher/dashboard?success=true");
          break;
        case "Admin":
          navigate("/admin/dashboard?success=true");
          break;
        default:
          navigate(from);
      }
    } else {
      setError(res.data?.message || "Đăng nhập không thành công");
    }
  };

  // Xử lý lỗi OAuth2
  const handleOAuth2Error = (err) => {
    if (err.response && err.response.data) {
      if (err.response.data.needAdditionalInfo) {
        // Chuyển đến form nhập thông tin bổ sung
        navigate("/oauth2/additional-info", {
          state: err.response.data
        });
        return;
      }
      setError(err.response.data.message || "Đăng nhập không thành công");
    } else {
      setError("Không thể kết nối tới máy chủ");
    }
  };

  const handleCloseAlert = () => {
    setError("");
  };

  return (
    <div className="login-container container">
      <div className="row justify-content-center">
        <div className="col-xl-10">
          <div className="login-card card">
            <div className="card-body p-0">
              <div className="row">
                {/* Login Form */}
                <div className="col-lg-6">
                  <div className="login-form-area">
                    <div className="text-center mb-4">
                      <img src={logo} alt="School Logo" className="school-logo" />
                      <h3 className="login-title text-center">Đăng nhập hệ thống</h3>
                    </div>

                    {/* Hiển thị lỗi nếu có */}
                    {error && (
                      <div className="box_alert_fail_login alert alert-danger mb-4">
                        <i className="fas fa-exclamation-circle me-2"></i> {error}
                        <span className="closebtn" style={{ float: "right", cursor: "pointer" }} onClick={handleCloseAlert}>&times;</span>
                      </div>
                    )}

                    <form onSubmit={handleSubmit}>
                      <div className="role-selector">
                        <div className="form-floating">
                          <select
                            className="form-select"
                            id="role"
                            value={role}
                            onChange={(e) => setRole(e.target.value)}
                            required
                          >
                            <option value="">Chọn vai trò của bạn</option>
                            <option value="Admin">Giáo vụ</option>
                            <option value="Teacher">Giảng viên</option>
                            <option value="Student">Sinh viên</option>
                          </select>
                          <label htmlFor="role"><i className="fas fa-user-tag me-2"></i>Vai trò</label>
                        </div>
                      </div>

                      <div className="form-floating mb-4">
                        <input
                          type="text"
                          className="form-control"
                          id="username"
                          placeholder="Nhập username..."
                          value={username}
                          onChange={(e) => setUsername(e.target.value)}
                          required
                        />
                        <label htmlFor="username"><i className="fas fa-user me-2"></i>Tên đăng nhập</label>
                      </div>

                      <div className="form-floating mb-4">
                        <input
                          type="password"
                          className="form-control"
                          id="pwd"
                          placeholder="Nhập mật khẩu..."
                          value={password}
                          onChange={(e) => setPassword(e.target.value)}
                          required
                        />
                        <label htmlFor="pwd"><i className="fas fa-lock me-2"></i>Mật khẩu</label>
                      </div>

                      <div className="d-grid mb-4">
                        <button
                          type="submit"
                          className="btn btn-primary btn-login"
                          disabled={loading}
                        >
                          {loading ?
                            <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span> :
                            <i className="fas fa-sign-in-alt me-2"></i>
                          }
                          {loading ? "Đang đăng nhập..." : "Đăng nhập"}
                        </button>
                      </div>
                    </form>

                    <div className="text-center mb-4">
                      <p className="divider-text">
                        <span className="bg-light">Hoặc đăng nhập với</span>
                      </p>
                      <div className="social-login-buttons">
                        {/* Google Login Button */}
                        <div className="mb-2 d-flex justify-content-center">
                          <GoogleLogin
                            onSuccess={handleGoogleSuccess}
                            onError={handleGoogleFailure}
                            type="standard"
                            theme="filled_blue"
                            text="signin_with"
                            shape="rectangular"
                            locale="vi"
                            disabled={oauthLoading}
                          />
                        </div>

                        {/* Facebook Login Button */}
                        <div className="d-flex justify-content-center">
                          <FacebookLogin
                            appId="1229596068700241" // Thay bằng Facebook App ID của bạn
                            fields="name,email,picture"
                            callback={responseFacebook}
                            cssClass="btn btn-facebook btn-block"
                            icon="fa-facebook-f"
                            textButton={oauthLoading ? "Đang đăng nhập..." : "Đăng nhập với Facebook"}
                            disabled={oauthLoading}
                          />
                        </div>
                      </div>
                    </div>

                    <div className="text-center">
                      <p className="mb-0">Chưa có tài khoản?
                        <Link to="/register" className="text-primary fw-bold ms-1">Đăng ký</Link>
                      </p>
                    </div>
                  </div>
                </div>

                {/* Right block */}
                <div className="col-lg-6 d-none d-lg-block">
                  <div className="account-block">
                    <div className="overlay"></div>
                    <div className="position-absolute bottom-0 start-0 p-4 text-white">
                      <h4>Hệ thống Quản lý Điểm số</h4>
                      <p className="mb-0">Trường Đại học MilkyWay</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;