import React, { useState, useContext, useEffect, useRef } from 'react';
import { MyUserContext } from "../App";
import { userApis } from "../configs/Apis";
import { Alert } from 'react-bootstrap';
import moment from 'moment';
import "../assets/css/base.css";
import "../assets/css/styles.css";

const Profile = () => {
  const [user] = useContext(MyUserContext);
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState("");
  const [error, setError] = useState("");
  
  // Thông tin người dùng
  const [profileData, setProfileData] = useState({
    id: user?.id || "",
    name: user?.name || "",
    gender: user?.gender === 0 ? "Nữ" : "Nam",
    hometown: user?.hometown || "",
    identifyCard: user?.identifyCard || "",
    birthdate: user?.birthdate ? moment(user.birthdate).format("YYYY-MM-DD") : "",
    phone: user?.phone || "",
  });
  
  // Thông tin đổi mật khẩu
  const [passwordData, setPasswordData] = useState({
    id: user?.id || "",
    currentPassword: "",
    newPassword: "",
    confirmPassword: ""
  });
  
  const [passwordErrors, setPasswordErrors] = useState({
    newPassword: false,
    confirmPassword: false
  });
  
  const fileInputRef = useRef(null);
  const [roleSpecificInfo, setRoleSpecificInfo] = useState(null);
  
  useEffect(() => {
    // Load thông tin chi tiết theo vai trò
    const loadRoleSpecificInfo = async () => {
      try {
        if (user) {
          // API call để lấy thông tin chi tiết theo vai trò
          const response = await userApis.getRoleSpecificInfo(user.id, user.role);
          setRoleSpecificInfo(response.data);
        }
      } catch (err) {
        console.error("Lỗi khi tải thông tin:", err);
      }
    };
    
    loadRoleSpecificInfo();
  }, [user]);
  
  const handleProfileChange = (e) => {
    const { name, value } = e.target;
    setProfileData({
      ...profileData,
      [name]: value
    });
  };
  
  const handlePasswordChange = (e) => {
    const { name, value } = e.target;
    setPasswordData({
      ...passwordData,
      [name]: value
    });
    
    // Kiểm tra định dạng mật khẩu mới
    if (name === 'newPassword') {
      const regex = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{6,}$/;
      setPasswordErrors({
        ...passwordErrors,
        newPassword: value && !regex.test(value)
      });
    }
    
    // Kiểm tra mật khẩu xác nhận
    if (name === 'confirmPassword' || name === 'newPassword') {
      const newPwd = name === 'newPassword' ? value : passwordData.newPassword;
      const confirmPwd = name === 'confirmPassword' ? value : passwordData.confirmPassword;
      
      setPasswordErrors({
        ...passwordErrors,
        confirmPassword: confirmPwd && newPwd !== confirmPwd
      });
    }
  };
  
  const handleProfileSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setSuccess("");
    setError("");
    
    try {
      const formData = new FormData();
      for (const key in profileData) {
        formData.append(key, profileData[key]);
      }
      
      if (fileInputRef.current.files.length > 0) {
        formData.append('imageFile', fileInputRef.current.files[0]);
      }
      
      const response = await userApis.updateProfile(formData);
      if (response.data && response.data.status === "success") {
        setSuccess("Cập nhật thông tin thành công!");
      } else {
        setError("Cập nhật thông tin không thành công!");
      }
    } catch (err) {
      console.error("Lỗi cập nhật:", err);
      setError("Không thể cập nhật thông tin. Vui lòng thử lại sau.");
    } finally {
      setLoading(false);
      
      // Scroll to the top to show the alert
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  };
  
  const handlePasswordSubmit = async (e) => {
    e.preventDefault();
    
    if (passwordErrors.newPassword || passwordErrors.confirmPassword) {
      setError("Vui lòng kiểm tra lại thông tin mật khẩu!");
      return;
    }
    
    setLoading(true);
    setSuccess("");
    setError("");
    
    try {
      const response = await userApis.changePassword(passwordData);
      if (response.data && response.data.status === "success") {
        setSuccess("Đổi mật khẩu thành công!");
        setPasswordData({
          ...passwordData,
          currentPassword: "",
          newPassword: "",
          confirmPassword: ""
        });
      } else {
        setError(response.data?.message || "Đổi mật khẩu không thành công!");
      }
    } catch (err) {
      console.error("Lỗi đổi mật khẩu:", err);
      if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else {
        setError("Không thể đổi mật khẩu. Vui lòng thử lại sau.");
      }
    } finally {
      setLoading(false);
      
      // Scroll to the top to show the alert
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  };
  
  const togglePasswordVisibility = (inputId) => {
    const input = document.getElementById(inputId);
    if (input.type === 'password') {
      input.type = 'text';
      document.querySelector(`[data-target="${inputId}"] i`).classList.remove('fa-eye');
      document.querySelector(`[data-target="${inputId}"] i`).classList.add('fa-eye-slash');
    } else {
      input.type = 'password';
      document.querySelector(`[data-target="${inputId}"] i`).classList.remove('fa-eye-slash');
      document.querySelector(`[data-target="${inputId}"] i`).classList.add('fa-eye');
    }
  };
  
  if (!user) {
    return (
      <div className="container mt-4">
        <Alert variant="warning">
          <i className="fas fa-exclamation-triangle me-2"></i>
          Bạn cần đăng nhập để xem thông tin cá nhân.
        </Alert>
      </div>
    );
  }
  
  return (
    <div className="container mt-4">
      <div className="row mb-4">
        <div className="col-lg-12">
          <h2 className="mb-3">
            <i className="fas fa-user-circle"></i> Thông tin cá nhân
          </h2>
        </div>
      </div>

      {/* Thông báo */}
      {success && (
        <Alert variant="success" dismissible onClose={() => setSuccess("")}>
          <i className="fas fa-check-circle"></i> {success}
        </Alert>
      )}

      {error && (
        <Alert variant="danger" dismissible onClose={() => setError("")}>
          <i className="fas fa-exclamation-circle"></i> {error}
        </Alert>
      )}

      <div className="row">
        {/* Thông tin cá nhân */}
        <div className="col-lg-6 mb-4">
          <div className="card shadow h-100">
            <div className="profile-header p-3">
              <h5 className="card-title mb-0"><i className="fas fa-id-card"></i> Thông tin người dùng</h5>
            </div>
            <div className="card-body text-center">
              <div className="mb-4">
                <img 
                  src={user.image || "/images/default-avatar.jpg"} 
                  className="profile-img mb-3" 
                  alt="Profile" 
                />
                <h4>{user.name}</h4>
                <p className="text-muted">
                  <span className="badge bg-primary">{user.role}</span>
                </p>
              </div>

              <hr />

              <form className="text-start" id="profileForm" onSubmit={handleProfileSubmit}>
                <input type="hidden" name="id" value={profileData.id} />

                <div className="mb-3">
                  <label htmlFor="name" className="form-label">Họ và tên</label>
                  <input 
                    type="text" 
                    className="form-control" 
                    id="name" 
                    name="name" 
                    value={profileData.name} 
                    onChange={handleProfileChange}
                    required 
                  />
                </div>

                <div className="mb-3">
                  <label htmlFor="gender" className="form-label">Giới tính</label>
                  <select 
                    className="form-select" 
                    id="gender" 
                    name="gender"
                    value={profileData.gender}
                    onChange={handleProfileChange}
                  >
                    <option value="Nam">Nam</option>
                    <option value="Nữ">Nữ</option>
                  </select>
                </div>

                <div className="mb-3">
                  <label htmlFor="hometown" className="form-label">Quê quán</label>
                  <input 
                    type="text" 
                    className="form-control" 
                    id="hometown" 
                    name="hometown"
                    value={profileData.hometown}
                    onChange={handleProfileChange}
                  />
                </div>

                <div className="mb-3">
                  <label htmlFor="identifyCard" className="form-label">CMND/CCCD</label>
                  <input 
                    type="text" 
                    className="form-control" 
                    id="identifyCard" 
                    name="identifyCard"
                    value={profileData.identifyCard}
                    onChange={handleProfileChange}
                  />
                </div>

                <div className="mb-3">
                  <label htmlFor="birthdate" className="form-label">Ngày sinh</label>
                  <input 
                    type="date" 
                    className="form-control" 
                    id="birthdate" 
                    name="birthdate"
                    value={profileData.birthdate}
                    onChange={handleProfileChange}
                  />
                </div>

                <div className="mb-3">
                  <label htmlFor="phone" className="form-label">Số điện thoại</label>
                  <input 
                    type="text" 
                    className="form-control" 
                    id="phone" 
                    name="phone"
                    value={profileData.phone}
                    onChange={handleProfileChange}
                  />
                </div>

                <div className="mb-3">
                  <label htmlFor="imageFile" className="form-label">Ảnh đại diện</label>
                  <input 
                    type="file" 
                    className="form-control" 
                    id="imageFile" 
                    name="imageFile"
                    ref={fileInputRef}
                    accept="image/*" 
                  />
                  <small className="form-text text-muted">Để trống nếu không muốn thay đổi ảnh đại diện.</small>
                </div>

                <div className="text-center">
                  <button 
                    type="submit" 
                    className="btn btn-primary"
                    disabled={loading}
                  >
                    {loading ? (
                      <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                    ) : (
                      <i className="fas fa-save me-2"></i>
                    )}
                    Lưu thông tin
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>

        {/* Đổi mật khẩu */}
        <div className="col-lg-6 mb-4">
          <div className="card shadow h-50">
            <div className="profile-header p-3">
              <h5 className="card-title mb-0"><i className="fas fa-key"></i> Đổi mật khẩu</h5>
            </div>
            <div className="card-body">
              <div className="alert alert-info">
                <i className="fas fa-info-circle"></i> Mật khẩu phải có ít nhất 6 ký tự và bao gồm chữ cái và số.
              </div>

              <form id="passwordForm" onSubmit={handlePasswordSubmit}>
                <input type="hidden" name="id" value={passwordData.id} />

                <div className="mb-3">
                  <label htmlFor="currentPassword" className="form-label">Mật khẩu hiện tại <span className="text-danger">*</span></label>
                  <div className="input-group">
                    <input 
                      type="password" 
                      className="form-control" 
                      id="currentPassword" 
                      name="currentPassword"
                      value={passwordData.currentPassword}
                      onChange={handlePasswordChange}
                      required 
                    />
                    <button 
                      className="btn btn-outline-secondary toggle-password" 
                      type="button" 
                      data-target="currentPassword"
                      onClick={() => togglePasswordVisibility("currentPassword")}
                    >
                      <i className="fas fa-eye"></i>
                    </button>
                  </div>
                </div>

                <div className="mb-3">
                  <label htmlFor="newPassword" className="form-label">Mật khẩu mới <span className="text-danger">*</span></label>
                  <div className="input-group">
                    <input 
                      type="password" 
                      className={`form-control ${passwordErrors.newPassword ? 'is-invalid' : ''}`}
                      id="newPassword" 
                      name="newPassword"
                      value={passwordData.newPassword}
                      onChange={handlePasswordChange}
                      required 
                      minLength="6" 
                    />
                    <button 
                      className="btn btn-outline-secondary toggle-password" 
                      type="button" 
                      data-target="newPassword"
                      onClick={() => togglePasswordVisibility("newPassword")}
                    >
                      <i className="fas fa-eye"></i>
                    </button>
                    {passwordErrors.newPassword && (
                      <div className="invalid-feedback">
                        Mật khẩu phải có ít nhất 6 ký tự, bao gồm cả chữ và số.
                      </div>
                    )}
                  </div>
                </div>

                <div className="mb-3">
                  <label htmlFor="confirmPassword" className="form-label">Xác nhận mật khẩu mới <span className="text-danger">*</span></label>
                  <div className="input-group">
                    <input 
                      type="password" 
                      className={`form-control ${passwordErrors.confirmPassword ? 'is-invalid' : ''}`}
                      id="confirmPassword" 
                      name="confirmPassword"
                      value={passwordData.confirmPassword}
                      onChange={handlePasswordChange}
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
                    {passwordErrors.confirmPassword && (
                      <div className="invalid-feedback">
                        Mật khẩu xác nhận không khớp.
                      </div>
                    )}
                  </div>
                </div>

                <div className="text-center">
                  <button 
                    type="submit" 
                    className="btn btn-warning" 
                    id="changePasswordBtn"
                    disabled={loading || passwordErrors.newPassword || passwordErrors.confirmPassword}
                  >
                    {loading ? (
                      <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                    ) : (
                      <i className="fas fa-key me-2"></i>
                    )}
                    Đổi mật khẩu
                  </button>
                </div>
              </form>
            </div>
          </div>

          {/* Thông tin tài khoản */}
          <div className="card shadow mt-4">
            <div className="card-header bg-secondary text-white">
              <h5 className="card-title mb-0"><i className="fas fa-user-lock"></i> Thông tin tài khoản</h5>
            </div>
            <div className="card-body">
              <div className="mb-3 row">
                <label className="col-sm-4 col-form-label fw-bold">Tên đăng nhập:</label>
                <div className="col-sm-8">
                  <input type="text" className="form-control-plaintext" readOnly value={user.username} />
                </div>
              </div>

              <div className="mb-3 row">
                <label className="col-sm-4 col-form-label fw-bold">Email:</label>
                <div className="col-sm-8">
                  <input type="text" className="form-control-plaintext" readOnly value={user.email} />
                </div>
              </div>

              <div className="mb-3 row">
                <label className="col-sm-4 col-form-label fw-bold">Trạng thái:</label>
                <div className="col-sm-8">
                  {user.active ? (
                    <span className="badge bg-success">Hoạt động</span>
                  ) : (
                    <span className="badge bg-danger">Bị khóa</span>
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Thông tin chi tiết theo vai trò */}
      {roleSpecificInfo && (
        <div className="row mt-2">
          <div className="col-lg-12">
            <div className="card shadow">
              <div className="card-header bg-info text-white">
                <h5 className="card-title mb-0">
                  <i className="fas fa-info-circle"></i> 
                  {user.role === 'Student' && "Thông tin sinh viên"}
                  {user.role === 'Teacher' && "Thông tin giảng viên"}
                  {user.role === 'Admin' && "Thông tin quản trị viên"}
                </h5>
              </div>
              <div className="card-body">
                {/* Thông tin sinh viên */}
                {user.role === 'Student' && roleSpecificInfo && (
                  <div className="row mb-3">
                    <div className="col-md-4">
                      <label className="fw-bold">Mã sinh viên:</label>
                      <p>{roleSpecificInfo.id}</p>
                    </div>
                    <div className="col-md-4">
                      <label className="fw-bold">Lớp:</label>
                      <p>{roleSpecificInfo.classId ? roleSpecificInfo.classId.className : 'Chưa phân lớp'}</p>
                    </div>
                    <div className="col-md-4">
                      <label className="fw-bold">Ngành học:</label>
                      <p>
                        {roleSpecificInfo.classId && roleSpecificInfo.classId.majorId 
                          ? roleSpecificInfo.classId.majorId.majorName 
                          : 'Chưa có thông tin'}
                      </p>
                    </div>
                  </div>
                )}

                {/* Thông tin giảng viên */}
                {user.role === 'Teacher' && roleSpecificInfo && (
                  <div className="row mb-3">
                    <div className="col-md-4">
                      <label className="fw-bold">Mã giảng viên:</label>
                      <p>{roleSpecificInfo.id}</p>
                    </div>
                    <div className="col-md-4">
                      <label className="fw-bold">Khoa:</label>
                      <p>
                        {roleSpecificInfo.departmentId 
                          ? roleSpecificInfo.departmentId.departmentName 
                          : 'Chưa phân khoa'}
                      </p>
                    </div>
                    <div className="col-md-4">
                      <label className="fw-bold">Chức vụ:</label>
                      <p>{roleSpecificInfo.position || 'Giảng viên'}</p>
                    </div>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Profile;