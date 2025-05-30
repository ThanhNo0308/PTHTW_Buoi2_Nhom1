import React, { useContext, useEffect, useState } from 'react';
import { useParams, Link, useSearchParams, useNavigate } from 'react-router-dom';
import { Card, Button, Container, Row, Col, Alert, Spinner, Table, Badge, Form } from 'react-bootstrap';
import { MyUserContext } from '../App';
import { teacherClassApis } from '../configs/Apis';
import { useSchoolYear } from '../reducers/SchoolYearContext';
import { UniqueSubjectTeacherIdContext } from '../reducers/UniqueSubjectTeacherIdContext';

const TeacherClassDetail = () => {
    const { classId } = useParams();
    const [searchParams, setSearchParams] = useSearchParams();
    const schoolYearIdParam = searchParams.get('schoolYearId');
    const navigate = useNavigate();

    const [user] = useContext(MyUserContext);
    const { selectedSchoolYearId, setSelectedSchoolYearId } = useSchoolYear();
    const { setSelectedSubjectTeacherId } = useContext(UniqueSubjectTeacherIdContext);

    const [classData, setClassData] = useState(null);
    const [students, setStudents] = useState([]);
    const [subjects, setSubjects] = useState([]);
    const [assignedSchoolYears, setAssignedSchoolYears] = useState([]);
    const [currentSchoolYearId, setCurrentSchoolYearId] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        const loadClassDetail = async () => {
            try {
                setLoading(true);
                // Sử dụng schoolYearIdParam nếu được cung cấp trong URL
                const apiUrl = schoolYearIdParam ?
                    `${classId}?username=${user.username}&schoolYearId=${schoolYearIdParam}` :
                    `${classId}?username=${user.username}`;

                const response = await teacherClassApis.getClassDetail(apiUrl);

                if (response.data) {
                    setClassData(response.data.classroom || null);
                    setStudents(response.data.students || []);
                    setSubjects(response.data.subjects || []);
                    setAssignedSchoolYears(response.data.assignedSchoolYears || []);

                    // Lưu học kỳ đang được chọn
                    if (response.data.selectedSchoolYearId) {
                        setCurrentSchoolYearId(response.data.selectedSchoolYearId);
                    }
                }
            } catch (err) {
                console.error("Error loading class detail:", err);
                setError("Không thể tải thông tin lớp học. " + (err.response?.data?.error || "Vui lòng thử lại sau."));
            } finally {
                setLoading(false);
            }
        };

        if (user && user.username && classId) {
            loadClassDetail();
        }
    }, [user, classId, schoolYearIdParam]);

    const handleSelectSubject = (subjectTeacherId) => {
        setSelectedSubjectTeacherId(subjectTeacherId);
    };

    const handleSchoolYearChange = (e) => {
        const newSchoolYearId = e.target.value;
        // Cập nhật URL với học kỳ mới
        navigate(`/teacher/classes/${classId}?schoolYearId=${newSchoolYearId}`);
    };

    if (loading) {
        return (
            <Container className="text-center my-5">
                <Spinner animation="border" variant="primary" />
                <p className="mt-3">Đang tải dữ liệu...</p>
            </Container>
        );
    }

    return (
        <Container className="my-4">
            {error && (
                <Alert variant="danger" dismissible onClose={() => setError("")}>
                    <i className="fas fa-exclamation-circle me-2"></i> {error}
                </Alert>
            )}

            {classData && (
                <>
                    <div className="d-flex justify-content-between align-items-center mb-4">
                        <h1>
                            <i className="fas fa-chalkboard me-2"></i>
                            Lớp: {classData.className}
                        </h1>
                        <Button as={Link} to="/teacher/classes" variant="outline-primary">
                            <i className="fas fa-arrow-left me-2"></i> Quay lại
                        </Button>
                    </div>

                    <Row className="mb-4">
                        <Col md={6}>
                            <Card className="shadow-sm">
                                <Card.Header className="bg-info text-white">
                                    <h5 className="mb-0">
                                        <i className="fas fa-info-circle me-2"></i> Thông tin lớp
                                    </h5>
                                </Card.Header>
                                <Card.Body>
                                    <p><strong>Mã lớp:</strong> {classData.id}</p>
                                    <p><strong>Tên lớp:</strong> {classData.className}</p>
                                    <p><strong>Ngành:</strong> {classData.majorId ? classData.majorId.majorName : 'Chưa có thông tin'}</p>
                                    <p>
                                        <strong>Số sinh viên:</strong>{' '}
                                        <Badge bg="info">{students.length}</Badge>
                                    </p>
                                </Card.Body>
                            </Card>
                        </Col>

                        <Col md={6}>
                            <Card className="shadow-sm h-100">
                                <Card.Header className="bg-success text-white d-flex justify-content-between align-items-center">
                                    <h5 className="mb-0">
                                        <i className="fas fa-book me-2"></i> Môn học phụ trách
                                    </h5>

                                    {/* Dropdown chọn học kỳ */}
                                    {assignedSchoolYears && assignedSchoolYears.length > 0 && (
                                        <Form.Select
                                            value={schoolYearIdParam || currentSchoolYearId || ''}
                                            onChange={handleSchoolYearChange}
                                            style={{ maxWidth: '250px' }}
                                            size="sm"
                                            className="text-dark"
                                        >
                                            {assignedSchoolYears.map(year => (
                                                <option key={year.id} value={year.id}>
                                                    {year.nameYear} {year.semesterName} {year.isCurrentSemester ? "(Hiện tại)" : ""}
                                                </option>
                                            ))}
                                        </Form.Select>
                                    )}
                                </Card.Header>
                                <Card.Body style={{ overflowY: 'auto', maxHeight: '250px' }}>
                                    {subjects.length > 0 ? (
                                        <Table striped hover responsive>
                                            <thead>
                                                <tr>
                                                    <th>Môn học</th>
                                                    <th>Học kỳ</th>
                                                    <th>Hành động</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                {subjects.map(subject => (
                                                    <tr key={subject.id}>
                                                        <td>{subject.subjectId.subjectName}</td>
                                                        <td>{subject.schoolYearId.nameYear} {subject.schoolYearId.semesterName}</td>
                                                        <td>
                                                            <Button
                                                                variant="primary"
                                                                size="sm"
                                                                as={Link}
                                                                to={`/teacher/classes/${classId}/scores?subjectTeacherId=${subject.id}&schoolYearId=${subject.schoolYearId.id}`}
                                                                onClick={() => handleSelectSubject(subject.id)}
                                                            >
                                                                <i className="fas fa-pen me-1"></i> Nhập điểm
                                                            </Button>
                                                        </td>
                                                    </tr>
                                                ))}
                                            </tbody>
                                        </Table>
                                    ) : (
                                        <Alert variant="warning">
                                            <i className="fas fa-exclamation-triangle me-2"></i>
                                            Không có môn học nào được phân công cho lớp này trong học kỳ đã chọn.
                                        </Alert>
                                    )}
                                </Card.Body>
                            </Card>
                        </Col>
                    </Row>

                    {/* Phần danh sách sinh viên giữ nguyên */}
                    <Card className="shadow-sm mb-4">
                        <Card.Header className="bg-primary text-white">
                            <h5 className="mb-0">
                                <i className="fas fa-users me-2"></i> Danh sách sinh viên
                            </h5>
                        </Card.Header>
                        <Card.Body>
                            {students.length > 0 ? (
                                <Table striped hover responsive>
                                    <thead>
                                        <tr>
                                            <th>STT</th>
                                            <th>Mã sinh viên</th>
                                            <th>Họ và tên</th>
                                            <th>Email</th>
                                            <th>Giới tính</th>
                                            <th>Thao tác</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {students.map((student, index) => (
                                            <tr key={student.id}>
                                                <td>{index + 1}</td>
                                                <td>{student.studentCode}</td>
                                                <td>{student.lastName} {student.firstName}</td>
                                                <td>{student.email}</td>
                                                <td>{student.gender === 0 ? 'Nam' : 'Nữ'}</td>
                                                <td>
                                                    <div className="d-flex gap-2">
                                                        <Button
                                                            variant="info"
                                                            size="sm"
                                                            as={Link}
                                                            to={`/teacher/student/${student.studentCode}/detail`}
                                                        >
                                                            <i className="fas fa-info-circle me-1"></i> Chi tiết
                                                        </Button>
                                                        <Button
                                                            variant="primary"
                                                            size="sm"
                                                            as={Link}
                                                            to={`/teacher/student/${student.studentCode}/scores${schoolYearIdParam || currentSchoolYearId ? `?schoolYearId=${schoolYearIdParam || currentSchoolYearId}` : ''}`}
                                                        >
                                                            <i className="fas fa-chart-bar me-1"></i> Xem điểm
                                                        </Button>
                                                    </div>
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </Table>
                            ) : (
                                <Alert variant="info">
                                    <i className="fas fa-info-circle me-2"></i>
                                    Lớp học này chưa có sinh viên nào.
                                </Alert>
                            )}
                        </Card.Body>
                    </Card>
                </>
            )}
        </Container>
    );
};

export default TeacherClassDetail;