import React, { useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Table, Alert, Spinner, Form, Row, Col, Badge } from 'react-bootstrap';
import { MyUserContext } from "../App";
import { studentApis } from '../configs/Apis';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {
    faGraduationCap,
    faExclamationTriangle,
    faCalendarAlt,
    faSortAmountDown,
    faCalculator,
    faFilter,
    faCheckCircle
} from '@fortawesome/free-solid-svg-icons';

const StudentScoresList = () => {
    const [user] = useContext(MyUserContext);
    const navigate = useNavigate();

    // State variables
    const [student, setStudent] = useState(null);
    const [scores, setScores] = useState([]);
    const [schoolYears, setSchoolYears] = useState([]);
    const [selectedSchoolYear, setSelectedSchoolYear] = useState(null);
    const [currentSchoolYear, setCurrentSchoolYear] = useState(null);
    const [subjectAverages, setSubjectAverages] = useState({});
    const [semesterAverage, setSemesterAverage] = useState(0);
    const [enrolledSubjects, setEnrolledSubjects] = useState({});
    const [scoreTypes, setScoreTypes] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const scoreTableStyles = {
        unconfiguredScoreCell: {
            backgroundColor: '#f8f9fa',  // Màu nền nhạt
            color: 'black',            // Màu chữ nhạt
            fontStyle: 'italic',         // Chữ nghiêng
            position: 'relative'         // Để có thể thêm dấu hiệu nếu cần
        }
    };

    useEffect(() => {
        if (!user) {
            navigate('/login');
            return;
        }
    
        if (user.role !== 'Student') {
            navigate('/');
            return;
        }
    
        const loadScores = async () => {
            try {
                setLoading(true);
                setError(null);
    
                const response = await studentApis.getStudentScores(selectedSchoolYear);
                console.log("API response:", response.data);
    
                if (response.data) {
                    // Set scores data
                    setScores(response.data.scores || []);
    
                    // Set school years (filtered to only show those with enrollments)
                    setSchoolYears(response.data.schoolYears || []);
    
                    // Set current school year
                    setCurrentSchoolYear(response.data.currentSchoolYear);
    
                    // Set subject averages
                    setSubjectAverages(response.data.subjectAverages || {});
    
                    // Set semester average
                    setSemesterAverage(response.data.semesterAverage || 0);
    
                    // Set enrolled subjects
                    setEnrolledSubjects(response.data.enrolledSubjects || {});
    
                    // Mặc định luôn có Giữa kỳ và Cuối kỳ
                    const types = new Set(['Giữa kỳ', 'Cuối kỳ']);
                    
                    // Thêm các loại điểm khác từ dữ liệu API
                    (response.data.scores || []).forEach(score => {
                        if (score.scoreType && score.scoreType.scoreType 
                            && score.scoreType.scoreType !== 'Giữa kỳ' 
                            && score.scoreType.scoreType !== 'Cuối kỳ') {
                            types.add(score.scoreType.scoreType);
                        }
                    });
    
                    // Sắp xếp loại điểm
                    const sortedTypes = Array.from(types).sort((a, b) => {
                        // Cuối kỳ luôn đứng cuối cùng
                        if (a === 'Cuối kỳ') return 1;
                        if (b === 'Cuối kỳ') return -1;
                        
                        // Giữa kỳ luôn đứng áp cuối (trước Cuối kỳ)
                        if (a === 'Giữa kỳ') return 1;
                        if (b === 'Giữa kỳ') return -1;
                        
                        // Các loại điểm bổ sung khác sắp xếp theo alphabet và nằm đầu tiên
                        return a.localeCompare(b);
                    });
    
                    setScoreTypes(sortedTypes);
                }
            } catch (err) {
                console.error("Error loading scores:", err);
                setError("Không thể tải điểm. Vui lòng thử lại sau. " +
                    (err.response?.data?.error || err.message));
            } finally {
                setLoading(false);
            }
        };
    
        loadScores();
    }, [user, navigate, selectedSchoolYear]);

    // Handler for school year change
    const handleSchoolYearChange = (e) => {
        setSelectedSchoolYear(e.target.value ? parseInt(e.target.value) : null);
    };

    const groupScoresBySubject = () => {
        const groupedScores = {};

        // Đầu tiên, thêm tất cả môn học đã đăng ký (chưa có điểm)
        if (currentSchoolYear && enrolledSubjects[currentSchoolYear.id]) {
            enrolledSubjects[currentSchoolYear.id].forEach(subject => {
                groupedScores[subject.id] = {
                    subjectId: subject.id,
                    subjectName: subject.name,
                    credits: subject.credits || 0,
                    scores: {}
                };
            });
        }

        // Sau đó, thêm điểm cho các môn học (nếu có)
        if (Array.isArray(scores) && scores.length > 0) {
            scores.forEach(score => {
                // Validate required data exists
                if (!score || !score.subjectTeacherID || !score.subjectTeacherID.subjectId) {
                    console.error("Missing required properties in score object:", score);
                    return;
                }

                const subjectId = score.subjectTeacherID.subjectId.id;
                const subjectName = score.subjectTeacherID.subjectId.subjectName || 'Không có tên';
                const credits = score.subjectTeacherID.subjectId.credits || 0;

                // (trường hợp môn học có điểm nhưng không nằm trong enrolledSubjects)
                if (!groupedScores[subjectId]) {
                    groupedScores[subjectId] = {
                        subjectId,
                        subjectName,
                        credits,
                        scores: {}
                    };
                }

                // Add score for this subject
                if (score.scoreType && score.scoreType.scoreType) {
                    groupedScores[subjectId].scores[score.scoreType.scoreType] = score.scoreValue;
                }
            });
        }

        return Object.values(groupedScores);
    };

    // Calculate average for a subject based on its scores
    const calculateSubjectAverage = (subjectId, subject) => {
        // Kiểm tra xem môn học có đủ cả điểm giữa kỳ và cuối kỳ không
        if (!subject.scores ||
            subject.scores['Giữa kỳ'] === undefined ||
            subject.scores['Cuối kỳ'] === undefined) {
            return '-';
        }

        return subjectAverages[subjectId] ? subjectAverages[subjectId].toFixed(2) : '-';
    };



    const hasRequiredScores = (subject) => {
        return (
            subject.scores &&
            subject.scores['Giữa kỳ'] !== undefined &&
            subject.scores['Cuối kỳ'] !== undefined
        );
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
    const groupedSubjects = groupScoresBySubject();

    const calculateFilteredSemesterAverage = () => {
        // Lọc các môn có đủ điểm giữa kỳ và cuối kỳ
        const validSubjects = groupedSubjects.filter(subject =>
            hasRequiredScores(subject)
        );

        // Tính điểm trung bình học kỳ từ các môn hợp lệ
        let totalWeightedScore = 0;
        let totalCredits = 0;

        validSubjects.forEach(subject => {
            const subjectAvg = subjectAverages[subject.subjectId];
            if (subjectAvg && subject.credits) {
                totalWeightedScore += subjectAvg * subject.credits;
                totalCredits += subject.credits;
            }
        });

        return totalCredits > 0 ? totalWeightedScore / totalCredits : 0;
    };

    const filteredSemesterAverage = calculateFilteredSemesterAverage();


    if (loading) {
        return (
            <div className="container mt-4 text-center">
                <Spinner animation="border" variant="primary" />
                <p className="mt-2">Đang tải bảng điểm...</p>
            </div>
        );
    }

    return (
        <div className="container mt-4">
            <h2 className="mb-4">
                <FontAwesomeIcon icon={faGraduationCap} className="me-2" />
                Bảng điểm cá nhân
            </h2>

            {/* Error message display */}
            {error && (
                <Alert variant="danger" dismissible onClose={() => setError(null)}>
                    <FontAwesomeIcon icon={faExclamationTriangle} className="me-2" />
                    {error}
                </Alert>
            )}


            {/* School year filter */}
            <Row className="mb-4">
                <Col>
                    <Card className="shadow-sm">
                        <Card.Header className="bg-info text-white">
                            <Card.Title className="mb-0 d-flex justify-content-between align-items-center">
                                <span>
                                    <FontAwesomeIcon icon={faFilter} className="me-2" />
                                    Lọc điểm theo học kỳ
                                </span>
                                {semesterAverage > 0 && (
                                    <Badge bg="light" text="dark">
                                        <FontAwesomeIcon icon={faCalculator} className="me-1" />
                                        Điểm TB học kỳ: <span className="text-primary fw-bold">{filteredSemesterAverage.toFixed(2)}</span>
                                    </Badge>
                                )}
                            </Card.Title>
                        </Card.Header>
                        <Card.Body>
                            <Form.Group>
                                <Form.Select
                                    value={selectedSchoolYear || ''}
                                    onChange={handleSchoolYearChange}
                                    className="mb-3"
                                >
                                    <option value="">-- Chọn học kỳ --</option>
                                    {schoolYears.map(year => (
                                        <option
                                            key={year.id}
                                            value={year.id}
                                        >
                                            {year.nameYear || `${year.yearStart}-${year.yearEnd}`} {year.semesterName}
                                            {currentSchoolYear && year.id === currentSchoolYear.id ? ' (Hiện tại)' : ''}
                                        </option>
                                    ))}
                                </Form.Select>
                            </Form.Group>

                            {currentSchoolYear && (
                                <Alert variant="info">
                                    <FontAwesomeIcon icon={faCalendarAlt} className="me-2" />
                                    Đang hiển thị điểm học kỳ: <strong>
                                        {currentSchoolYear.nameYear || `${currentSchoolYear.yearStart}-${currentSchoolYear.yearEnd}`} {currentSchoolYear.semesterName}
                                    </strong>
                                </Alert>
                            )}
                        </Card.Body>
                    </Card>
                </Col>
            </Row>

            {/* Scores table */}
            <Card className="shadow-sm">
                <Card.Header className="bg-primary text-white">
                    <Card.Title className="mb-0">
                        <FontAwesomeIcon icon={faSortAmountDown} className="me-2" />
                        Bảng điểm
                    </Card.Title>
                </Card.Header>
                <Card.Body>
                    {groupedSubjects.length > 0 ? (
                        <div className="table-responsive text-center">
                            <Table bordered hover>
                                <thead className="table-light">
                                    <tr>
                                        <th>STT</th>
                                        <th>Tên môn học</th>
                                        <th>Số tín chỉ</th>
                                        {/* Dynamic columns for each score type */}
                                        {scoreTypes.map(type => (
                                            <th key={type}>{type}</th>
                                        ))}
                                        <th>Điểm TB</th>
                                        <th>Kết quả</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {groupedSubjects.map((subject, index) => {
                                        const avgScore = calculateSubjectAverage(subject.subjectId, subject);
                                        const isPassed = avgScore !== '-' && parseFloat(avgScore) >= 4.0;

                                        return (
                                            <tr key={subject.subjectId}>
                                                <td>{index + 1}</td>
                                                <td className="text-start">{subject.subjectName}</td>
                                                <td>{subject.credits}</td>

                                                {/* Display scores for each type */}
                                                {scoreTypes.map(type => (
                                                    <td
                                                        key={`${subject.subjectId}-${type}`}
                                                        // Thêm lớp CSS và thuộc tính title cho các ô không cấu hình
                                                        className={isScoreTypeConfigured(subject, type) ? '' : 'unconfigured-score-cell'}
                                                        title={isScoreTypeConfigured(subject, type) ? '' : 'Loại điểm này không được cấu hình cho môn học'}
                                                    >
                                                        {subject.scores[type] !== undefined ? subject.scores[type].toFixed(1) : '—'}
                                                    </td>
                                                ))}

                                                <td className={hasRequiredScores(subject) && avgScore !== '-' ? 'fw-bold' : ''}>
                                                    {hasRequiredScores(subject) ? avgScore : '-'}
                                                </td>
                                                <td>
                                                    {hasRequiredScores(subject) && avgScore !== '-' && (
                                                        <Badge bg={isPassed ? 'success' : 'danger'}>
                                                            {isPassed ? 'Đạt' : 'Không đạt'}
                                                        </Badge>
                                                    )}
                                                </td>
                                            </tr>
                                        );
                                    })}
                                </tbody>
                                <tfoot className="table-secondary">
                                    <tr>
                                        <th colSpan={3 + scoreTypes.length} className="text-end">Điểm trung bình học kỳ:</th>
                                        <th>{filteredSemesterAverage > 0 ? filteredSemesterAverage.toFixed(2) : '-'}</th>
                                        <th>
                                            {filteredSemesterAverage >= 5.0 ? (
                                                <Badge bg="success">Đạt</Badge>
                                            ) : filteredSemesterAverage > 0 ? (
                                                <Badge bg="danger">Không đạt</Badge>
                                            ) : null}
                                        </th>
                                    </tr>
                                </tfoot>
                            </Table>
                        </div>
                    ) : (
                        <Alert variant="info">
                            <FontAwesomeIcon icon={faExclamationTriangle} className="me-2" />
                            Không có dữ liệu điểm nào được tìm thấy. Hãy chọn học kỳ khác hoặc liên hệ với giáo viên để biết thêm thông tin.
                        </Alert>
                    )}
                    {groupedSubjects.length > 0 && (
                        <div className="mt-2 text-muted small">
                            <span className="d-inline-block px-2 py-1 me-2" style={scoreTableStyles.unconfiguredScoreCell}>—</span>
                            <span>Chưa có điểm hoặc Loại điểm không được cấu hình cho môn học này</span>
                        </div>
                    )}
                </Card.Body>
            </Card>
        </div>
    );
};

export default StudentScoresList;