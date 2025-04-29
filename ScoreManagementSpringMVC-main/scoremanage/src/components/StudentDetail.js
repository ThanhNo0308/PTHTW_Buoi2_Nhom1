import React, { useState, useEffect } from 'react';
import { Container, Card, Row, Col, Badge, Table, Alert, Spinner, Button } from 'react-bootstrap';
import { useParams, Link } from 'react-router-dom';
import { scoreApis } from '../configs/Apis';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faUserGraduate, faIdCard, faEnvelope, faPhone, faSchool, faArrowLeft, faChartBar, faExclamationTriangle } from '@fortawesome/free-solid-svg-icons';

const formatDate = (dateString) => {
  if (!dateString) return '-';
  const date = new Date(dateString);
  return date.toLocaleDateString('vi-VN');
};

const StudentDetail = () => {
  const { studentCode } = useParams();
  
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [student, setStudent] = useState(null);
  const [schoolYears, setSchoolYears] = useState([]);
  const [averageScores, setAverageScores] = useState({});
  
  // Tải dữ liệu sinh viên
  useEffect(() => {
    const loadStudentData = async () => {
      try {
        setLoading(true);
        setError('');
        
        const response = await scoreApis.getStudentDetail(studentCode);
        
        if (response.data && response.data.success) {
          setStudent(response.data.student);
          setSchoolYears(response.data.schoolYears || []);
          setAverageScores(response.data.averageScores || {});
        } else {
          setError(response.data?.message || 'Không thể tải thông tin sinh viên');
        }
      } catch (err) {
        console.error("Error loading student:", err);
        setError(`Lỗi: ${err.response?.data?.message || err.message}`);
      } finally {
        setLoading(false);
      }
    };
    
    if (studentCode) {
      loadStudentData();
    }
  }, [studentCode]);
  
  if (loading) {
    return (
      <Container className="mt-4 text-center">
        <Spinner animation="border" variant="primary" />
        <p className="mt-2">Đang tải thông tin sinh viên...</p>
      </Container>
    );
  }
  
  if (error) {
    return (
      <Container className="mt-4">
        <Alert variant="danger">
          <FontAwesomeIcon icon={faExclamationTriangle} className="me-2" />
          {error}
        </Alert>
        <Button as={Link} to="/teacher/students/search" variant="secondary">
          <FontAwesomeIcon icon={faArrowLeft} className="me-2" />
          Quay lại tìm kiếm
        </Button>
      </Container>
    );
  }
  
  if (!student) {
    return (
      <Container className="mt-4">
        <Alert variant="warning">
          <FontAwesomeIcon icon={faExclamationTriangle} className="me-2" />
          Không tìm thấy thông tin sinh viên.
        </Alert>
        <Button as={Link} to="/teacher/students/search" variant="secondary">
          <FontAwesomeIcon icon={faArrowLeft} className="me-2" />
          Quay lại tìm kiếm
        </Button>
      </Container>
    );
  }
  
  return (
    <Container className="mt-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h2>
          <FontAwesomeIcon icon={faUserGraduate} className="me-2" />
          Thông tin sinh viên: <span className="text-primary">{student.firstName} {student.lastName}</span>
        </h2>
        
        <Button as={Link} to="/teacher/students/search" variant="secondary">
          <FontAwesomeIcon icon={faArrowLeft} className="me-2" />
          Quay lại
        </Button>
      </div>
      
      <Row>
        <Col md={4}>
          <Card className="shadow-sm mb-4">
            <Card.Header className="bg-primary text-white">
              <Card.Title className="mb-0">Thông tin cá nhân</Card.Title>
            </Card.Header>
            <Card.Body className="text-center">
              <div className="mb-3">
                {student.avatar ? (
                  <img 
                    src={student.avatar}
                    alt={`${student.firstName} ${student.lastName}`}
                    className="rounded-circle"
                    style={{ width: '150px', height: '150px', objectFit: 'cover' }}
                  />
                ) : (
                  <div 
                    className="rounded-circle bg-light d-flex align-items-center justify-content-center mx-auto" 
                    style={{ width: '150px', height: '150px' }}
                  >
                    <FontAwesomeIcon icon={faUserGraduate} size="5x" className="text-secondary" />
                  </div>
                )}
              </div>
              
              <h4>{student.firstName} {student.lastName}</h4>
              <p className="text-muted">
                <FontAwesomeIcon icon={faIdCard} className="me-2" />
                {student.studentCode}
              </p>
              
              <hr />
              
              <div className="text-start">
                <p>
                  <FontAwesomeIcon icon={faEnvelope} className="me-2" />
                  <strong>Email:</strong> {student.email}
                </p>
                <p>
                  <FontAwesomeIcon icon={faPhone} className="me-2" />
                  <strong>Số điện thoại:</strong> {student.phone || '-'}
                </p>
                <p>
                  <FontAwesomeIcon icon={faSchool} className="me-2" />
                  <strong>Lớp:</strong> {student.classId?.className || '-'}
                </p>
                <p>
                  <strong>Ngày sinh:</strong> {formatDate(student.birthDate)}
                </p>
                <p>
                  <strong>Giới tính:</strong> {student.gender === 'M' ? 'Nam' : 'Nữ'}
                </p>
              </div>
            </Card.Body>
          </Card>
        </Col>
        
        <Col md={8}>
          <Card className="shadow-sm mb-4">
            <Card.Header className="bg-primary text-white">
              <Card.Title className="mb-0">Lịch sử học tập</Card.Title>
            </Card.Header>
            <Card.Body>
              <div className="table-responsive">
                <Table bordered hover>
                  <thead className="table-dark">
                    <tr>
                      <th>Năm học</th>
                      <th>Học kỳ</th>
                      <th>Điểm TB học kỳ</th>
                      <th>Thao tác</th>
                    </tr>
                  </thead>
                  <tbody>
                    {schoolYears.length > 0 ? (
                      schoolYears.map((schoolYear) => (
                        <tr key={schoolYear.id}>
                          <td>{schoolYear.yearStart}-{schoolYear.yearEnd}</td>
                          <td>{schoolYear.semesterName}</td>
                          <td>
                            {averageScores[schoolYear.id] ? (
                              <Badge bg="primary" pill>
                                {averageScores[schoolYear.id].toFixed(2)}
                              </Badge>
                            ) : (
                              <Badge bg="secondary" pill>-</Badge>
                            )}
                          </td>
                          <td>
                            <Link 
                              to={`/teacher/student/${student.studentCode}/scores?schoolYearId=${schoolYear.id}`} 
                              className="btn btn-sm btn-primary"
                            >
                              <FontAwesomeIcon icon={faChartBar} className="me-1" />
                              Xem điểm
                            </Link>
                          </td>
                        </tr>
                      ))
                    ) : (
                      <tr>
                        <td colSpan="4" className="text-center">Không có dữ liệu học tập</td>
                      </tr>
                    )}
                  </tbody>
                </Table>
              </div>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </Container>
  );
};

export default StudentDetail;