import React, { useState, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { userApis } from "../configs/Apis";
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

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value
    });

    // Kiểm tra mật khẩu khớp nhau
    if (name === 'confirmPassword' || name === 'password') {
      const password = name === 'password' ? value : formData.password;
      const confirmPassword = name === 'confirmPassword' ? value : formData.confirmPassword;
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
      document.querySelector(`[data-target=${inputId}] i`).classList.remove('fa-eye');
      document.querySelector(`[data-target=${inputId}] i`).classList.add('fa-eye-slash');
    } else {
      input.type = 'password';
      document.querySelector(`[data-target=${inputId}] i`).classList.remove('fa-eye-slash');
      document.querySelector(`[data-target=${inputId}] i`).classList.add('fa-eye');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (formData.password !== formData.confirmPassword) {
      setError("Mật khẩu xác nhận không khớp!");
      return;
    }
    
    if (!formData.avatar) {
      setError("Vui lòng chọn ảnh đại diện");
      return;
    }
    
    try {
      setLoading(true);
      setError("");
      
      const response = await userApis.registerStudent({
        email: formData.email,
        username: formData.username,
        password: formData.password,
        avatar: formData.avatar
      });
      
      if (response.data && response.data.status === "success") {
        navigate('/login?success=register');
      } else {
        setError(response.data.message || "Đăng ký không thành công");
      }
    } catch (err) {
      console.error("Lỗi đăng ký:", err);
      if (err.response && err.response.data) {
        if (err.response.data.includes("email")) {
          setError("Email không hợp lệ hoặc đã tồn tại trong hệ thống");
        } else if (err.response.data.includes("invalid-email")) {
          setError("Email không tồn tại trong danh sách sinh viên hoặc đã được đăng ký");
        } else {
          setError(err.response.data || "Đăng ký không thành công");
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
            <i className="fas fa-user-plus"></i> Đăng ký tài khoản sinh viên
          </h2>
          <p className="text-white-50 mb-0">Nhập thông tin của bạn để tạo tài khoản</p>
        </div>
        
        <div className="register-form">
          {/* Thông báo lỗi */}
          {error && (
            <div className="alert alert-danger mb-4">
              <i className="fas fa-exclamation-circle me-2"></i> {error}
            </div>
          )}

          <form onSubmit={handleSubmit}>
            <div className="row">
              <div className="col-md-6">
                <div className="avatar-preview mb-4">
                  {previewImage ? (
                    <img src={previewImage} alt="Avatar Preview" />
                  ) : (
                    <i className="fas fa-user"></i>
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
                    <span className="input-group-text"><i className="fas fa-envelope"></i></span>
                    <input 
                      type="email" 
                      className="form-control" 
                      id="email" 
                      name="email"
                      value={formData.email}
                      onChange={handleInputChange}
                      pattern="[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,}$"
                      placeholder="Nhập email trường cấp (@dh.edu.vn)" 
                      required 
                    />
                  </div>
                  <small className="form-text text-muted">Vui lòng sử dụng email do trường cấp (@dh.edu.vn)</small>
                </div>

                <div className="mb-4">
                  <label htmlFor="username" className="form-label">Tên đăng nhập <span className="text-danger">*</span></label>
                  <div className="input-group">
                    <span className="input-group-text"><i className="fas fa-user"></i></span>
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
                    <span className="input-group-text"><i className="fas fa-lock"></i></span>
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
                      data-target="password"
                      onClick={() => togglePasswordVisibility("password")}
                    >
                      <i className="fas fa-eye"></i>
                    </button>
                  </div>
                </div>

                <div className="mb-4">
                  <label htmlFor="confirmPassword" className="form-label">Xác nhận mật khẩu <span className="text-danger">*</span></label>
                  <div className="input-group">
                    <span className="input-group-text"><i className="fas fa-lock"></i></span>
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
                      data-target="confirmPassword"
                      onClick={() => togglePasswordVisibility("confirmPassword")}
                    >
                      <i className="fas fa-eye"></i>
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
              <h6><i className="fas fa-shield-alt me-2"></i>Yêu cầu mật khẩu:</h6>
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
                {loading ? 
                  <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span> : 
                  <i className="fas fa-user-plus me-2"></i>
                }
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