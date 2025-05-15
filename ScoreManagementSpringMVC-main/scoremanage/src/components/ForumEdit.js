import React, { useState, useEffect, useContext } from 'react';
import { Container, Card, Form, Button, Alert, Spinner } from 'react-bootstrap';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { forumApis, teacherClassApis } from '../configs/Apis';
import { MyUserContext } from '../App';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faComments, faArrowLeft, faSave, faExclamationTriangle } from '@fortawesome/free-solid-svg-icons';
import "../assets/css/forum.css";

const ForumEdit = () => {
  const { forumId } = useParams();
  const [user] = useContext(MyUserContext);
  const navigate = useNavigate();

  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [content, setContent] = useState('');
  const [subjectTeacherId, setSubjectTeacherId] = useState('');

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [subjectTeachers, setSubjectTeachers] = useState([]);

  useEffect(() => {
    const loadForumAndSubjects = async () => {
      try {
        setLoading(true);

        // 1. Load forum details
        const forumResponse = await forumApis.getForumDetail(forumId);

        if (!forumResponse.data.success) {
          setError("Không thể tải thông tin diễn đàn");
          return;
        }

        const forum = forumResponse.data.forum;

        // Check if user has permission to edit this forum
        if (user.id !== forum.userId?.id && user.role !== 'Admin' && user.role !== 'Teacher') {
          setError("Bạn không có quyền chỉnh sửa diễn đàn này");
          return;
        }

        // Set form values
        setTitle(forum.title || '');
        setDescription(forum.description || '');
        setContent(forum.content || '');

        if (forum.subjectTeacherId) {
          setSubjectTeacherId(forum.subjectTeacherId.id);
        }

        // 2. Load subject teachers
        let subjectsResponse;
        if (user.role === 'Teacher') {
          subjectsResponse = await teacherClassApis.getTeacherSubjects(user.username);

          if (subjectsResponse.data && subjectsResponse.data.success) {
            const subjects = subjectsResponse.data.subjectTeachers || [];

            setSubjectTeachers(subjects.map(st => ({
              id: st.id,
              name: `${st.subjectId?.subjectName || 'Không có tên'} - ${st.classId?.className || 'Chưa phân lớp'}`
            })));
          }
        }
      } catch (err) {
        console.error("Error loading forum for edit:", err);
        setError("Lỗi khi tải thông tin diễn đàn: " + (err.response?.data?.error || err.message));
      } finally {
        setLoading(false);
      }
    };

    loadForumAndSubjects();
  }, [forumId, user]);

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!title.trim() || !description.trim() || !content.trim() || !subjectTeacherId) {
      setError('Vui lòng điền đầy đủ thông tin và chọn môn học.');
      return;
    }

    try {
      setSaving(true);
      setError('');

      const forumData = {
        id: parseInt(forumId),
        title,
        description,
        content,
        subjectTeacherId: parseInt(subjectTeacherId)
      };

      const response = await forumApis.updateForum(forumData);

      if (response.data.success) {
        alert("Cập nhật diễn đàn thành công!");
        navigate(`/forums/${forumId}`);
      } else {
        setError(response.data.error || 'Không thể cập nhật diễn đàn.');
      }
    } catch (err) {
      console.error("Error updating forum:", err);
      setError(`Lỗi khi cập nhật diễn đàn: ${err.response?.data?.error || err.message}`);
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <Container className="text-center my-5">
        <Spinner animation="border" variant="primary" />
        <p className="mt-3">Đang tải thông tin diễn đàn...</p>
      </Container>
    );
  }

  return (
    <Container className="my-4">
      <Button variant="secondary" as={Link} to={`/forums/${forumId}`} className="mb-3">
        <FontAwesomeIcon icon={faArrowLeft} className="me-2" />
        Quay lại diễn đàn
      </Button>

      <h2 className="mb-4">
        <FontAwesomeIcon icon={faComments} className="me-2" />
        Chỉnh sửa diễn đàn
      </h2>

      {error && (
        <Alert variant="danger" dismissible onClose={() => setError('')}>
          <FontAwesomeIcon icon={faExclamationTriangle} className="me-2" />
          {error}
        </Alert>
      )}

      <Card className="shadow-sm forum-detail-card">
        <Card.Header className="bg-primary text-white">
          <h5 className="mb-0">Thông tin diễn đàn</h5>
        </Card.Header>
        <Card.Body>
          <Form onSubmit={handleSubmit}>
            <Form.Group className="mb-3">
              <Form.Label>Môn học - Lớp</Form.Label>
              <Form.Select
                value={subjectTeacherId}
                onChange={(e) => setSubjectTeacherId(e.target.value)}
                required
                disabled={user.role !== 'Admin'} // Chỉ admin mới được đổi môn học
              >
                {subjectTeachers.length === 0 ? (
                  <option value="" disabled>Không có môn học nào được phân công</option>
                ) : (
                  subjectTeachers.map(subject => (
                    <option key={subject.id} value={subject.id}>
                      {subject.name}
                    </option>
                  ))
                )}
              </Form.Select>
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Tiêu đề</Form.Label>
              <Form.Control
                type="text"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                required
              />
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Mô tả ngắn</Form.Label>
              <Form.Control
                type="text"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                required
              />
            </Form.Group>

            <Form.Group className="mb-4">
              <Form.Label>Nội dung chi tiết</Form.Label>
              <Form.Control
                as="textarea"
                rows={5}
                value={content}
                onChange={(e) => setContent(e.target.value)}
                required
              />
            </Form.Group>

            <div className="d-flex justify-content-end">
              <Button variant="secondary" as={Link} to={`/forums/${forumId}`} className="me-2">
                Hủy
              </Button>
              <Button type="submit" variant="primary" disabled={saving}>
                {saving ? (
                  <>
                    <Spinner as="span" animation="border" size="sm" role="status" aria-hidden="true" className="me-2" />
                    Đang cập nhật...
                  </>
                ) : (
                  <>
                    <FontAwesomeIcon icon={faSave} className="me-2" />
                    Lưu thay đổi
                  </>
                )}
              </Button>
            </div>
          </Form>
        </Card.Body>
      </Card>
    </Container>
  );
};

export default ForumEdit;