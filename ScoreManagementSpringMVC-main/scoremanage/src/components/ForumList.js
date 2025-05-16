import React, { useState, useEffect, useContext } from 'react';
import { Container, Card, Row, Col, Button, Alert, Spinner, Badge, Form } from 'react-bootstrap';
import { Link, useNavigate } from 'react-router-dom';
import { forumApis, teacherClassApis, studentApis } from '../configs/Apis';
import { MyUserContext } from '../App';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {
  faComments, faPlus, faSearch, faExclamationTriangle, faCommentDots,
  faEdit, faTrash, faUser, faUniversity, faGraduationCap,
  faSchool, faCalendarAlt, faSync
} from '@fortawesome/free-solid-svg-icons';
import "../assets/css/forum.css";

const ForumList = () => {
  const [user] = useContext(MyUserContext);
  const navigate = useNavigate();
  const [forums, setForums] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [subjectTeachers, setSubjectTeachers] = useState([]);
  const [selectedSubject, setSelectedSubject] = useState("");
  const [schoolYears, setSchoolYears] = useState([]);
  const [selectedSchoolYear, setSelectedSchoolYear] = useState("");

  useEffect(() => {
    if (!user) {
      navigate("/login");
      return;
    }
  }, [user, navigate]);

  useEffect(() => {
    if (user) {
      const loadData = async () => {
        // Load school years first
        await loadSchoolYears();
        await loadSubjectTeachers();
        loadForums();
      };

      loadData();
    }
  }, [user]);

  const loadSchoolYears = async () => {
    try {
      const response = await forumApis.getAvailableSchoolYears();
      if (response.data && response.data.success) {
        setSchoolYears(response.data.schoolYears || []);

        // Nếu có học kỳ hiện tại, chọn mặc định
        const currentSchoolYear = response.data.schoolYears.find(sy => sy.isCurrent);
        if (currentSchoolYear) {
          setSelectedSchoolYear(currentSchoolYear.id);
        } else if (response.data.schoolYears.length > 0) {
          setSelectedSchoolYear(response.data.schoolYears[0].id);
        }
      }
    } catch (err) {
      console.error("Error loading school years:", err);
    }
  };

  const loadForums = async (subjectId = selectedSubject, schoolYearId = selectedSchoolYear) => {
    try {
      setLoading(true);
      setError("");

      if (!user) return;

      // Truyền các tham số vào API call
      let response = await forumApis.getFilteredForums(subjectId, schoolYearId);

      if (response.data && response.data.success) {
        setForums(response.data.forums || []);
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

  const loadSubjectTeachers = async (schoolYearId = selectedSchoolYear) => {
    try {
      if (!user) return [];

      // Tạo biến để lưu danh sách môn học/lớp
      let subjects = [];

      if (user.role === 'Teacher') {
        // Cập nhật: Truyền schoolYearId vào API call
        const response = await teacherClassApis.getTeacherSubjects(user.username, schoolYearId);

        if (response.data && response.data.success) {
          subjects = response.data.subjectTeachers || [];
        }
      }
      else if (user.role === 'Student') {
        // Đã hỗ trợ lọc theo học kỳ
        const response = await studentApis.getEnrolledSubjects(schoolYearId);

        if (response.data) {
          subjects = (response.data.subjects || []).map(subject => ({
            id: subject.subjectId,
            name: `${subject.subjectName} - ${subject.teacherName || 'Chưa có giảng viên'}`,
            schoolYear: subject.schoolYear || null
          }));
        }
      }

      const formattedSubjects = subjects.map(st => {
        // Nếu đã ở định dạng chuẩn
        if (st.id && st.name) return st;

        // Nếu là dữ liệu raw từ API
        return {
          id: st.id || st.subjectId,
          name: st.name || `${st.subjectId?.subjectName || 'Không có tên'} - ${st.classId?.className || 'Chưa phân lớp'}`,
          schoolYear: st.schoolYear || st.schoolYearId
        };
      });

      setSubjectTeachers(formattedSubjects);
      return formattedSubjects;
    } catch (err) {
      console.error("Error loading subject teachers:", err);
      return [];
    }
  };

  const handleSchoolYearChange = async (e) => {
    const schoolYearId = e.target.value;
    setSelectedSchoolYear(schoolYearId);

    // Reset selected subject
    setSelectedSubject("");

    // Reload subjects based on new school year - truyền schoolYearId mới vào
    await loadSubjectTeachers(schoolYearId);

    // Then load forums based on new filters
    loadForums(null, schoolYearId);
  };

  const handleSubjectChange = async (e) => {
    const subjectId = e.target.value;
    setSelectedSubject(subjectId);
    loadForums(subjectId, selectedSchoolYear);
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
        {user && user.role !== 'Student' && (
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

      <Card className="mb-4 filter-card">
        <Card.Header>
          <div className="d-flex justify-content-between align-items-center">
            <h5 className="mb-0">Lọc diễn đàn</h5>
            <Button
              variant="outline-primary"
              onClick={() => {
                // Reset các bộ lọc và tải lại tất cả dữ liệu
                setSelectedSchoolYear("");
                setSelectedSubject("");
                loadSubjectTeachers(null); // Tải lại tất cả môn học
                loadForums(null, null); // Tải lại tất cả diễn đàn
              }}
            >
              <FontAwesomeIcon icon={faSync} className="me-2" />
              Xóa bộ lọc
            </Button>
          </div>
        </Card.Header>
        <Card.Body>
          <Form>
            <Row className="g-3">
              {/* Dropdown học kỳ */}
              <Col md={6}>
                <Form.Group>
                  <Form.Label>
                    <FontAwesomeIcon icon={faCalendarAlt} className="me-2" />
                    Năm học - Học kỳ
                  </Form.Label>
                  <Form.Select
                    value={selectedSchoolYear}
                    onChange={handleSchoolYearChange}
                    className="mb-3"
                  >
                    <option value="">Tất cả học kỳ</option>
                    {schoolYears.map((sy) => (
                      <option key={sy.id} value={sy.id}>
                        {sy.nameYear} {sy.semesterName} {sy.isCurrent ? "(Hiện tại)" : ""}
                      </option>
                    ))}
                  </Form.Select>
                </Form.Group>
              </Col>

              {/* Dropdown môn học */}
              <Col md={6}>
                <Form.Group>
                  <Form.Label>
                    <FontAwesomeIcon icon={faSearch} className="me-2" />
                    Chọn môn học
                  </Form.Label>
                  <Form.Select
                    value={selectedSubject}
                    onChange={handleSubjectChange}
                  >
                    <option value="">Tất cả môn học</option>
                    {subjectTeachers.map((subject) => (
                      <option key={subject.id} value={subject.id}>
                        {subject.name}
                      </option>
                    ))}
                  </Form.Select>
                </Form.Group>
              </Col>
            </Row>
          </Form>
        </Card.Body>
      </Card>

      {forums.length === 0 ? (
        <Alert variant="info" className="fade-in">
          <FontAwesomeIcon icon={faCommentDots} className="me-2" />
          {selectedSchoolYear ? (
            <>Không có diễn đàn nào trong học kỳ đã chọn.</>
          ) : (
            <>Không có diễn đàn nào phù hợp với điều kiện tìm kiếm.</>
          )}
        </Alert>
      ) : (
        <Row xs={1} md={2} className="g-4">
          {forums.map((forum) => (
            <Col key={forum.id} className="fade-in">
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
                      <FontAwesomeIcon icon={faUniversity} className="me-1" />
                      {forum.subjectTeacherId?.classId?.majorId?.departmentId?.departmentName || "Không có thông tin"}
                    </div>
                    <div className="forum-info-item">
                      <FontAwesomeIcon icon={faGraduationCap} className="me-1" />
                      {forum.subjectTeacherId?.classId?.majorId?.trainingTypeId?.trainingTypeName || "Không có thông tin"}
                    </div>
                    <div className="forum-info-item">
                      <FontAwesomeIcon icon={faSchool} className="me-1" />
                      {forum.subjectTeacherId?.classId?.className || "Không có thông tin"}
                    </div>
                    <div className="forum-info-item">
                      <FontAwesomeIcon icon={faUser} className="me-1" />
                      {forum.userId?.name || forum.userId?.username || "Không xác định"}
                    </div>
                    {/* Hiển thị học kỳ */}
                    {forum.subjectTeacherId?.schoolYearId && (
                      <div className="forum-info-item">
                        <FontAwesomeIcon icon={faCalendarAlt} className="me-1" />
                        {forum.subjectTeacherId.schoolYearId.nameYear} - {forum.subjectTeacherId.schoolYearId.semesterName}
                      </div>
                    )}
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