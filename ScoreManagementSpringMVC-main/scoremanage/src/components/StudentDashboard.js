import React, { useState, useEffect, useContext } from 'react';
import { Link, Navigate } from 'react-router-dom';
import { MyUserContext } from "../App";
import axios from 'axios';
import { endpoints, API } from '../configs/Apis';
import { Alert, Spinner, Col, Card, Button, Row } from 'react-bootstrap';
import "../assets/css/base.css";
import "../assets/css/styles.css";
import "../assets/css/dashboard.css";
import defaultAvatar from '../assets/images/logo.png';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {
  faComments, faCheckCircle, faExclamationCircle,
  faChartLine, faUsers, faUserEdit, faBook, faGraduationCap, faPaperPlane
} from '@fortawesome/free-solid-svg-icons';

const StudentDashboard = () => {
  const [user] = useContext(MyUserContext);
  const [student, setStudent] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  useEffect(() => {
    if (!user) return;
    const loadStudentInfo = async () => {
      try {
        setLoading(true);
        // Gọi API student current thay vì current-user để lấy đầy đủ thông tin sinh viên
        const res = await API.get(endpoints["student-current"]);

        if (res.data && res.data.student) {
          // Lưu thông tin sinh viên từ API trả về
          setStudent(res.data.student);

          // Nếu có query param success, hiển thị thông báo
          const urlParams = new URLSearchParams(window.location.search);
          if (urlParams.get('success')) {
            setSuccessMessage("Đăng nhập thành công!");
          }
        }
      } catch (err) {
        console.error("Lỗi khi tải thông tin sinh viên:", err);
        setError("Không thể tải thông tin sinh viên. Vui lòng thử lại sau.");
      } finally {
        setLoading(false);
      }
    };

    loadStudentInfo();
  }, [user]);

  if (!user) {
    return <Navigate to="/login" />;
  }

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
    <div className="teacher-dashboard"> {/* Sử dụng class của teacher */}
      <div className="dashboard-header">
        <div className="container">
          <div className="teacher-profile">
            <img src={student?.image || user?.image || defaultAvatar} alt="Profile" className="profile-avatar" />
            <div className="profile-info">
              <div className="welcome-text">Xin chào, Sinh viên</div>
              <h2>{student?.lastName || user?.name} {student?.firstName || ""}</h2>
              <div className="d-flex gap-2">
                <span className="badge bg-light text-dark">
                  MSSV: {student?.studentCode || "Chưa cập nhật"}
                </span>
                <span className="badge bg-light text-dark">
                  Lớp: {student?.classId?.className || "Chưa cập nhật"}
                </span>
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
                    <FontAwesomeIcon icon={faChartLine} className="card-header-icon" />
                    Điểm số
                  </div>
                  <Card.Body>
                    <Card.Text>
                      Xem bảng điểm của tất cả các môn học và theo dõi kết quả học tập.
                    </Card.Text>
                    <Link to="/student/scores" className="btn btn-primary feature-btn w-100">
                      <FontAwesomeIcon icon={faChartLine} className="me-2" />
                      Xem điểm của tôi
                    </Link>
                  </Card.Body>
                </div>
              </div>

              <div className="col-md-6 mb-3">
                <div className="feature-card">
                  <div className="card-header-custom bg-soft-info">
                    <FontAwesomeIcon icon={faUsers} className="card-header-icon" />
                    Lớp học
                  </div>
                  <Card.Body>
                    <Card.Text>
                      Xem thông tin về lớp học và danh sách các bạn cùng lớp.
                    </Card.Text>
                    <Link to="/student/class-info" className="btn btn-info feature-btn w-100 text-white">
                      <FontAwesomeIcon icon={faUsers} className="me-2" />
                      Thông tin lớp
                    </Link>
                  </Card.Body>
                </div>
              </div>
            </div>

            <div className="row mb-4">
              <div className="col-md-6 mb-3">
                <div className="feature-card">
                  <div className="card-header-custom bg-soft-success">
                    <FontAwesomeIcon icon={faBook} className="card-header-icon" />
                    Môn học
                  </div>
                  <Card.Body>
                    <Card.Text>
                      Xem danh sách môn học bạn đã đăng ký trong học kỳ hiện tại và trước đây.
                    </Card.Text>
                    <Link to="/student/subjects" className="btn btn-success feature-btn w-100">
                      <FontAwesomeIcon icon={faBook} className="me-2" />
                      Môn học đã đăng ký
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
                      Tham gia các diễn đàn học tập để xem thảo luận và đặt câu hỏi về các môn học.
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
                      Gửi và nhận tin nhắn với giảng viên và đồng nghiệp. Trao đổi thông tin nhanh chóng.
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
                <FontAwesomeIcon icon={faGraduationCap} className="card-header-icon" />
                Thông tin cá nhân
              </div>
              <Card.Body className="text-center">
                <img
                  src={student?.image || user?.image || defaultAvatar}
                  alt="Profile"
                  className="rounded-circle mb-3"
                  style={{ width: "80px", height: "80px", objectFit: "cover", border: "3px solid #f8f9fa" }}
                />
                <h5 className="mb-1">{student?.lastName || user?.name} {student?.firstName}</h5>
                <span className="badge bg-primary px-3 py-2 mb-3">{user?.role || "Sinh viên"}</span>
                <div className="mb-4 text-start">
                  <p className="mb-2"><strong>MSSV:</strong> {student?.studentCode || "Chưa cập nhật"}</p>
                  <p className="mb-2"><strong>Email:</strong> {student?.email || user?.email || "Chưa cập nhật"}</p>
                  <p className="mb-2"><strong>Lớp:</strong> {student?.classId?.className || "Chưa cập nhật"}</p>
                  <p className="mb-2"><strong>Chuyên ngành:</strong> {student?.classId?.majorId?.majorName || "Chưa cập nhật"}</p>
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

export default StudentDashboard;