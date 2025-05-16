import React, { useState, useEffect, useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { MyUserContext } from "../App";
import { endpoints, API } from '../configs/Apis';
import { Alert, Spinner, Card, Button, Modal, Form } from 'react-bootstrap';
import "../assets/css/base.css";
import "../assets/css/dashboard.css";
import defaultAvatar from '../assets/images/logo.png';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {
  faComments, faExclamationCircle, faCheckCircle, faUserEdit, faUsers, faGraduationCap, faFileImport, faSearch, faUser,
  faChalkboard, faPaperPlane, faUnlock, faEnvelope, faCalendarAlt
} from '@fortawesome/free-solid-svg-icons';

const TeacherDashboard = () => {
  const [user] = useContext(MyUserContext);
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [teacherInfo, setTeacherInfo] = useState(null);
  const [showUnlockModal, setShowUnlockModal] = useState(false);
  const [adminUsers, setAdminUsers] = useState([]);
  const [loadingAdmins, setLoadingAdmins] = useState(false);
  const [sendingRequest, setSendingRequest] = useState(false);

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

  useEffect(() => {
    if (teacherInfo?.roleSpecificInfo?.departmentId?.departmentName) {
      setUnlockRequest(prev => ({
        ...prev,
        department: teacherInfo.roleSpecificInfo.departmentId.departmentName
      }));
    }
  }, [teacherInfo]);

  const [unlockRequest, setUnlockRequest] = useState({
    adminEmail: '',
    schoolYearInfo: '',
    department: teacherInfo?.roleSpecificInfo?.departmentId?.departmentName || '',
    major: '',
    classOrStudent: '',
    subject: '',
    reason: ''
  });

  useEffect(() => {
    if (showUnlockModal) {
      loadAdminUsers();
    }
  }, [showUnlockModal]);

  // Hàm để load danh sách admin users
  const loadAdminUsers = async () => {
    try {
      setLoadingAdmins(true);
      const response = await API.get(endpoints["admin-users"]);

      if (response.data && response.data.success) {
        setAdminUsers(response.data.adminUsers || []);
      } else {
        setError("Không thể tải danh sách quản trị viên");
      }
    } catch (err) {
      console.error("Lỗi khi tải danh sách admin:", err);
      setError("Không thể tải danh sách quản trị viên: " + err.message);
    } finally {
      setLoadingAdmins(false);
    }
  };

  // Hàm xử lý thay đổi form
  const handleUnlockRequestChange = (e) => {
    const { name, value } = e.target;
    setUnlockRequest(prev => ({
      ...prev,
      [name]: value
    }));
  };

  // Hàm xử lý gửi yêu cầu
  const handleSendUnlockRequest = async (e) => {
    e.preventDefault();

    // Validate form
    if (!unlockRequest.adminEmail) {
      setError("Vui lòng chọn quản trị viên nhận yêu cầu");
      return;
    }

    if (!unlockRequest.schoolYearInfo) {
      setError("Vui lòng nhập thông tin năm học/học kỳ");
      return;
    }

    if (!unlockRequest.subject) {
      setError("Vui lòng nhập tên môn học cần mở khóa");
      return;
    }

    if (!unlockRequest.classOrStudent) {
      setError("Vui lòng nhập thông tin lớp hoặc sinh viên cần mở khóa");
      return;
    }

    if (!unlockRequest.reason) {
      setError("Vui lòng nhập lý do yêu cầu mở khóa");
      return;
    }

    try {
      setSendingRequest(true);

      const response = await API.post(endpoints["send-unlock-request"], {
        ...unlockRequest,
        teacherId: parseInt(teacherInfo?.roleSpecificInfo?.id) || 0
      });

      if (response.data && response.data.success) {
        setSuccessMessage("Yêu cầu mở khóa điểm đã được gửi thành công!");
        setShowUnlockModal(false);
        setUnlockRequest({
          adminEmail: '',
          schoolYearInfo: '',
          department: teacherInfo?.roleSpecificInfo?.departmentId?.departmentName || '',
          major: '',
          classOrStudent: '',
          subject: '',
          reason: ''
        });
      } else {
        setError(response.data?.message || "Không thể gửi yêu cầu mở khóa điểm");
      }
    } catch (err) {
      console.error("Lỗi khi gửi yêu cầu mở khóa:", err);
      setError("Lỗi khi gửi yêu cầu: " + err.message);
    } finally {
      setSendingRequest(false);
    }
  };



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
              <div className="col-md-6 mb-3">
                <div className="feature-card">
                  <div className="card-header-custom bg-soft-warning">
                    <FontAwesomeIcon icon={faUnlock} className="card-header-icon" />
                    Yêu cầu mở khóa điểm
                  </div>
                  <Card.Body>
                    <Card.Text>
                      Gửi yêu cầu mở khóa điểm đến quản trị viên khi cần chỉnh sửa điểm đã khóa.
                    </Card.Text>
                    <Button
                      variant="warning"
                      className="feature-btn w-100"
                      onClick={() => setShowUnlockModal(true)}
                    >
                      <FontAwesomeIcon icon={faEnvelope} className="me-2" />
                      Gửi yêu cầu mở khóa
                    </Button>
                  </Card.Body>
                </div>
              </div>
              <div className="col-md-6 mb-3">
                <div className="feature-card">
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

              <div className="col-md-6 mb-3">
                <div className="feature-card">
                  <div className="card-header-custom bg-soft-info">
                    <FontAwesomeIcon icon={faCalendarAlt} className="card-header-icon" />
                    Lịch Dạy
                  </div>
                  <Card.Body>
                    <Card.Text>
                      Xem lịch dạy của bạn theo tuần, tháng. Theo dõi thời gian và phòng học của các môn học được phân công.
                    </Card.Text>
                    <Link to="/teacher/schedule" className="btn btn-info feature-btn w-100 text-white">
                      <FontAwesomeIcon icon={faCalendarAlt} className="me-2" />
                      Xem lịch dạy
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

      <Modal show={showUnlockModal} onHide={() => setShowUnlockModal(false)} size="lg">
        <Modal.Header closeButton className="bg-warning text-white">
          <Modal.Title><FontAwesomeIcon icon={faUnlock} className="me-2" /> Yêu cầu mở khóa điểm</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <p>Vui lòng điền đầy đủ thông tin để gửi yêu cầu mở khóa điểm đến quản trị viên.</p>

          {error && (
            <Alert variant="danger" dismissible onClose={() => setError("")}>
              <FontAwesomeIcon icon={faExclamationCircle} className="me-2" />
              {error}
            </Alert>
          )}

          <Form onSubmit={handleSendUnlockRequest}>
            <Form.Group className="mb-3">
              <Form.Label>Gửi đến quản trị viên: <span className="text-danger">*</span></Form.Label>
              <Form.Select
                name="adminEmail"
                value={unlockRequest.adminEmail}
                onChange={handleUnlockRequestChange}
                required
                disabled={loadingAdmins}
              >
                <option value="">-- Chọn quản trị viên --</option>
                {loadingAdmins ? (
                  <option value="" disabled>Đang tải danh sách...</option>
                ) : (
                  adminUsers.map((admin, index) => (
                    <option key={index} value={admin.email}>
                      {admin.username} ({admin.email})
                    </option>
                  ))
                )}
              </Form.Select>
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Năm học/Học kỳ: <span className="text-danger">*</span></Form.Label>
              <Form.Control
                type="text"
                name="schoolYearInfo"
                value={unlockRequest.schoolYearInfo}
                onChange={handleUnlockRequestChange}
                placeholder="Ví dụ: 2024-2025 Học kỳ 1"
                required
              />
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Khoa:</Form.Label>
              <Form.Control
                type="text"
                name="department"
                value={unlockRequest.department}
                onChange={handleUnlockRequestChange}
                placeholder="Nhập tên khoa"
                readOnly={!!teacherInfo?.roleSpecificInfo?.departmentId?.departmentName}
              />
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Ngành:</Form.Label>
              <Form.Control
                type="text"
                name="major"
                value={unlockRequest.major}
                onChange={handleUnlockRequestChange}
                placeholder="Nhập tên ngành"
              />
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Môn học cần mở khóa: <span className="text-danger">*</span></Form.Label>
              <Form.Control
                type="text"
                name="subject"
                value={unlockRequest.subject}
                onChange={handleUnlockRequestChange}
                placeholder="Ví dụ: Lập trình Java, Cơ sở dữ liệu,..."
                required
              />
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Lớp/Sinh viên cần mở khóa: <span className="text-danger">*</span></Form.Label>
              <Form.Control
                type="text"
                name="classOrStudent"
                value={unlockRequest.classOrStudent}
                onChange={handleUnlockRequestChange}
                placeholder="Ví dụ: DHKTPM17A hoặc Nguyễn Văn A - 2051050123"
                required
              />
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Lý do yêu cầu mở khóa: <span className="text-danger">*</span></Form.Label>
              <Form.Control
                as="textarea"
                rows={3}
                name="reason"
                value={unlockRequest.reason}
                onChange={handleUnlockRequestChange}
                placeholder="Vui lòng nêu rõ lý do cần mở khóa điểm"
                required
              />
            </Form.Group>
          </Form>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShowUnlockModal(false)}>
            Hủy
          </Button>
          <Button
            variant="warning"
            onClick={handleSendUnlockRequest}
            disabled={sendingRequest}
          >
            {sendingRequest ? (
              <>
                <Spinner animation="border" size="sm" className="me-2" />
                Đang gửi...
              </>
            ) : (
              <>
                <FontAwesomeIcon icon={faEnvelope} className="me-2" />
                Gửi yêu cầu
              </>
            )}
          </Button>
        </Modal.Footer>
      </Modal>
    </div>
  );
};

export default TeacherDashboard;