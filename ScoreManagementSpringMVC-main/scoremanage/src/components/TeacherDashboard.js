import React, { useState, useEffect, useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { MyUserContext } from "../App";
import axios from 'axios';
import { endpoints, authApi, SERVER, API } from '../configs/Apis';
import { Alert, Spinner, Col, Card, Button } from 'react-bootstrap';
import "../assets/css/base.css";
import "../assets/css/dashboard.css";
import defaultAvatar from '../assets/images/logo.png';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {
  faComments, faChalkboardTeacher, faExclamationCircle, faCheckCircle, faUserEdit, faUsers, faGraduationCap, faFileImport, faSearch, faUser,
  faChalkboard, faSchool, faPaperPlane
} from '@fortawesome/free-solid-svg-icons';

const TeacherDashboard = () => {
  const [user] = useContext(MyUserContext);
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [teacherInfo, setTeacherInfo] = useState(null);

  useEffect(() => {
    if (!user) {
      navigate("/login");
      return;
    }
    const loadTeacherInfo = async () => {
      try {
        setLoading(true);
        // Use API instead of axios with SERVER
        const res = await API.get(endpoints["current-user"]);

        if (res.data) {
          setTeacherInfo(res.data);
          // Nếu có query param success, hiển thị thông báo
          const urlParams = new URLSearchParams(window.location.search);
          if (urlParams.get('success')) {
            setSuccessMessage("Đăng nhập thành công!");
          }
        }
      } catch (err) {
        console.error("Lỗi khi tải thông tin giảng viên:", err);
        setError("Không thể tải thông tin. Vui lòng thử lại sau.");
      } finally {
        setLoading(false);
      }
    };

    loadTeacherInfo();
  }, [user, navigate]);


  if (loading) {
    return (
      <div className="container d-flex justify-content-center align-items-center" style={{ minHeight: "60vh" }}>
        <Spinner animation="border" role="status" variant="primary">
          <span className="visually-hidden">Đang tải...</span>
        </Spinner>
      </div>
    );
  }

  return (
    <div className="teacher-dashboard">
      <div className="dashboard-header">
        <div className="container">
          <div className="teacher-profile">
            <img src={user?.image || defaultAvatar} alt="Profile" className="profile-avatar" />
            <div className="profile-info">
              <div className="welcome-text">Xin chào, Giảng viên</div>
              <h2>{user?.name || "Giảng viên " + user?.username}</h2>
              <div className="d-flex gap-2">
                <span className="badge bg-light text-dark">
                  {teacherInfo?.roleSpecificInfo?.departmentId?.departmentName || "Khoa CNTT"}
                </span>
                <span className="badge bg-light text-dark">{user?.email || "Email chưa cập nhật"}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="container">
        {successMessage && (
          <Alert variant="success" dismissible onClose={() => setSuccessMessage("")} className="animate-fade-in">
            <FontAwesomeIcon icon={faCheckCircle} className="me-2" />
            {successMessage}
          </Alert>
        )}

        {error && (
          <Alert variant="danger" dismissible onClose={() => setError("")} className="animate-fade-in">
            <FontAwesomeIcon icon={faExclamationCircle} className="me-2" />
            {error}
          </Alert>
        )}

        <div className="row">
          <div className="col-lg-8">
            <div className="row mb-4">
              <div className="col-md-6 mb-3">
                <div className="feature-card">
                  <div className="card-header-custom bg-soft-primary">
                    <FontAwesomeIcon icon={faUsers} className="card-header-icon" />
                    Quản lý lớp học
                  </div>
                  <Card.Body>
                    <Card.Text>
                      Xem danh sách lớp dạy, quản lý sinh viên và điểm số của lớp học bạn phụ trách.
                    </Card.Text>
                    <Link to="/teacher/classes" className="btn btn-primary feature-btn w-100">
                      <FontAwesomeIcon icon={faChalkboard} className="me-2" />
                      Danh sách lớp dạy
                    </Link>
                  </Card.Body>
                </div>
              </div>

              <div className="col-md-6 mb-3">
                <div className="feature-card">
                  <div className="card-header-custom bg-soft-success">
                    <FontAwesomeIcon icon={faFileImport} className="card-header-icon" />
                    Nhập điểm
                  </div>
                  <Card.Body>
                    <Card.Text>
                      Nhập điểm cho sinh viên theo từng môn học và lớp. Nhập từ file CSV nhanh chóng.
                    </Card.Text>
                    <Link to="/teacher/scores/import" className="btn btn-success feature-btn w-100">
                      <FontAwesomeIcon icon={faFileImport} className="me-2" />
                      Nhập điểm từ file
                    </Link>
                  </Card.Body>
                </div>
              </div>
            </div>

            <div className="row mb-4">
              <div className="col-md-6 mb-3">
                <div className="feature-card">
                  <div className="card-header-custom bg-soft-info">
                    <FontAwesomeIcon icon={faSearch} className="card-header-icon" />
                    Tra cứu thông tin
                  </div>
                  <Card.Body>
                    <Card.Text>
                      Tìm kiếm thông tin sinh viên theo MSSV, họ tên hoặc lớp học nhanh chóng.
                    </Card.Text>
                    <Link to="/teacher/students/search" className="btn btn-info feature-btn w-100 text-white">
                      <FontAwesomeIcon icon={faSearch} className="me-2" />
                      Tìm kiếm sinh viên
                    </Link>
                  </Card.Body>
                </div>
              </div>

              <div className="col-md-6 mb-3">
                <div className="feature-card">
                  <div className="card-header-custom bg-soft-warning">
                    <FontAwesomeIcon icon={faComments} className="card-header-icon" />
                    Diễn đàn học tập
                  </div>
                  <Card.Body>
                    <Card.Text>
                      Trao đổi, thảo luận với sinh viên. Giải đáp thắc mắc và chia sẻ tài liệu học tập.
                    </Card.Text>
                    <Link to="/forums" className="btn btn-warning feature-btn w-100">
                      <FontAwesomeIcon icon={faComments} className="me-2" />
                      Truy cập diễn đàn
                    </Link>
                  </Card.Body>
                </div>
              </div>
              <div className="col-md-12 mb-3">
                <div className="feature-card mb-4">
                  <div className="card-header-custom bg-soft-danger">
                    <FontAwesomeIcon icon={faPaperPlane} className="card-header-icon" />
                    Tin nhắn
                  </div>
                  <Card.Body>
                    <Card.Text>
                      Gửi và nhận tin nhắn với sinh viên và đồng nghiệp. Trao đổi thông tin nhanh chóng.
                    </Card.Text>
                    <Link to="/chat" className="btn btn-outline-danger feature-btn w-100">
                      <FontAwesomeIcon icon={faPaperPlane} className="me-2" />
                      Truy cập tin nhắn
                    </Link>
                  </Card.Body>
                </div>
              </div>
            </div>
          </div>

          <div className="col-lg-4">
            <div className="feature-card mb-4">
              <div className="card-header-custom bg-soft-dark">
                <FontAwesomeIcon icon={faUser} className="card-header-icon" />
                Thông tin cá nhân
              </div>
              <Card.Body className="text-center">
                <img
                  src={user?.image || defaultAvatar}
                  alt="Profile"
                  className="rounded-circle mb-1"
                  style={{ width: "80px", height: "80px", objectFit: "cover", border: "3px solid #f8f9fa" }}
                />
                <h5 className="mb-2">{user?.name || user?.username || "Giảng viên"}</h5>
                <span className="badge bg-primary px-3 py-2 mb-3">{user?.role || "Giảng viên"}</span>
                <div className="mb-4 text-start">
                  <p className="mb-2"><strong>Email:</strong> {user?.email || "Chưa cập nhật"}</p>
                  <p className="mb-2"><strong>Khoa:</strong> {teacherInfo?.roleSpecificInfo?.departmentId?.departmentName || "Chưa cập nhật"}</p>
                </div>
                <Link to="/profile" className="btn btn-outline-dark w-100 feature-btn">
                  <FontAwesomeIcon icon={faUserEdit} className="me-2" />
                  Cập nhật thông tin
                </Link>
              </Card.Body>
            </div>


          </div>

        </div>
      </div>
    </div>
  );
};

export default TeacherDashboard;