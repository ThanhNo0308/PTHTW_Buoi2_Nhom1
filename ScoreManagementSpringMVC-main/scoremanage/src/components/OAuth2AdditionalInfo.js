import React, { useState, useContext } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { userApis } from "../configs/Apis";
import { MyUserContext } from "../App";
import cookie from "react-cookies";
import "../assets/css/login.css";

const OAuth2AdditionalInfo = () => {
  const { state } = useLocation();
  const navigate = useNavigate();
  const [user, dispatch] = useContext(MyUserContext);
  const [email, setEmail] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  // Nếu không có state (người dùng truy cập trực tiếp URL)
  if (!state || !state.provider) {
    navigate('/login');
    return null;
  }

  const { provider, name, picture } = state;

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!email) {
      setError("Vui lòng nhập email");
      return;
    }

    try {
      setLoading(true);
      setError("");
      
      const res = await userApis.submitAdditionalInfo(provider, email);
      
      if (res.data && res.data.status === "success") {
        const userData = res.data.user || {};
        
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
            navigate("/");
        }
      } else {
        setError(res.data?.message || "Không thể xác thực email. Vui lòng thử lại.");
      }
    } catch (err) {
      console.error("Error submitting additional info:", err);
      if (err.response && err.response.data) {
        setError(err.response.data.message || "Có lỗi xảy ra. Vui lòng thử lại.");
      } else {
        setError("Không thể kết nối tới máy chủ");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container">
      <div className="row justify-content-center mt-5">
        <div className="col-md-6">
          <div className="card shadow">
            <div className="card-header bg-primary text-white">
              <h4 className="mb-0">Thông tin bổ sung</h4>
            </div>
            <div className="card-body">
              {/* Hiển thị lỗi nếu có */}
              {error && (
                <div className="alert alert-danger mb-3">
                  <i className="fas fa-exclamation-circle me-2"></i>
                  <span>{error}</span>
                </div>
              )}
              
              <div className="text-center mb-4">
                {provider === 'facebook' && <i className="fab fa-facebook text-primary fa-4x mb-3"></i>}
                {provider === 'google' && <i className="fab fa-google text-danger fa-4x mb-3"></i>}
                <p>Xin chào <strong>{name}</strong>!</p>
                <p>Chúng tôi cần thêm email của bạn để hoàn tất quá trình đăng nhập.</p>
              </div>

              <form onSubmit={handleSubmit}>
                <div className="mb-3">
                  <label htmlFor="email" className="form-label">Email <span className="text-danger">*</span></label>
                  <input 
                    type="email" 
                    className="form-control" 
                    id="email" 
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="Nhập địa chỉ email của bạn (@dh.edu.vn)" 
                    required 
                  />
                  <small className="text-muted">Vui lòng nhập email do trường cấp (<b>@dh.edu.vn</b>) để xác minh bạn là học sinh hoặc giảng viên của trường.</small>
                </div>

                <div className="d-flex justify-content-between">
                  <button 
                    type="button" 
                    className="btn btn-outline-secondary"
                    onClick={() => navigate('/login')}
                  >
                    Quay lại đăng nhập
                  </button>
                  <button 
                    type="submit" 
                    className="btn btn-primary"
                    disabled={loading}
                  >
                    {loading ? (
                      <>
                        <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                        Đang xử lý...
                      </>
                    ) : (
                      'Hoàn tất đăng nhập'
                    )}
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default OAuth2AdditionalInfo;