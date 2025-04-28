import React, { useState, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { userApis } from "../configs/Apis";
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { 
  faUserPlus, 
  faExclamationCircle, 
  faEnvelope, 
  faUser, 
  faLock, 
  faEye, 
  faEyeSlash, 
  faShieldAlt 
} from '@fortawesome/free-solid-svg-icons';
import "../assets/css/register.css";
import "../assets/css/styles.css";
import "../assets/css/base.css";

const RegisterStudent = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [formData, setFormData] = useState({
    email: '',
    username: '',
    password: '',
    confirmPassword: '',
    avatar: null
  });

  const [passwordMismatch, setPasswordMismatch] = useState(false);
  const fileInputRef = useRef(null);
  const [previewImage, setPreviewImage] = useState(null);
  const [passwordVisibility, setPasswordVisibility] = useState({
    password: false,
    confirmPassword: false
  });

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value
    });

    // Kiểm tra mật khẩu khớp nhau
    if (name === 'confirmPassword' || name === 'password') {
      const password = name === 'password' ? value.trim() : formData.password.trim();
      const confirmPassword = name === 'confirmPassword' ? value.trim() : formData.confirmPassword.trim();
      setPasswordMismatch(password !== confirmPassword && confirmPassword !== '');
    }
  };

  const handleImageChange = (e) => {
    const file = e.target.files[0];
    if (!file) return;

    const validTypes = ["image/png", "image/jpeg", "image/jpg"];
    if (!validTypes.includes(file.type)) {
      alert("Chỉ chấp nhận ảnh PNG, JPG hoặc JPEG!");
      fileInputRef.current.value = '';
      return;
    }

    const reader = new FileReader();
    reader.onloadend = () => {
      const base64String = reader.result;
      setPreviewImage(base64String);
      setFormData({
        ...formData,
        avatar: base64String.split(',')[1]
      });
    };
    reader.readAsDataURL(file);
  };

  const togglePasswordVisibility = (inputId) => {
    const input = document.getElementById(inputId);
    if (input.type === 'password') {
      input.type = 'text';
      setPasswordVisibility(prev => ({
        ...prev,
        [inputId]: true
      }));
    } else {
      input.type = 'password';
      setPasswordVisibility(prev => ({
        ...prev,
        [inputId]: false
      }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (formData.password !== formData.confirmPassword) {
      setError("Mật khẩu xác nhận không khớp!");
      return;
    }

    if (formData.password.length < 6) {
      setError("Mật khẩu phải có ít nhất 6 ký tự");
      return;
    }

    if (!formData.avatar) {
      setError("Vui lòng chọn ảnh đại diện");
      return;
    }

    if (!formData.email.endsWith('@dh.edu.vn')) {
      setError("Email phải có định dạng @dh.edu.vn");
      return;
    }

    try {
      setLoading(true);
      setError("");

      const trimmedPassword = formData.password.trim();

      console.log("Dữ liệu đăng ký:", {
        email: formData.email,
        username: formData.username,
        password: trimmedPassword,
        confirmPassword: trimmedPassword,
        avatar: formData.avatar ? "(base64 data)" : null
      });

      const response = await userApis.registerStudent({
        email: formData.email,
        username: formData.username,
        password: trimmedPassword,
        confirmPassword: trimmedPassword,
        avatar: formData.avatar
      });

      if (response.data && response.data.status === "success") {
        navigate('/login?success=register');
      } else {
        setError(response.data.message || "Đăng ký không thành công");
      }
    } catch (err) {
      console.error("Lỗi đăng ký:", err);

      // Chi tiết lỗi từ server
      console.error("Chi tiết lỗi:", err.response?.data);

      if (err.response && err.response.data) {
        if (typeof err.response.data === 'string') {
          setError(err.response.data);
        } else if (err.response.data.message) {
          setError(err.response.data.message);
        } else {
          setError("Đăng ký không thành công. Vui lòng thử lại sau.");
        }
      } else {
        setError("Không thể kết nối tới máy chủ");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="register-container container">
      <div className="register-card card">
        <div className="register-header">
          <h2 className="register-title">
            <FontAwesomeIcon icon={faUserPlus} className="me-2" /> Đăng ký tài khoản sinh viên
          </h2>
          <p className="text-white-50 mb-0">Nhập thông tin của bạn để tạo tài khoản</p>
        </div>
  
        <div className="register-form">
          {/* Thông báo lỗi */}
          {error && (
            <div className="alert alert-danger mb-4">
              <FontAwesomeIcon icon={faExclamationCircle} className="me-2" /> {error}
            </div>
          )}
  
          <form onSubmit={handleSubmit}>
            <div className="row">
              <div className="col-md-6">
                <div className="avatar-preview mb-4">
                  {previewImage ? (
                    <img src={previewImage} alt="Avatar Preview" />
                  ) : (
                    <FontAwesomeIcon icon={faUser} />
                  )}
                </div>
  
                <div className="mb-4">
                  <label htmlFor="avatar" className="form-label">Ảnh đại diện <span className="text-danger">*</span></label>
                  <input
                    type="file"
                    className="form-control"
                    id="avatar"
                    name="avatar"
                    ref={fileInputRef}
                    accept="image/*"
                    onChange={handleImageChange}
                    required
                  />
                  <small className="form-text text-muted">Chọn ảnh đại diện (jpg, png, jpeg)</small>
                </div>
              </div>
  
              <div className="col-md-6">
                <div className="mb-4">
                  <label htmlFor="email" className="form-label">Email sinh viên <span className="text-danger">*</span></label>
                  <div className="input-group">
                    <span className="input-group-text">
                      <FontAwesomeIcon icon={faEnvelope} />
                    </span>
                    <input
                      type="email"
                      className="form-control"
                      id="email"
                      name="email"
                      value={formData.email}
                      onChange={handleInputChange}
                      placeholder="Nhập email trường cấp (@dh.edu.vn)"
                      required
                    />
                  </div>
                  <small className="form-text text-muted">Vui lòng sử dụng email do trường cấp (@dh.edu.vn)</small>
                </div>
  
                <div className="mb-4">
                  <label htmlFor="username" className="form-label">Tên đăng nhập <span className="text-danger">*</span></label>
                  <div className="input-group">
                    <span className="input-group-text">
                      <FontAwesomeIcon icon={faUser} />
                    </span>
                    <input
                      type="text"
                      className="form-control"
                      id="username"
                      name="username"
                      value={formData.username}
                      onChange={handleInputChange}
                      placeholder="Nhập tên đăng nhập"
                      required
                    />
                  </div>
                </div>
  
                <div className="mb-4">
                  <label htmlFor="password" className="form-label">Mật khẩu <span className="text-danger">*</span></label>
                  <div className="input-group">
                    <span className="input-group-text">
                      <FontAwesomeIcon icon={faLock} />
                    </span>
                    <input
                      type="password"
                      className="form-control"
                      id="password"
                      name="password"
                      value={formData.password}
                      onChange={handleInputChange}
                      minLength="6"
                      required
                    />
                    <button
                      className="btn btn-outline-secondary toggle-password"
                      type="button"
                      onClick={() => togglePasswordVisibility("password")}
                    >
                      <FontAwesomeIcon icon={passwordVisibility.password ? faEyeSlash : faEye} />
                    </button>
                  </div>
                </div>
  
                <div className="mb-4">
                  <label htmlFor="confirmPassword" className="form-label">Xác nhận mật khẩu <span className="text-danger">*</span></label>
                  <div className="input-group">
                    <span className="input-group-text">
                      <FontAwesomeIcon icon={faLock} />
                    </span>
                    <input
                      type="password"
                      className="form-control"
                      id="confirmPassword"
                      name="confirmPassword"
                      value={formData.confirmPassword}
                      onChange={handleInputChange}
                      minLength="6"
                      required
                    />
                    <button
                      className="btn btn-outline-secondary toggle-password"
                      type="button"
                      onClick={() => togglePasswordVisibility("confirmPassword")}
                    >
                      <FontAwesomeIcon icon={passwordVisibility.confirmPassword ? faEyeSlash : faEye} />
                    </button>
                  </div>
                  {passwordMismatch && (
                    <div className="invalid-feedback d-block">
                      Mật khẩu xác nhận không khớp!
                    </div>
                  )}
                </div>
              </div>
            </div>
  
            <div className="password-tips">
              <h6>
                <FontAwesomeIcon icon={faShieldAlt} className="me-2" />
                Yêu cầu mật khẩu:
              </h6>
              <ul>
                <li>Tối thiểu 6 ký tự</li>
                <li>Nên kết hợp chữ cái và số</li>
                <li>Không nên sử dụng thông tin cá nhân dễ đoán</li>
              </ul>
            </div>
  
            <div className="mt-4 d-grid">
              <button
                type="submit"
                className="btn btn-primary btn-register"
                disabled={loading || passwordMismatch}
              >
                {loading ? (
                  <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                ) : (
                  <FontAwesomeIcon icon={faUserPlus} className="me-2" />
                )}
                {loading ? "Đang xử lý..." : "Đăng ký tài khoản"}
              </button>
            </div>
          </form>
  
          <div className="divider">
            <span>hoặc</span>
          </div>
  
          <div className="text-center">
            <p>Đã có tài khoản? <Link to="/login" className="text-primary fw-bold">Đăng nhập</Link></p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default RegisterStudent;