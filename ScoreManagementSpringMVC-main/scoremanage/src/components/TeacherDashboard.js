import React, { useState, useEffect, useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { MyUserContext } from "../App";
import axios from 'axios';
import { endpoints, authApi, SERVER, API } from '../configs/Apis';
import { Alert, Spinner, Col, Card, Button } from 'react-bootstrap';
import "../assets/css/base.css";
import "../assets/css/styles.css";
import defaultAvatar from '../assets/images/logo.png';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faComments, faChalkboardTeacher, faExclamationCircle, faCheckCircle, faUserEdit } from '@fortawesome/free-solid-svg-icons';

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
    <div className="container mt-4">
      <h1 className="mb-4">
        <i className="fas fa-chalkboard-teacher me-2"></i>
        Trang quản lý của giảng viên
      </h1>

      {successMessage && (
        <Alert variant="success" dismissible onClose={() => setSuccessMessage("")}>
          <i className="fas fa-check-circle me-2"></i> {successMessage}
        </Alert>
      )}

      {error && (
        <Alert variant="danger" dismissible onClose={() => setError("")}>
          <i className="fas fa-exclamation-circle me-2"></i> {error}
        </Alert>
      )}

      <div className="row mt-4">
        <div className="col-md-6">
          <div className="card mb-4 shadow-sm">
            <div className="card-header bg-primary text-white">
              <i className="fas fa-users me-2"></i>
              Quản lý lớp học
            </div>
            <div className="card-body">
              <Link to="/teacher/classes" className="btn btn-primary btn-lg d-block mb-3">
                <i className="fas fa-chalkboard me-2"></i> Danh sách lớp dạy
              </Link>
              <p>Xem danh sách các lớp học bạn được phân công giảng dạy, quản lý điểm của lớp.</p>
            </div>
          </div>
        </div>

        <div className="col-md-6">
          <div className="card mb-4 shadow-sm">
            <div className="card-header bg-success text-white">
              <i className="fas fa-graduation-cap me-2"></i>
              Nhập điểm
            </div>
            <div className="card-body">
              <Link to="/teacher/scores/import" className="btn btn-success btn-lg d-block mb-3">
                <i className="fas fa-file-import me-2"></i> Nhập điểm từ file
              </Link>
              <p>Nhập điểm cho sinh viên theo từng môn học và lớp.</p>
            </div>
          </div>
        </div>
      </div>

      <div className="row">
        <div className="col-md-6">
          <div className="card mb-4 shadow-sm">
            <div className="card-header bg-info text-white">
              <i className="fas fa-search me-2"></i>
              Tra cứu thông tin
            </div>
            <div className="card-body">
              <Link to="/teacher/students/search" className="btn btn-info btn-lg d-block mb-3">
                <i className="fas fa-search me-2"></i> Tìm kiếm sinh viên
              </Link>
              <p>Tìm kiếm thông tin sinh viên theo mã, tên hoặc lớp.</p>
            </div>
          </div>
        </div>

        <div className="col-md-6">
          <div className="card mb-4 shadow-sm">
            <div className="card-header bg-warning text-dark">
              <i className="fas fa-user me-2"></i>
              Thông tin cá nhân
            </div>
            <div className="card-body">
              <div className="mb-3 text-center">
                <img
                  src={user?.image || defaultAvatar}
                  alt="Profile"
                  className="img-thumbnail rounded-circle"
                  style={{ width: "100px", height: "100px", objectFit: "cover" }}
                />
              </div>
              <h5 className="text-center mb-3">{user?.name || user?.username || "Giảng viên"}</h5>
              <div className="text-center mb-4">
                <span className="badge bg-primary px-3 py-2">{user?.role || "Giảng viên"}</span>
              </div>
              <div className="d-grid">
                <Link to="/profile" className="btn btn-warning">
                  <i className="fas fa-user-edit me-2"></i> Cập nhật thông tin
                </Link>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="row">
        <Col md={4} className="mb-4">
          <Card className="h-100 shadow dashboard-card">
            <Card.Body>
              <div className="d-flex align-items-center mb-3">
                <div className="rounded-icon bg-info me-3">
                  <FontAwesomeIcon icon={faComments} />
                </div>
                <h3 className="card-title mb-0">Diễn đàn học tập</h3>
              </div>
              <Card.Text>
                Trao đổi, thảo luận về nội dung bài học. Đặt câu hỏi và giải đáp thắc mắc của sinh viên.
              </Card.Text>
            </Card.Body>
            <Card.Footer className="bg-transparent border-0 pb-3">
              <Button as={Link} to="/forums" variant="outline-info" className="w-100">
                <FontAwesomeIcon icon={faComments} className="me-2" />
                Truy cập diễn đàn
              </Button>
            </Card.Footer>
          </Card>
        </Col>
      </div>
    </div>
  );
};

export default TeacherDashboard;