import React, { useState, useEffect, useContext } from 'react';
import { Link, Navigate } from 'react-router-dom';
import { MyUserContext } from "../App";
import axios from 'axios';
import { endpoints, API } from '../configs/Apis';
import { Alert, Spinner, Col, Card, Button, Row } from 'react-bootstrap';
import "../assets/css/base.css";
import "../assets/css/styles.css";
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {
  faComments, faCheckCircle, faExclamationCircle,
  faChartLine, faUsers, faUserEdit, faBook, faGraduationCap
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
    <div className="container mt-4">
      <h1 className="mb-4">Chào mừng, {user.name || user.username}!</h1>

      {successMessage && (
        <Alert variant="success" dismissible onClose={() => setSuccessMessage("")}>
          <FontAwesomeIcon icon={faCheckCircle} className="me-2" /> {successMessage}
        </Alert>
      )}

      {error && (
        <Alert variant="danger" dismissible onClose={() => setError("")}>
          <FontAwesomeIcon icon={faExclamationCircle} className="me-2" /> {error}
        </Alert>
      )}

      <Row className="mt-4">
        <Col md={6} lg={4} className="mb-4">
          <Card className="h-100 shadow-sm">
            <Card.Header className="bg-primary text-white">
              <FontAwesomeIcon icon={faChartLine} className="me-2" />
              Điểm số
            </Card.Header>
            <Card.Body className="d-flex flex-column">
              <p>Xem bảng điểm của tất cả các môn học và theo dõi kết quả học tập.</p>
              <div className="mt-auto">
                <Link to="/student/scores" className="btn btn-primary w-100">
                  <FontAwesomeIcon icon={faChartLine} className="me-2" /> Xem điểm của tôi
                </Link>
              </div>
            </Card.Body>
          </Card>
        </Col>

        <Col md={6} lg={4} className="mb-4">
          <Card className="h-100 shadow-sm">
            <Card.Header className="bg-info text-white">
              <FontAwesomeIcon icon={faUsers} className="me-2" />
              Lớp học
            </Card.Header>
            <Card.Body className="d-flex flex-column">
              <p>Xem thông tin về lớp học và danh sách các bạn cùng lớp.</p>
              <div className="mt-auto">
                <Link to="/student/class-info" className="btn btn-info w-100 text-white">
                  <FontAwesomeIcon icon={faUsers} className="me-2" /> Thông tin lớp
                </Link>
              </div>
            </Card.Body>
          </Card>
        </Col>

        <Col md={6} lg={4} className="mb-4">
          <Card className="h-100 shadow-sm">
            <Card.Header className="bg-success text-white">
              <FontAwesomeIcon icon={faBook} className="me-2" />
              Môn học
            </Card.Header>
            <Card.Body className="d-flex flex-column">
              <p>Xem danh sách môn học bạn đã đăng ký trong học kỳ hiện tại và trước đây.</p>
              <div className="mt-auto">
                <Link to="/student/subjects" className="btn btn-success w-100">
                  <FontAwesomeIcon icon={faBook} className="me-2" /> Môn học đã đăng ký
                </Link>
              </div>
            </Card.Body>
          </Card>
        </Col>
      </Row>

      <Row className="mt-2">
        <Col md={6} className="mb-4">
          <Card className="h-100 shadow-sm">
            <Card.Header className="bg-warning text-dark">
              <FontAwesomeIcon icon={faUserEdit} className="me-2" />
              Thông tin cá nhân
            </Card.Header>
            <Card.Body>
              {student ? (
                <>
                  <div className="text-center mb-3">
                    <img
                      src={student?.image || user?.image || "/images/default-avatar.jpg"}
                      alt="Avatar"
                      className="rounded-circle"
                      style={{ width: "100px", height: "100px", objectFit: "cover" }}
                    />
                    <h5 className="mt-2">{student?.lastName || user?.name} {student?.firstName} </h5>
                    <span className="badge bg-primary mb-3">{user?.role}</span>
                  </div>
                  <table className="table">
                    <tbody>
                      <tr>
                        <th style={{ width: "40%" }}>MSSV:</th>
                        <td>{student?.studentCode || "-"}</td>
                      </tr>
                      <tr>
                        <th>Email:</th>
                        <td>{student?.email || user?.email || "-"}</td>
                      </tr>
                      <tr>
                        <th>Giới tính:</th>
                        <td>{student?.gender === 0 || user?.gender === 0 ? "Nam" : "Nữ"}</td>
                      </tr>
                      <tr>
                        <th>Lớp:</th>
                        <td>{student?.classId?.className || "-"}</td>
                      </tr>
                    </tbody>
                  </table>
                  <div className="text-center mt-3">
                    <Link to="/profile" className="btn btn-warning">
                      <FontAwesomeIcon icon={faUserEdit} className="me-2" /> Cập nhật thông tin
                    </Link>
                  </div>
                </>
              ) : (
                <div className="text-center">
                  <Spinner animation="border" size="sm" className="me-2" />
                  Đang tải thông tin...
                </div>
              )}
            </Card.Body>
          </Card>
        </Col>

        <Col md={6} className="mb-4">
          <Card className="h-100 shadow-sm">
            <Card.Header className="bg-info text-white">
              <FontAwesomeIcon icon={faComments} className="me-2" />
              Diễn đàn học tập
            </Card.Header>
            <Card.Body className="d-flex flex-column">
              <p>Tham gia các diễn đàn học tập để xem thảo luận và đặt câu hỏi về các môn học. Trao đổi kiến thức và học hỏi từ giảng viên và bạn học.</p>
              <div className="mt-auto">
                <Link to="/forums" className="btn btn-info w-100 text-white">
                  <FontAwesomeIcon icon={faComments} className="me-2" />
                  Truy cập diễn đàn
                </Link>
              </div>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default StudentDashboard;