import React, { useState, useEffect, useContext } from 'react';
import { Link, Navigate } from 'react-router-dom';
import { MyUserContext } from "../App";
import axios from 'axios';
import { endpoints, API } from '../configs/Apis';
import { Alert, Spinner, Col, Card, Button } from 'react-bootstrap';
import "../assets/css/base.css";
import "../assets/css/styles.css";
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faComments, faCheckCircle, faExclamationCircle, faChartLine, faUsers, faUserEdit } from '@fortawesome/free-solid-svg-icons';


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
        const res = await API.get(endpoints["current-user"]);

        if (res.data) {
          setStudent(res.data);
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

  // Di chuyển điều kiện vào đây để React Hooks được gọi theo đúng trình tự
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

  const userName = user?.name || user?.username || "Sinh viên";

  return (
    <div className="container mt-4">
      <h1 className="mb-4">Chào mừng, {user.name || user.username}!</h1>

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
              <i className="fas fa-graduation-cap me-2"></i>
              Điểm số
            </div>
            <div className="card-body">
              <Link to="/listscoreofstudent" className="btn btn-primary btn-lg d-block mb-3">
                <i className="fas fa-chart-line me-2"></i> Xem điểm của tôi
              </Link>
              <p>Xem bảng điểm của tất cả các môn học của bạn.</p>
            </div>
          </div>
        </div>

        <div className="col-md-6">
          <div className="card mb-4 shadow-sm">
            <div className="card-header bg-info text-white">
              <i className="fas fa-users me-2"></i>
              Lớp học
            </div>
            <div className="card-body">
              <Link to="/listoldclass" className="btn btn-info btn-lg d-block mb-3 text-white">
                <i className="fas fa-users me-2"></i> Thông tin lớp
              </Link>
              <p>Xem thông tin về lớp học và danh sách các bạn cùng lớp.</p>
            </div>
          </div>
        </div>
      </div>

      <div className="row">
        <div className="col-md-6">
          <div className="card mb-4 shadow-sm">
            <div className="card-header bg-success text-white">
              <i className="fas fa-user me-2"></i>
              Thông tin cá nhân
            </div>
            <div className="card-body">
              <h5>Thông tin sinh viên</h5>
              {student ? (
                <table className="table">
                  <tbody>
                    <tr>
                      <th style={{ width: "40%" }}>Mã sinh viên:</th>
                      <td>{student.id || "-"}</td>
                    </tr>
                    <tr>
                      <th>Họ và tên:</th>
                      <td>{student.name || "-"}</td>
                    </tr>
                    <tr>
                      <th>Email:</th>
                      <td>{student.email || "-"}</td>
                    </tr>
                    <tr>
                      <th>Vai trò:</th>
                      <td><span className="badge bg-primary">{student.role || "-"}</span></td>
                    </tr>
                  </tbody>
                </table>
              ) : (
                <p className="text-center">Không có thông tin</p>
              )}
              <div className="text-center">
                <Link to="/profile" className="btn btn-success">
                  <i className="fas fa-user-edit me-2"></i> Cập nhật thông tin
                </Link>
              </div>
            </div>
          </div>
        </div>

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
                Tham gia các diễn đàn học tập để thảo luận và đặt câu hỏi về các môn học.
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

export default StudentDashboard;