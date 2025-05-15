import React, { useState, useEffect, useContext } from 'react';
import { Container, Card, Row, Col, Button, Alert, Spinner, Badge, Form } from 'react-bootstrap';
import { Link, useNavigate } from 'react-router-dom';
import { forumApis, teacherClassApis, studentApis } from '../configs/Apis';
import { MyUserContext } from '../App';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faComments, faPlus, faSearch, faExclamationTriangle, faCommentDots, faEdit, faTrash, faBuilding, faGraduationCap, faSchool, faUser } from '@fortawesome/free-solid-svg-icons';
import "../assets/css/forum.css";

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
      // Chỉ tải diễn đàn sau khi đã lấy được danh sách môn học
      const loadData = async () => {
        await loadSubjectTeachers();
        loadForums();
      };

      loadData();
    }
  }, [user]);

  const loadForums = async () => {
    try {
      setLoading(true);
      setError("");

      if (!user) return;

      let response;
      if (user.role === 'Teacher') {
        response = await forumApis.getTeacherForums();
      } else {
        // Sinh viên xem tất cả diễn đàn
        response = await forumApis.getAllForums();
      }

      if (response.data && response.data.success) {
        let forumsToShow = response.data.forums || [];

        // Lọc diễn đàn cho sinh viên dựa trên môn học đã đăng ký
        if (user.role === 'Student') {
          if (subjectTeachers.length === 0) {
            // Nếu chưa có dữ liệu môn học, tải lại
            const subjects = await loadSubjectTeachers();
            const subjectIds = subjects.map(subject => subject.id);

            forumsToShow = forumsToShow.filter(forum => {
              const forumSubjectId = forum.subjectTeacherId?.id;
              return subjectIds.includes(forumSubjectId);
            });
          } else {
            // Sử dụng danh sách môn học đã có
            const subjectIds = subjectTeachers.map(subject => subject.id);

            forumsToShow = forumsToShow.filter(forum => {
              const forumSubjectId = forum.subjectTeacherId?.id;
              return subjectIds.includes(forumSubjectId);
            });
          }

        }

        setForums(forumsToShow);
      } else {
        setError(response.data?.error || "Không thể tải danh sách diễn đàn");
      }
    } catch (err) {
      console.error("Error loading forums:", err);
      setError("Lỗi khi tải danh sách diễn đàn: " + (err.response?.data?.error || err.message));
    } finally {
      setLoading(false);
    }
  };

  const loadSubjectTeachers = async () => {
    try {
      if (!user) return [];

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

      const formattedSubjects = subjects.map(st => {
        // Nếu đã ở định dạng chuẩn
        if (st.id && st.name) return st;

        // Nếu là dữ liệu raw từ API
        return {
          id: st.id || st.subjectId,
          name: st.name || `${st.subjectId?.subjectName || 'Không có tên'} - ${st.classId?.className || 'Chưa phân lớp'}`
        };
      });

      setSubjectTeachers(formattedSubjects);

      return formattedSubjects;
    } catch (err) {
      console.error("Error loading subject teachers:", err);
      return [];
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

  const deleteForum = async (forumId) => {
    if (!window.confirm('Bạn có chắc chắn muốn xóa diễn đàn này?')) {
      return;
    }

    try {
      setLoading(true);
      const response = await forumApis.deleteForum({ forumId });

      if (response.data.success) {
        // Xóa diễn đàn khỏi state forums để cập nhật UI ngay lập tức
        setForums(forums.filter(f => f.id !== forumId));
        alert('Xóa diễn đàn thành công!');
      } else {
        setError(response.data.error || 'Không thể xóa diễn đàn');
      }
    } catch (err) {
      console.error("Error deleting forum:", err);
      setError("Lỗi khi xóa diễn đàn: " + (err.response?.data?.error || err.message));
    } finally {
      setLoading(false);
    }
  };

  const canEditForum = (forum) => {
    if (!user) return false;

    // Admin có thể chỉnh sửa mọi diễn đàn
    if (user.role === 'Admin') return true;

    // Giảng viên có thể chỉnh sửa diễn đàn của mình hoặc diễn đàn của môn học mình dạy
    if (user.role === 'Teacher') {
      // Người tạo diễn đàn
      if (forum.userId && user.id === forum.userId.id) return true;

      // Nếu là giảng viên phụ trách môn học của diễn đàn
      if (forum.subjectTeacherId &&
        forum.subjectTeacherId.teacherId &&
        forum.subjectTeacherId.teacherId.email === user.email) {
        return true;
      }
    }

    return false;
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
          {user.role === 'Student' ? (
            <>Không có diễn đàn nào cho các môn học bạn đã đăng ký.</>
          ) : (
            <>Không có diễn đàn nào hiện tại. Hãy tạo diễn đàn mới!</>
          )}
        </Alert>
      ) : (
        <Row xs={1} md={2} className="g-4">
          {forums.map((forum) => (
            <Col key={forum.id}>
              <Card className="h-100 shadow-sm forum-card">
                <Card.Header className="bg-light">
                  <div className="d-flex justify-content-between align-items-center">
                    <span className="text-muted">
                      {forum.subjectTeacherId?.subjectId?.subjectName || "Chưa có môn học"} -
                      {forum.subjectTeacherId?.teacherId?.teacherName || "Chưa có giảng viên"}
                    </span>
                    <Badge bg="secondary" className="forum-badge">
                      {formatDate(forum.createdAt)}
                    </Badge>
                  </div>
                </Card.Header>
                <Card.Body>
                  <h5 className="forum-title">{forum.title}</h5>
                  <div className="forum-info">
                    <div className="forum-info-item">
                      <FontAwesomeIcon icon={faBuilding} />
                      {forum.subjectTeacherId?.classId?.majorId?.departmentId?.departmentName || "Không có thông tin"}
                    </div>
                    <div className="forum-info-item">
                      <FontAwesomeIcon icon={faGraduationCap} />
                      {forum.subjectTeacherId?.classId?.majorId?.trainingTypeId?.trainingTypeName || "Không có thông tin"}
                    </div>
                    <div className="forum-info-item">
                      <FontAwesomeIcon icon={faSchool} />
                      {forum.subjectTeacherId?.classId?.className || "Không có thông tin"}
                    </div>
                    <div className="forum-info-item">
                      <FontAwesomeIcon icon={faUser} />
                      {forum.userId?.name || forum.userId?.username || "Không xác định"}
                    </div>
                  </div>
                  <div className="forum-description">{forum.description}</div>
                  <div className="forum-content">{forum.content}</div>
                </Card.Body>
                <Card.Footer>
                  <div className="d-flex justify-content-between align-items-center">
                    <Button
                      variant="primary"
                      as={Link}
                      to={`/forums/${forum.id}`}
                    >
                      <FontAwesomeIcon icon={faCommentDots} className="me-2" />
                      Xem thảo luận
                    </Button>

                    {/* Hiển thị các nút sửa/xóa nếu user có quyền */}
                    {canEditForum(forum) && (
                      <div>
                        <Button
                          variant="warning"
                          as={Link}
                          to={`/forums/edit/${forum.id}`}
                          className="me-2"
                          size="sm"
                        >
                          <FontAwesomeIcon icon={faEdit} className="me-1" />
                          Sửa
                        </Button>
                        <Button
                          variant="danger"
                          size="sm"
                          onClick={() => deleteForum(forum.id)}
                        >
                          <FontAwesomeIcon icon={faTrash} className="me-1" />
                          Xóa
                        </Button>
                      </div>
                    )}
                  </div>
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