import React, { useState, useEffect } from 'react';
import { Container, Card, Row, Col, Table, Alert, Spinner, Button, Form } from 'react-bootstrap';
import { useParams, useSearchParams, Link } from 'react-router-dom';
import { scoreApis } from '../configs/Apis';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {
  faUserGraduate, faArrowLeft, faExclamationTriangle, faCheckCircle,
  faFilter, faSortAmountDown, faCalculator, faInfoCircle
} from '@fortawesome/free-solid-svg-icons';

const StudentScores = () => {
  const { studentCode } = useParams();
  const [searchParams] = useSearchParams();
  const initialSchoolYearId = searchParams.get('schoolYearId');

  // State variables
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [student, setStudent] = useState(null);
  const [scores, setScores] = useState([]);
  const [schoolYears, setSchoolYears] = useState([]);
  const [selectedSchoolYear, setSelectedSchoolYear] = useState(initialSchoolYearId || '');
  const [currentSchoolYear, setCurrentSchoolYear] = useState(null);
  const [semesterAverage, setSemesterAverage] = useState(null);
  const [allScoreTypes, setAllScoreTypes] = useState([]);
  const [subjectAverages, setSubjectAverages] = useState({});

  const scoreTableStyles = {
    unconfiguredScoreCell: {
      backgroundColor: '#f8f9fa',
      color: '#000',
      fontStyle: 'italic',
      position: 'relative'
    }
  };

  // Load student scores data
  useEffect(() => {
    const loadScores = async () => {
      try {
        setLoading(true);
        setError('');

        const response = await scoreApis.getStudentScores(
          studentCode,
          selectedSchoolYear || null
        );

        if (response.data && response.data.success) {
          setStudent(response.data.student || {});
          const validScores = (response.data.scores || []).filter(score => {
            return score && score.subjectTeacherID && score.subjectTeacherID.subjectId;
          });
          setScores(validScores);
          setSchoolYears(response.data.schoolYears || []);
          setCurrentSchoolYear(response.data.currentSchoolYear || null);
          setSubjectAverages(response.data.subjectAverages || {});
          setSemesterAverage(response.data.semesterAverage);

          // Thu thập tất cả các loại điểm từ dữ liệu
          const scoreTypesSet = new Set();
          response.data.scores.forEach(score => {
            if (score.scoreType && score.scoreType.scoreType) {
              scoreTypesSet.add(score.scoreType.scoreType);
            }
          });

          // Đảm bảo luôn có Giữa kỳ và Cuối kỳ
          if (!scoreTypesSet.has('Giữa kỳ')) {
            scoreTypesSet.add('Giữa kỳ');
          }
          if (!scoreTypesSet.has('Cuối kỳ')) {
            scoreTypesSet.add('Cuối kỳ');
          }

          // Sắp xếp các loại điểm
          const sortedTypes = Array.from(scoreTypesSet).sort((a, b) => {
            // Cuối kỳ luôn đứng cuối cùng
            if (a === 'Cuối kỳ') return 1;
            if (b === 'Cuối kỳ') return -1;

            // Giữa kỳ luôn đứng áp cuối (trước Cuối kỳ)
            if (a === 'Giữa kỳ') return 1;
            if (b === 'Giữa kỳ') return -1;

            // Các loại điểm bổ sung khác sắp xếp theo alphabet và nằm đầu tiên
            return a.localeCompare(b);
          });

          setAllScoreTypes(sortedTypes);
        } else {
          setError(response.data?.message || 'Không thể tải dữ liệu điểm');
        }
      } catch (err) {
        console.error("Error loading scores:", err);
        setError(`Lỗi: ${err.response?.data?.message || err.message}`);
      } finally {
        setLoading(false);
      }
    };

    if (studentCode) {
      loadScores();
    }
  }, [studentCode, selectedSchoolYear]);

  // Handle school year change
  const handleSchoolYearChange = (e) => {
    setSelectedSchoolYear(e.target.value);
  };



  // Group scores by subject
  const groupScoresBySubject = () => {
    const groupedScores = {};

    // Kiểm tra scores có phải là một mảng hợp lệ không
    if (!Array.isArray(scores) || scores.length === 0) {
      return [];
    }

    scores.forEach(score => {
      // Kiểm tra dữ liệu hợp lệ trước khi truy cập
      if (!score || !score.subjectTeacherID || !score.subjectTeacherID.subjectId) {
        console.error("Missing required properties in score object:", score);
        return; // Bỏ qua score này
      }

      const subjectId = score.subjectTeacherID.subjectId.id;
      const subjectName = score.subjectTeacherID.subjectId.subjectName || 'Chưa có tên';

      if (!groupedScores[subjectId]) {
        groupedScores[subjectId] = {
          subjectId,
          subjectName,
          credits: score.subjectTeacherID.subjectId.credits || 0,
          scores: {},
          weights: {}
        };
      }

      if (score.scoreType) {
        groupedScores[subjectId].scores[score.scoreType.scoreType] = score.scoreValue;
        // Nếu có thông tin về trọng số, lưu lại
        if (score.scoreType.weight) {
          groupedScores[subjectId].weights[score.scoreType.scoreType] = score.scoreType.weight;
        }
      }
    });

    return Object.values(groupedScores);
  };

  const groupedScoresBySubject = groupScoresBySubject();

  // Calculate subject average based on score weights
  const calculateSubjectAverage = (subject) => {
    // Ưu tiên sử dụng điểm trung bình từ API
    if (subjectAverages[subject.subjectId] !== undefined) {
      return subjectAverages[subject.subjectId];
    }

    // Kiểm tra xem có đủ cả điểm giữa kỳ và cuối kỳ không
    if (!subject.scores['Giữa kỳ'] || !subject.scores['Cuối kỳ']) {
      return null;  // Trả về null nếu không đủ các điểm bắt buộc
    }

    // Default weights if not configured
    const defaultWeights = {
      'Giữa kỳ': 0.4,
      'Cuối kỳ': 0.6
    };

    let totalWeightedScore = 0;
    let totalWeight = 0;

    Object.keys(subject.scores).forEach(type => {
      const score = subject.scores[type];
      if (score === undefined || score === null) return;

      // Sử dụng trọng số từ dữ liệu nếu có, nếu không dùng giá trị mặc định
      const weight = subject.weights[type] || defaultWeights[type] ||
        (1 / Object.keys(subject.scores).length);

      totalWeightedScore += score * weight;
      totalWeight += weight;
    });

    if (totalWeight > 0) {
      return totalWeightedScore / totalWeight;
    }

    return null;
  };

  const isScoreTypeConfigured = (subject, scoreType) => {
    // Nếu đã có điểm của loại này thì coi như đã cấu hình
    if (subject.scores[scoreType] !== undefined) return true;

    // Giữa kỳ và Cuối kỳ luôn được coi là có cấu hình
    if (scoreType === 'Giữa kỳ' || scoreType === 'Cuối kỳ') return true;

    // Kiểm tra xem môn học này có điểm nào khác không
    // Nếu có ít nhất một điểm khác, thì loại điểm này không được cấu hình
    return Object.keys(subject.scores).length === 0;
  };

  // Thêm hàm tính điểm trung bình học kỳ
  const calculateSemesterAverage = () => {
    // Sử dụng giá trị từ API nếu có
    if (semesterAverage !== null && semesterAverage !== undefined && semesterAverage > 0) {
      return semesterAverage;
    }

    // Tính toán dự phòng nếu API không trả về semesterAverage
    let totalWeightedScore = 0;
    let totalCredits = 0;

    groupedScoresBySubject.forEach(subject => {
      const avgScore = calculateSubjectAverage(subject);
      if (avgScore !== null) {
        totalWeightedScore += avgScore * subject.credits;
        totalCredits += subject.credits;
      }
    });

    return totalCredits > 0 ? totalWeightedScore / totalCredits : null;
  };

  const calculatedSemesterAverage = calculateSemesterAverage();

  if (loading) {
    return (
      <Container className="mt-4 text-center">
        <Spinner animation="border" variant="primary" />
        <p className="mt-2">Đang tải bảng điểm...</p>
      </Container>
    );
  }

  return (
    <Container className="mt-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h2>
          <FontAwesomeIcon icon={faUserGraduate} className="me-2" />
          Bảng điểm: <span className="text-primary">
            {student ? `${student.lastName} ${student.firstName}` : studentCode}
          </span>
        </h2>

        <Button as={Link} to={`/teacher/student/${studentCode}/detail`} variant="secondary">
          <FontAwesomeIcon icon={faArrowLeft} className="me-2" />
          Quay lại
        </Button>
      </div>

      {/* Error and Success Alerts */}
      {error && (
        <Alert variant="danger" dismissible onClose={() => setError('')}>
          <FontAwesomeIcon icon={faExclamationTriangle} className="me-2" />
          {error}
        </Alert>
      )}

      {success && (
        <Alert variant="success" dismissible onClose={() => setSuccess('')}>
          <FontAwesomeIcon icon={faCheckCircle} className="me-2" />
          {success}
        </Alert>
      )}

      {/* Student Info Card */}
      <Row className="mb-4">
        <Col md={4}>
          <Card className="shadow-sm">
            <Card.Header className="bg-primary text-white">
              <Card.Title className="mb-0">Thông tin sinh viên</Card.Title>
            </Card.Header>
            <Card.Body>
              <p><strong>Mã sinh viên:</strong> {student?.studentCode}</p>
              <p><strong>Họ và tên:</strong> {student?.lastName} {student?.firstName}</p>
              <p><strong>Lớp:</strong> {student?.classId?.className || '-'}</p>
              <p><strong>Email:</strong> {student?.email || '-'}</p>
            </Card.Body>
          </Card>
        </Col>

        <Col md={8}>
          <Card className="shadow-sm">
            <Card.Header className="bg-info text-white">
              <Card.Title className="mb-0 d-flex justify-content-between align-items-center">
                <span>
                  <FontAwesomeIcon icon={faFilter} className="me-2" />
                  Lọc điểm theo học kỳ
                </span>
                {calculatedSemesterAverage !== null && (
                  <span className="badge bg-light text-dark">
                    <FontAwesomeIcon icon={faCalculator} className="me-1" />
                    Điểm TB học kỳ: <span className="text-primary fw-bold">{calculatedSemesterAverage.toFixed(2)}</span>
                  </span>
                )}
              </Card.Title>
            </Card.Header>
            <Card.Body>
              <Form.Group>
                <Form.Select
                  value={selectedSchoolYear}
                  onChange={handleSchoolYearChange}
                  className="mb-3"
                >
                  <option value="">-- Tất cả học kỳ --</option>
                  {schoolYears.map(year => (
                    <option
                      key={year.id}
                      value={year.id}
                    >
                      {year.nameYear} {year.semesterName}
                      {year.isCurrent ? ' (Hiện tại)' : ''}
                    </option>
                  ))}
                </Form.Select>
              </Form.Group>

              {currentSchoolYear && (
                <Alert variant="info">
                  Đang hiển thị điểm học kỳ: <strong>{currentSchoolYear.nameYear} {currentSchoolYear.semesterName}</strong>
                </Alert>
              )}
            </Card.Body>
          </Card>
        </Col>
      </Row>

      {/* Scores Table */}
      <Card className="shadow-sm">
        <Card.Header className="bg-primary text-white">
          <Card.Title className="mb-0">
            <FontAwesomeIcon icon={faSortAmountDown} className="me-2" />
            Bảng điểm
          </Card.Title>
        </Card.Header>
        <Card.Body>
          {groupedScoresBySubject.length > 0 ? (
            <div className="table-responsive text-center">
              <Table bordered hover>
                <thead className="table-light">
                  <tr>
                    <th>STT</th>
                    <th>Tên môn học</th>
                    <th>Số tín chỉ</th>
                    {/* Tạo cột động cho mỗi loại điểm */}
                    {allScoreTypes.map(scoreType => (
                      <th key={scoreType}>{scoreType}</th>
                    ))}
                    <th>Điểm TB</th>
                    <th>Kết quả</th>
                  </tr>
                </thead>
                <tbody>
                  {groupedScoresBySubject.map((subject, index) => {
                    const avgScore = calculateSubjectAverage(subject);
                    const isPassed = avgScore !== null ? avgScore >= 5 : false;

                    return (
                      <tr key={subject.subjectId}>
                        <td>{index + 1}</td>
                        <td>{subject.subjectName}</td>
                        <td className="text-center">{subject.credits}</td>
                        {/* Hiển thị điểm cho từng loại điểm */}
                        {allScoreTypes.map(scoreType => (
                          <td
                            key={`${subject.subjectId}-${scoreType}`}
                            className="text-center"
                            style={!isScoreTypeConfigured(subject, scoreType) ? scoreTableStyles.unconfiguredScoreCell : {}}
                            title={!isScoreTypeConfigured(subject, scoreType) ? 'Loại điểm này không được cấu hình cho môn học' : ''}
                          >
                            {subject.scores[scoreType] !== undefined ? subject.scores[scoreType].toFixed(1) : '—'}
                          </td>
                        ))}
                        <td className={`text-center ${avgScore !== null ? 'fw-bold' : ''}`}>
                          {avgScore !== null ? avgScore.toFixed(2) : '-'}
                        </td>
                        <td className="text-center">
                          {avgScore !== null && (
                            <span className={`badge ${isPassed ? 'bg-success' : 'bg-danger'}`}>
                              {isPassed ? 'Đạt' : 'Không đạt'}
                            </span>
                          )}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </Table>
            </div>
          ) : (
            <Alert variant="info">
              Không có dữ liệu điểm nào được tìm thấy.
            </Alert>
          )}
          {groupedScoresBySubject.length > 0 && (
            <div className="mt-2 text-muted small">
              <span className="d-inline-block px-2 py-1 me-2" style={scoreTableStyles.unconfiguredScoreCell}>—</span>
              <span>Chưa có điểm hoặc Loại điểm không được cấu hình cho môn học này</span>
            </div>
          )}
          <Alert variant="info" className="mt-3">
            <FontAwesomeIcon icon={faInfoCircle} className="me-2" />
            Chỉ hiển thị điểm đã được giảng viên chính thức khóa điểm. Một số điểm có thể chưa hiển thị nếu giảng viên chưa khóa điểm.
          </Alert>
        </Card.Body>
      </Card>
    </Container>
  );
};

export default StudentScores;