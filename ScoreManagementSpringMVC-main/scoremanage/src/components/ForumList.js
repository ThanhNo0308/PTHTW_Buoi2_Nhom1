import React, { useState, useEffect, useContext } from 'react';
import { Container, Card, Row, Col, Button, Alert, Spinner, Badge, Form } from 'react-bootstrap';
import { Link, useNavigate } from 'react-router-dom';
import { forumApis, teacherClassApis, studentApis } from '../configs/Apis';
import { MyUserContext } from '../App';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faComments, faPlus, faSearch, faExclamationTriangle, faCommentDots } from '@fortawesome/free-solid-svg-icons';

const ForumList = () => {
  const [user] = useContext(MyUserContext);
  const navigate = useNavigate();
  const [forums, setForums] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [subjectTeachers, setSubjectTeachers] = useState([]);
  const [selectedSubject, setSelectedSubject] = useState("");

  useEffect(() => {
    if (!user) {
      navigate("/login");
      return;
    }
  }, [user, navigate]);

  useEffect(() => {
    if (user) {
      loadForums();
      loadSubjectTeachers();
    }
  }, [user]);

  const loadForums = async () => {
    try {
      setLoading(true);
      setError("");

      let response;
      if (!user) {
        return;
      }

      console.log("Loading forums for user role:", user.role);

      if (user.role === 'Teacher') {
        response = await forumApis.getTeacherForums();
      } else if (user.role === 'Student') {
        response = await forumApis.getStudentForums();
        console.log("Student forums API response:", response.data);
      } else {
        response = await forumApis.getAllForums();
      }

      if (response.data.success) {
        console.log("Forums loaded:", response.data.forums.length);
        setForums(response.data.forums);
      } else {
        setError(response.data.error || "Không thể tải danh sách diễn đàn");
      }
    } catch (err) {
      console.error("Error loading forums:", err);
      console.error("Error details:", err.response?.data);
      setError("Lỗi khi tải danh sách diễn đàn: " + (err.response?.data?.error || err.message));
    } finally {
      setLoading(false);
    }
  };

  const loadSubjectTeachers = async () => {
    try {
      if (!user) return;

      // Tạo biến để lưu danh sách môn học/lớp
      let subjects = [];

      if (user.role === 'Teacher') {
        // Logic hiện tại cho giáo viên
        const response = await teacherClassApis.getTeacherSubjects(user.username);

        if (response.data && response.data.success) {
          subjects = response.data.subjectTeachers || [];
        }
      }
      else if (user.role === 'Student') {
        // Logic mới cho sinh viên
        // Lấy danh sách môn học đã đăng ký
        const response = await studentApis.getEnrolledSubjects();

        if (response.data) {
          // Chuyển đổi dữ liệu từ API thành cùng định dạng
          subjects = (response.data.subjects || []).map(subject => ({
            id: subject.subjectId,
            name: `${subject.subjectName} - ${subject.teacherName || 'Chưa có giảng viên'}`
          }));
        }
      }

      setSubjectTeachers(subjects.map(st => {
        // Nếu đã ở định dạng chuẩn
        if (st.id && st.name) return st;

        // Nếu là dữ liệu raw từ API
        return {
          id: st.id || st.subjectId,
          name: st.name || `${st.subjectId?.subjectName || 'Không có tên'} - ${st.classId?.className || 'Chưa phân lớp'}`
        };
      }));

      console.log("Loaded subject teachers:", subjects);
    } catch (err) {
      console.error("Error loading subject teachers:", err);
    }
  };

  const handleSubjectChange = async (e) => {
    const subjectTeacherId = e.target.value;
    setSelectedSubject(subjectTeacherId);

    if (!subjectTeacherId) {
      loadForums(); // Load all forums
      return;
    }

    try {
      setLoading(true);
      setError("");

      const response = await forumApis.getForumsBySubjectTeacher(subjectTeacherId);

      if (response.data.success) {
        setForums(response.data.forums);
      } else {
        setError(response.data.error || "Không thể tải danh sách diễn đàn");
      }
    } catch (err) {
      console.error("Error loading forums by subject:", err);
      setError("Lỗi khi tải danh sách diễn đàn: " + (err.response?.data?.error || err.message));
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString) => {
    const options = {
      year: 'numeric',
      month: 'numeric',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    };
    return new Date(dateString).toLocaleDateString('vi-VN', options);
  };

  if (loading) {
    return (
      <Container className="text-center my-5">
        <Spinner animation="border" variant="primary" />
        <p className="mt-3">Đang tải danh sách diễn đàn...</p>
      </Container>
    );
  }

  return (
    <Container className="my-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h2>
          <FontAwesomeIcon icon={faComments} className="me-2" />
          Diễn đàn trao đổi học tập
        </h2>
        {user && user.role !== 'Student' && (  // Thêm kiểm tra user ở đây
          <Button variant="success" as={Link} to="/forums/create">
            <FontAwesomeIcon icon={faPlus} className="me-2" />
            Tạo diễn đàn mới
          </Button>
        )}
      </div>

      {error && (
        <Alert variant="danger" dismissible onClose={() => setError("")}>
          <FontAwesomeIcon icon={faExclamationTriangle} className="me-2" />
          {error}
        </Alert>
      )}

      <Card className="mb-4">
        <Card.Header>
          <div className="d-flex justify-content-between align-items-center">
            <h5 className="mb-0">Lọc diễn đàn</h5>
            <Button variant="outline-primary" onClick={loadForums}>Làm mới</Button>
          </div>
        </Card.Header>
        <Card.Body>
          <Form>
            <Form.Group>
              <Form.Label>Chọn môn học</Form.Label>
              <Form.Select
                value={selectedSubject}
                onChange={handleSubjectChange}
              >
                <option value="">Tất cả diễn đàn</option>
                {subjectTeachers.map((subject) => (
                  <option key={subject.id} value={subject.id}>
                    {subject.name}
                  </option>
                ))}
              </Form.Select>
            </Form.Group>
          </Form>
        </Card.Body>
      </Card>

      {forums.length === 0 ? (
        <Alert variant="info">
          <FontAwesomeIcon icon={faCommentDots} className="me-2" />
          Không có diễn đàn nào hiện tại. Hãy tạo diễn đàn mới!
        </Alert>
      ) : (
        <Row xs={1} md={2} className="g-4">
          {forums.map((forum) => (
            <Col key={forum.id}>
              <Card className="h-100 shadow-sm">
                <Card.Header className="bg-light">
                  <div className="d-flex justify-content-between align-items-center">
                    <span className="text-muted">
                      {forum.subjectTeacherId?.subjectId?.subjectName || "Chưa có môn học"} -
                      {forum.subjectTeacherId?.teacherId?.teacherName || "Chưa có giảng viên"}
                    </span>
                    <Badge bg="secondary">
                      {formatDate(forum.createdAt)}
                    </Badge>
                  </div>
                </Card.Header>
                <Card.Body>
                  <div className="mb-3 text-muted">
                    <div className="row">
                      <div className="col-md-6">
                        <p className="mb-1"><strong>Khoa:</strong> {forum.subjectTeacherId?.classId?.majorId?.departmentId?.departmentName || "Không có thông tin"}</p>
                        <p className="mb-1"><strong>Hệ đào tạo:</strong> {forum.subjectTeacherId?.classId?.majorId?.trainingTypeId?.trainingTypeName || "Không có thông tin"}</p>
                        <p className="mb-1"><strong>Lớp:</strong> {forum.subjectTeacherId?.classId?.className || "Không có thông tin"}</p>
                        <p className="mb-1"><strong>Người tạo:</strong> {forum.userId?.name || forum.userId?.username || "Không xác định"}</p>
                      </div>
                    </div>
                  </div>
                  <Card.Subtitle className="mb-3 text-muted">{forum.description}</Card.Subtitle>
                  <Card.Text>{forum.content}</Card.Text>
                </Card.Body>
                <Card.Footer className="text-center">
                  <Button
                    variant="primary"
                    as={Link}
                    to={`/forums/${forum.id}`}
                    className="w-100"
                  >
                    <FontAwesomeIcon icon={faCommentDots} className="me-2" />
                    Xem thảo luận
                  </Button>
                </Card.Footer>
              </Card>
            </Col>
          ))}
        </Row>
      )}
    </Container>
  );
};

export default ForumList;