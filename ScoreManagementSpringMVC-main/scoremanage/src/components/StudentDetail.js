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
  const [enrolledSubjects, setEnrolledSubjects] = useState({});
  const [subjectScoresByYear, setSubjectScoresByYear] = useState({});

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
          setEnrolledSubjects(response.data.enrolledSubjects || {});
          setSubjectScoresByYear(response.data.subjectScoresByYear || {});

          // Log để kiểm tra
          console.log("School years:", response.data.schoolYears);
          console.log("Average scores:", response.data.averageScores);

          // Lọc ra các học kỳ có điểm hoặc có môn học đã đăng ký
          const filteredSchoolYears = response.data.schoolYears?.filter(year =>
            response.data.averageScores[year.id] !== undefined ||
            (response.data.enrolledSubjects && response.data.enrolledSubjects[year.id]?.length > 0)
          ) || [];

          setSchoolYears(filteredSchoolYears);
          setAverageScores(response.data.averageScores || {});
          setEnrolledSubjects(response.data.enrolledSubjects || {});
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

  const formatSchoolYear = (yearString) => {
    if (!yearString) return '-';

    // Xử lý chuỗi timestamp dạng "1738342800000-1751216400000"
    if (yearString.includes('-')) {
      try {
        const parts = yearString.split('-');

        // Chuyển timestamp thành đối tượng Date
        const startDate = new Date(Number(parts[0]));
        const endDate = new Date(Number(parts[1]));

        // Lấy năm
        if (!isNaN(startDate.getFullYear()) && !isNaN(endDate.getFullYear())) {
          return `${startDate.getFullYear()}-${endDate.getFullYear()}`;
        }
      } catch (e) {
        console.error("Error formatting year:", e);
      }
    }

    return yearString;
  };

  const calculateSemesterAverage = (schoolYearId) => {
    if (!subjectScoresByYear[schoolYearId]) return null;

    const subjects = subjectScoresByYear[schoolYearId];
    let totalWeightedScore = 0;
    let totalCredits = 0;

    subjects.forEach(subject => {
      const credits = subject.credits || 0;
      const avgScore = subject.averageScore || 0;

      totalWeightedScore += avgScore * credits;
      totalCredits += credits;
    });

    if (totalCredits > 0) {
      return (totalWeightedScore / totalCredits).toFixed(2);
    }

    return null;
  };

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
          Thông tin sinh viên: <span className="text-primary">{student.lastName} {student.firstName} </span>
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
                    alt={`${student.lastName} ${student.firstName}`}
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

              <h4>{student.lastName} {student.firstName}</h4>
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
                  <strong>Ngày sinh:</strong> {formatDate(student.birthdate || student.birthDate)}
                </p>
                <p>
                  <strong>Giới tính:</strong> {student.gender === 0 ? 'Nam' : 'Nữ'}
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
              <div className="table-responsive text-center">
                <Table bordered hover>
                  <thead className="table-dark">
                    <tr>
                      <th>Năm học</th>
                      <th>Học kỳ</th>
                      <th>Điểm TB học kỳ (hệ 10)</th>
                      <th>Thao tác</th>
                    </tr>
                  </thead>
                  <tbody>
                    {schoolYears.length > 0 ? (
                      schoolYears.map((schoolYear) => (
                        <tr key={schoolYear.id}>
                          <td>
                            <div className="position-relative" title={
                              enrolledSubjects && enrolledSubjects[schoolYear.id] ?
                                enrolledSubjects[schoolYear.id].map(sub => sub.name).join(", ") :
                                "Không có môn học nào"
                            }>
                              {/* Ưu tiên sử dụng trường nameYear đã có sẵn */}
                              {schoolYear.nameYear || formatSchoolYear(schoolYear.id?.toString())}
                            </div>
                          </td>
                          <td>{schoolYear.semesterName}</td>
                          <td className="text-center">
                            {(() => {
                              // Ưu tiên tính toán từ dữ liệu chi tiết môn học nếu có
                              const calculatedAverage = calculateSemesterAverage(schoolYear.id);
                              if (calculatedAverage) {
                                return (
                                  <Badge bg="primary" pill>
                                    {calculatedAverage}
                                  </Badge>
                                );
                              }
                              // Sử dụng điểm từ API nếu không có chi tiết
                              else if (averageScores[schoolYear.id]) {
                                return (
                                  <Badge bg="primary" pill>
                                    {averageScores[schoolYear.id].toFixed(2)}
                                  </Badge>
                                );
                              }
                              // Không có điểm
                              else {
                                return (
                                  <Badge bg="secondary" pill>-</Badge>
                                );
                              }
                            })()}
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