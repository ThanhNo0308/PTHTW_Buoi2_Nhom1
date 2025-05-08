import React, { useState, useEffect, useContext } from 'react';
import { Container, Card, Form, Button, Alert, Spinner } from 'react-bootstrap';
import { useNavigate, Link } from 'react-router-dom';
import { forumApis, teacherClassApis } from '../configs/Apis';
import { MyUserContext } from '../App';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faComments, faArrowLeft, faSave, faExclamationTriangle } from '@fortawesome/free-solid-svg-icons';

const ForumCreate = () => {
  const [user] = useContext(MyUserContext);
  const navigate = useNavigate();
  
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [content, setContent] = useState('');
  const [subjectTeacherId, setSubjectTeacherId] = useState('');
  
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [subjectTeachers, setSubjectTeachers] = useState([]);
  const [loadingSubjects, setLoadingSubjects] = useState(true);
  
  // Load danh sách môn học khi component được mount
  useEffect(() => {
    loadSubjectTeachers();
  }, [user]);
  
  const loadSubjectTeachers = async () => {
    try {
      setLoadingSubjects(true);
      
      // 1. Đầu tiên lấy danh sách lớp được phân công
      const classResponse = await teacherClassApis.getTeacherClasses(user.username);
      
      if (classResponse.data && classResponse.data.classes) {
        // 2. Sau đó lấy thông tin môn học được phân công cho giảng viên
        const subjectsResponse = await teacherClassApis.getTeacherSubjects(user.username);
        
        if (subjectsResponse.data && subjectsResponse.data.success) {
          const subjects = subjectsResponse.data.subjectTeachers || [];
          
          setSubjectTeachers(subjects.map(st => ({
            id: st.id,
            name: `${st.subjectId?.subjectName || 'Không có tên'} - ${st.classId?.className || 'Chưa phân lớp'}`
          })));
          
          if (subjects.length > 0) {
            setSubjectTeacherId(subjects[0].id);
          }
        } else {
          // Giải pháp tạm thời: Tạo mảng từ danh sách lớp
          const classesWithSubjects = [];
          classResponse.data.classes.forEach(cls => {
            // Tạo một subjectTeacher giả định cho mỗi lớp
            classesWithSubjects.push({
              id: cls.id,
              name: `Lớp: ${cls.className}`
            });
          });
          
          setSubjectTeachers(classesWithSubjects);
          
          if (classesWithSubjects.length > 0) {
            setSubjectTeacherId(classesWithSubjects[0].id);
          }
        }
      }
    } catch (err) {
      console.error("Error loading subject teachers:", err);
      setError("Lỗi khi tải danh sách môn học. Vui lòng thử lại sau.");
    } finally {
      setLoadingSubjects(false);
    }
  };
  
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!title.trim() || !description.trim() || !content.trim() || !subjectTeacherId) {
      setError('Vui lòng điền đầy đủ thông tin và chọn môn học.');
      return;
    }
    
    try {
      setLoading(true);
      setError('');
      
      const forumData = {
        title,
        description,
        content,
        subjectTeacherId: parseInt(subjectTeacherId)
      };
      
      const response = await forumApis.addForum(forumData);
      if (response.data.success) {
        alert("Tạo diễn đàn thành công!");
        navigate(`/forums/${response.data.forumId}`);
      } else {
        setError(response.data.error || 'Không thể tạo diễn đàn mới.');
      }
    } catch (err) {
      console.error("Error creating forum:", err);
      setError(`Lỗi khi tạo diễn đàn: ${err.response?.data?.error || err.message}`);
    } finally {
      setLoading(false);
    }
  };
  
  if (loadingSubjects) {
    return (
      <Container className="text-center my-5">
        <Spinner animation="border" variant="primary" />
        <p className="mt-3">Đang tải thông tin môn học...</p>
      </Container>
    );
  }
  
  return (
    <Container className="my-4">
      <Button variant="secondary" as={Link} to="/forums" className="mb-3">
        <FontAwesomeIcon icon={faArrowLeft} className="me-2" />
        Quay lại danh sách diễn đàn
      </Button>
      
      <h2 className="mb-4">
        <FontAwesomeIcon icon={faComments} className="me-2" />
        Tạo diễn đàn mới
      </h2>
      
      {error && (
        <Alert variant="danger" dismissible onClose={() => setError('')}>
          <FontAwesomeIcon icon={faExclamationTriangle} className="me-2" />
          {error}
        </Alert>
      )}
      
      <Card className="shadow-sm">
        <Card.Body>
          <Form onSubmit={handleSubmit}>
            <Form.Group className="mb-3">
              <Form.Label>Môn học - Lớp</Form.Label>
              <Form.Select 
                value={subjectTeacherId} 
                onChange={(e) => setSubjectTeacherId(e.target.value)}
                required
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
              {subjectTeachers.length === 0 && (
                <Form.Text className="text-danger">
                  Bạn chưa được phân công môn học nào. Vui lòng liên hệ quản trị viên.
                </Form.Text>
              )}
            </Form.Group>
            
            <Form.Group className="mb-3">
              <Form.Label>Tiêu đề</Form.Label>
              <Form.Control 
                type="text" 
                value={title} 
                onChange={(e) => setTitle(e.target.value)}
                placeholder="Nhập tiêu đề diễn đàn"
                required
              />
            </Form.Group>
            
            <Form.Group className="mb-3">
              <Form.Label>Mô tả ngắn</Form.Label>
              <Form.Control 
                type="text" 
                value={description} 
                onChange={(e) => setDescription(e.target.value)}
                placeholder="Nhập mô tả ngắn về diễn đàn"
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
                placeholder="Nhập nội dung chi tiết của diễn đàn"
                required
              />
            </Form.Group>
            
            <div className="d-flex justify-content-end">
              <Button variant="secondary" as={Link} to="/forums" className="me-2">
                Hủy
              </Button>
              <Button type="submit" variant="success" disabled={loading || subjectTeachers.length === 0}>
                {loading ? (
                  <>
                    <Spinner as="span" animation="border" size="sm" role="status" aria-hidden="true" className="me-2" />
                    Đang tạo...
                  </>
                ) : (
                  <>
                    <FontAwesomeIcon icon={faSave} className="me-2" />
                    Tạo diễn đàn
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

export default ForumCreate;