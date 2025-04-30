import React, { useState, useContext, useEffect, useRef } from 'react';
import { MyUserContext } from "../App";
import { userApis, API, endpoints } from "../configs/Apis";
import { Alert } from 'react-bootstrap';
import moment from 'moment';
import "../assets/css/base.css";
import "../assets/css/styles.css";
import {
  faUserCircle, faCheckCircle, faExclamationCircle, faIdCard,
  faSave, faKey, faInfoCircle, faLock, faLockOpen, faEye, faEyeSlash, faShieldAlt, faUserLock
} from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

const Profile = () => {
  const [user, dispatch] = useContext(MyUserContext);
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState("");
  const [error, setError] = useState("");
  const [userActive, setUserActive] = useState(true);
  const [justUpdated, setJustUpdated] = useState(false);
  const justUpdatedRef = useRef(false);
  // Thông tin người dùng
  const [profileData, setProfileData] = useState({
    id: user?.id || "",
    name: user?.name || "",
    gender: user?.gender === 0 ? "Nam" : "Nữ",
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
    const loadUserDetail = async () => {
      try {
        if (justUpdatedRef.current) {
          justUpdatedRef.current = false;
          return;
        }
        if (user) {
          let response;

          // Sử dụng API phù hợp với từng vai trò
          if (user.role === 'Student') {
            // Dùng API student-current cho sinh viên
            response = await API.get(endpoints["student-current"]);
            console.log("Student API Response:", response.data);

            if (response.data && response.data.student) {
              // Lưu thông tin sinh viên vào roleSpecificInfo
              const studentData = response.data.student || {};
              setRoleSpecificInfo(studentData);
            
              // Cập nhật trạng thái người dùng
              setUserActive("Active");  // Đặt trạng thái rõ ràng là "Active"
            
              const userData = response.data.user || {};
            
              let formattedBirthdate = "";
              // Ưu tiên lấy từ studentData thay vì userData
              if (studentData.birthdate) {
                formattedBirthdate = moment(parseInt(studentData.birthdate)).format("YYYY-MM-DD");
              } else if (userData.birthdate) {
                formattedBirthdate = moment(userData.birthdate).format("YYYY-MM-DD");
              } else if (user.birthdate) {
                formattedBirthdate = moment(user.birthdate).format("YYYY-MM-DD");
              }
            
              console.log("Birthdate from API (student):", studentData.birthdate);
              console.log("Formatted birthdate:", formattedBirthdate);
            
              // Cập nhật thông tin cơ bản
              setProfileData({
                id: userData.id || user.id || "",
                name: userData.name || user.name || "",
                gender: (studentData.gender === 0 || user.gender === 0) ? "Nam" : "Nữ", 
                hometown: studentData.hometown || user.hometown || "",
                identifyCard: studentData.identifyCard || user.identifyCard || "",
                birthdate: formattedBirthdate,
                phone: studentData.phone || user.phone || "",
              });
            }
          } else {
            // Các vai trò khác vẫn dùng getCurrentUser
            response = await userApis.getCurrentUser();
            console.log("API Response:", response.data);

            if (response.data && response.data.user) {
              const userData = response.data.user;
              setUserActive(userData.active);

              // Cập nhật state với dữ liệu từ API
              setProfileData({
                id: userData.id || user.id || "",
                name: userData.name || user.name || "",
                gender: userData.gender === 0 ? "Nam" : "Nữ",
                hometown: userData.hometown || "",
                identifyCard: userData.identifyCard || "",
                birthdate: userData.birthdate ? moment(userData.birthdate).format("YYYY-MM-DD") : "",
                phone: userData.phone || "",
              });

              // Thêm logic để cập nhật roleSpecificInfo
              if (response.data.roleSpecificInfo) {
                console.log("Role specific info:", response.data.roleSpecificInfo);
                setRoleSpecificInfo(response.data.roleSpecificInfo);
              }
            }
          }
        }
      } catch (err) {
        console.error("Lỗi khi tải thông tin chi tiết người dùng:", err);
        setError("Không thể tải thông tin chi tiết. Vui lòng thử lại sau.");
      }
    };

    loadUserDetail();
  }, [user]);
  const handleProfileChange = (e) => {
    const { name, value } = e.target;
    setProfileData({
      ...profileData,
      [name]: value
    });
  };

  const isUserActive = (status) => {
    console.log("Checking status:", status);

    // Luôn trả về true nếu trạng thái là "Active"
    if (status === "Active") {
      return true;
    }

    if (status === null || status === undefined) {
      return true;  // Default to active if undefined
    }

    if (typeof status === 'boolean') {
      return status;
    }

    if (typeof status === 'string') {
      const normalizedStatus = status.trim().toLowerCase();
      return normalizedStatus === 'active' || normalizedStatus === 'true';
    }

    return true;  // Default to active
  };

  const profileImgStyle = {
    width: "150px",
    height: "150px",
    objectFit: "cover",
    borderRadius: "50%",
    border: "5px solid #e9ecef"
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
      let newImageUrl = null;

      // 1. Xử lý upload ảnh trước nếu có
      if (fileInputRef.current.files.length > 0) {
        console.log("File selected:", fileInputRef.current.files[0].name);

        try {
          const avatarResponse = await userApis.uploadAvatar(
            profileData.id,
            fileInputRef.current.files[0]
          );

          console.log("Avatar upload response:", avatarResponse.data);

          if (avatarResponse.data && avatarResponse.data.imageUrl) {
            // Lưu URL mới để cập nhật một lần duy nhất
            newImageUrl = avatarResponse.data.imageUrl;

            // Cập nhật ảnh hiển thị ngay lập tức
            const profileImg = document.querySelector("img[alt='Profile']");
            if (profileImg) {
              profileImg.src = `${newImageUrl}?t=${new Date().getTime()}`; // Thêm timestamp tránh cache
            }
          }
        } catch (uploadErr) {
          console.error("Error uploading avatar:", uploadErr);
          setError("Không thể upload ảnh đại diện. Chi tiết: " +
            (uploadErr.response?.data?.message || uploadErr.message));
          setLoading(false);
          return;
        }
      }

      // 2. Cập nhật thông tin người dùng sau khi upload ảnh
      const response = await userApis.updateProfile(profileData);

      // 3. Cập nhật context user một lần duy nhất với tất cả thông tin
      dispatch({
        type: "update",
        payload: {
          ...user,
          name: profileData.name,
          gender: profileData.gender === "Nam" ? 0 : 1,
          hometown: profileData.hometown,
          identifyCard: profileData.identifyCard,
          birthdate: profileData.birthdate, // Đảm bảo định dạng đúng
          phone: profileData.phone,
          image: newImageUrl || user.image
        }
      });
      
      // Đánh dấu là vừa cập nhật để tránh gọi API lại
      justUpdatedRef.current = true;

      setProfileData({
        ...profileData,
        birthdate: profileData.birthdate  // Đảm bảo giữ nguyên giá trị mới
      });

      // 4. Thông báo thành công
      setSuccess("Cập nhật thông tin thành công!");

    } catch (err) {
      console.error("Lỗi cập nhật:", err);
      setError("Không thể cập nhật thông tin. Vui lòng thử lại sau.");
    } finally {
      setLoading(false);
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
          <FontAwesomeIcon icon={faExclamationCircle} className="me-2" />
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
            <FontAwesomeIcon icon={faUserCircle} className="me-2" /> Thông tin cá nhân
          </h2>
        </div>
      </div>

      {/* Thông báo */}
      {success && (
        <Alert variant="success" dismissible onClose={() => setSuccess("")}>
          <FontAwesomeIcon icon={faCheckCircle} className="me-2" /> {success}
        </Alert>
      )}

      {error && (
        <Alert variant="danger" dismissible onClose={() => setError("")}>
          <FontAwesomeIcon icon={faExclamationCircle} className="me-2" /> {error}
        </Alert>
      )}

      <div className="row">
        {/* Thông tin cá nhân */}
        <div className="col-lg-6 mb-4">
          <div className="card shadow h-100">
            <div className="profile-header p-3">
              <h5 className="card-title mb-0"><FontAwesomeIcon icon={faIdCard} className="me-2" /> Thông tin người dùng</h5>
            </div>
            <div className="card-body text-center">
              <div className="mb-4">
                <img
                  src={user.image || "/images/default-avatar.jpg"}
                  style={profileImgStyle}
                  className="mb-3"
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
                      <FontAwesomeIcon icon={faSave} className="me-2" />
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
              <h5 className="card-title mb-0"><FontAwesomeIcon icon={faKey} className="me-2" /> Đổi mật khẩu</h5>
            </div>
            <div className="card-body">
              <div className="alert alert-info">
                <FontAwesomeIcon icon={faInfoCircle} className="me-2" /> Mật khẩu phải có ít nhất 6 ký tự và bao gồm chữ cái và số.
              </div>

              <form id="passwordForm" onSubmit={handlePasswordSubmit}>
                <input type="hidden" name="id" value={passwordData.id} />

                <div className="mb-3">
                  <label htmlFor="currentPassword" className="form-label">
                    <FontAwesomeIcon icon={faLock} className="me-2" />
                    Mật khẩu hiện tại <span className="text-danger">*</span>
                  </label>
                  <div className="input-group">
                    <span className="input-group-text">
                      <FontAwesomeIcon icon={faKey} className="me-2" />
                    </span>
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
                      <FontAwesomeIcon icon={faEye} className="me-2" />
                    </button>
                  </div>
                </div>

                <div className="mb-3">
                  <label htmlFor="newPassword" className="form-label">
                    <FontAwesomeIcon icon={faKey} className="me-2" />
                    Mật khẩu mới <span className="text-danger">*</span>
                  </label>
                  <div className="input-group">
                    <span className="input-group-text">
                      <FontAwesomeIcon icon={faLockOpen} className="me-2" />
                    </span>
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
                      <FontAwesomeIcon icon={faEye} className="me-2" />
                    </button>
                    {passwordErrors.newPassword && (
                      <div className="invalid-feedback">
                        Mật khẩu phải có ít nhất 6 ký tự, bao gồm cả chữ và số.
                      </div>
                    )}
                  </div>
                </div>

                <div className="mb-3">
                  <label htmlFor="confirmPassword" className="form-label">
                    <FontAwesomeIcon icon={faCheckCircle} className="me-2" />
                    Xác nhận mật khẩu mới <span className="text-danger">*</span>
                  </label>
                  <div className="input-group">
                    <span className="input-group-text">
                      <FontAwesomeIcon icon={faShieldAlt} className="me-2" />
                    </span>
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
                      <FontAwesomeIcon icon={faEye} className="me-2" />
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
                      <FontAwesomeIcon icon={faKey} className="me-2" />
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
              <h5 className="card-title mb-0"><FontAwesomeIcon icon={faUserLock} className="me-2" /> Thông tin tài khoản</h5>
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
                  {isUserActive(userActive) ? (
                    <span className="badge bg-success">Hoạt động</span>
                  ) : (
                    <span className="badge bg-danger">Bị khóa</span>
                  )}
                </div>
              </div>
            </div>
          </div>

          {/* Thông tin sinh viên (nếu vai trò là sinh viên) */}
          {user.role === 'Student' && roleSpecificInfo && (
            <div className="card shadow mt-4">
              <div className="card-header bg-info text-white">
                <h5 className="card-title mb-0">
                  <FontAwesomeIcon icon={faInfoCircle} className="me-2" />
                  Thông tin sinh viên
                </h5>
              </div>
              <div className="card-body">
                <div className="row mb-3">
                  <div className="col-md-4">
                    <label className="fw-bold">Mã sinh viên:</label>
                    <p>{roleSpecificInfo.studentCode || roleSpecificInfo.id || "-"}</p>
                  </div>
                  <div className="col-md-4">
                    <label className="fw-bold">Lớp:</label>
                    <p>{roleSpecificInfo.classId?.className || 'Chưa phân lớp'}</p>
                  </div>
                  <div className="col-md-4">
                    <label className="fw-bold">Ngành học:</label>
                    <p>{roleSpecificInfo.classId?.majorId?.majorName || 'Chưa có thông tin'}</p>
                  </div>
                  <div className="col-md-4">
                    <label className="fw-bold">Khoa:</label>
                    <p>{roleSpecificInfo.classId?.majorId?.departmentId?.departmentName || 'Chưa có thông tin'}</p>
                  </div>
                  <div className="col-md-4">
                    <label className="fw-bold">Hệ đào tạo:</label>
                    <p>{roleSpecificInfo.classId?.majorId?.trainingTypeId?.trainingTypeName || 'Chưa có thông tin'}</p>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Thông tin giảng viên (nếu vai trò là giảng viên) */}
          {user.role === 'Teacher' && roleSpecificInfo && (
            <div className="card shadow mt-4">
              <div className="card-header bg-info text-white">
                <h5 className="card-title mb-0">
                  <FontAwesomeIcon icon={faInfoCircle} className="me-2" />
                  Thông tin giảng viên
                </h5>
              </div>
              <div className="card-body">
                <div className="row mb-3">
                  <div className="col-md-4">
                    <label className="fw-bold">Mã giảng viên:</label>
                    <p>{roleSpecificInfo.id}</p>
                  </div>
                  <div className="col-md-4">
                    <label className="fw-bold">Khoa:</label>
                    <p>{roleSpecificInfo.departmentId ? roleSpecificInfo.departmentId.departmentName : 'Chưa phân khoa'}</p>
                  </div>
                  <div className="col-md-4">
                    <label className="fw-bold">Chức vụ:</label>
                    <p>{roleSpecificInfo.position || 'Giảng viên'}</p>
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default Profile;