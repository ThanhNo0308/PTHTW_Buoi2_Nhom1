import React, { useState, useEffect } from 'react';
import { Container, Card, Row, Col, Table, Alert, Spinner, Button, Form } from 'react-bootstrap';
import { useParams, useSearchParams, Link } from 'react-router-dom';
import { scoreApis } from '../configs/Apis';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { 
  faUserGraduate, 
  faArrowLeft, 
  faExclamationTriangle, 
  faCheckCircle,
  faFileDownload,
  faFilter,
  faSortAmountDown,
  faCalculator
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
          setScores(response.data.scores || []);
          setSchoolYears(response.data.schoolYears || []);
          setCurrentSchoolYear(response.data.currentSchoolYear || null);
          setSemesterAverage(response.data.semesterAverage);
          
          // Thu thập tất cả các loại điểm từ dữ liệu
          const scoreTypesSet = new Set();
          response.data.scores.forEach(score => {
            if (score.scoreType && score.scoreType.scoreType) {
              scoreTypesSet.add(score.scoreType.scoreType);
            }
          });
          
          // Đảm bảo "Giữa kỳ" và "Cuối kỳ" luôn hiển thị đầu tiên nếu có
          const types = Array.from(scoreTypesSet);
          const midtermIndex = types.indexOf('Giữa kỳ');
          const finalIndex = types.indexOf('Cuối kỳ');
          
          const sortedTypes = [];
          
          // Thêm Giữa kỳ nếu có
          if (midtermIndex !== -1) {
            sortedTypes.push('Giữa kỳ');
            types.splice(midtermIndex, 1);
          }
          
          // Thêm Cuối kỳ nếu có
          if (finalIndex !== -1) {
            sortedTypes.push('Cuối kỳ');
            types.splice(types.indexOf('Cuối kỳ'), 1);
          }
          
          // Thêm các loại điểm còn lại theo thứ tự alphabet
          sortedTypes.push(...types.sort());
          
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
    
    scores.forEach(score => {
      const subjectId = score.subjectTeacherID.subjectID.id;
      const subjectName = score.subjectTeacherID.subjectID.subjectName;
      const subjectCode = score.subjectTeacherID.subjectID.subjectCode;
      
      if (!groupedScores[subjectId]) {
        groupedScores[subjectId] = {
          subjectId,
          subjectName,
          subjectCode,
          credits: score.subjectTeacherID.subjectID.credits || 0,
          scores: {},
          weights: {} // Thêm thông tin về trọng số
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
  
  // Calculate subject average based on score weights
  const calculateSubjectAverage = (subjectScores, subjectWeights) => {
    // Default weights if not configured
    const defaultWeights = {
      'Giữa kỳ': 0.4,
      'Cuối kỳ': 0.6
    };
    
    let totalWeightedScore = 0;
    let totalWeight = 0;
    
    Object.keys(subjectScores).forEach(type => {
      const score = subjectScores[type];
      
      // Sử dụng trọng số từ dữ liệu nếu có, nếu không dùng giá trị mặc định
      // hoặc phân bổ đều cho các loại điểm khác
      const weight = subjectWeights[type] || defaultWeights[type] || 
                    (1 / Object.keys(subjectScores).length);
      
      totalWeightedScore += score * weight;
      totalWeight += weight;
    });
    
    if (totalWeight > 0) {
      return totalWeightedScore / totalWeight;
    }
    
    return null;
  };
  
  const groupedScoresBySubject = groupScoresBySubject();
  
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
            {student ? `${student.firstName} ${student.lastName}` : studentCode}
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
              <p><strong>Họ và tên:</strong> {student?.firstName} {student?.lastName}</p>
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
                {semesterAverage !== null && (
                  <span className="badge bg-light text-dark">
                    <FontAwesomeIcon icon={faCalculator} className="me-1" />
                    Điểm TB học kỳ: <span className="text-primary fw-bold">{semesterAverage.toFixed(2)}</span>
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
              
              <div className="d-flex justify-content-end">
                <Button variant="outline-primary" size="sm">
                  <FontAwesomeIcon icon={faFileDownload} className="me-2" />
                  Xuất bảng điểm
                </Button>
              </div>
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
            <div className="table-responsive">
              <Table bordered hover>
                <thead className="table-light">
                  <tr>
                    <th>STT</th>
                    <th>Mã môn học</th>
                    <th>Tên môn học</th>
                    <th>Số tín chỉ</th>
                    {/* Tạo cột động cho mỗi loại điểm */}
                    {allScoreTypes.map(scoreType => (
                      <th key={scoreType}>Điểm {scoreType}</th>
                    ))}
                    <th>Điểm TB</th>
                    <th>Kết quả</th>
                  </tr>
                </thead>
                <tbody>
                  {groupedScoresBySubject.map((subject, index) => {
                    const avgScore = calculateSubjectAverage(subject.scores, subject.weights);
                    const isPassed = avgScore !== null ? avgScore >= 5 : false;
                    
                    return (
                      <tr key={subject.subjectId}>
                        <td>{index + 1}</td>
                        <td>{subject.subjectCode}</td>
                        <td>{subject.subjectName}</td>
                        <td className="text-center">{subject.credits}</td>
                        {/* Hiển thị điểm cho từng loại điểm */}
                        {allScoreTypes.map(scoreType => (
                          <td key={`${subject.subjectId}-${scoreType}`} className="text-center">
                            {subject.scores[scoreType] !== undefined ? subject.scores[scoreType] : '-'}
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
        </Card.Body>
      </Card>
    </Container>
  );
};

export default StudentScores;