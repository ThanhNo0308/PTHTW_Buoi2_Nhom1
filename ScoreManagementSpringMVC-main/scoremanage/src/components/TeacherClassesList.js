import React, { useContext, useEffect, useState } from 'react';
import { Card, Button, Container, Row, Col, Alert, Spinner, Badge } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { MyUserContext } from '../App';
import { teacherClassApis } from '../configs/Apis';
import "../assets/css/base.css";
import "../assets/css/styles.css";

const TeacherClassesList = () => {
    const [user] = useContext(MyUserContext);
    const [classes, setClasses] = useState([]);
    const [studentCounts, setStudentCounts] = useState({});
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");

    useEffect(() => {
        const loadClasses = async () => {
            try {
                setLoading(true);
                const response = await teacherClassApis.getTeacherClasses(user.username);

                if (response.data) {
                    setClasses(response.data.classes || []);
                    setStudentCounts(response.data.studentCounts || {});
                }
            } catch (err) {
                console.error("Error loading classes:", err);
                setError("Không thể tải danh sách lớp học. " + (err.response?.data?.error || "Vui lòng thử lại sau."));
            } finally {
                setLoading(false);
            }
        };

        if (user && user.username) {
            loadClasses();
        }
    }, [user]);

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
            <h1 className="mb-4">
                <i className="fas fa-chalkboard-teacher me-2"></i>
                Danh sách lớp giảng dạy
            </h1>

            {error && (
                <Alert variant="danger" dismissible onClose={() => setError("")}>
                    <i className="fas fa-exclamation-circle me-2"></i> {error}
                </Alert>
            )}

            {success && (
                <Alert variant="success" dismissible onClose={() => setSuccess("")}>
                    <i className="fas fa-check-circle me-2"></i> {success}
                </Alert>
            )}

            {classes.length === 0 && !loading ? (
                <Alert variant="info">
                    <i className="fas fa-info-circle me-2"></i>
                    Bạn chưa được phân công lớp nào.
                </Alert>
            ) : (
                <Row xs={1} md={2} lg={3} className="g-4">
                    {classes.map((classroom) => (
                        <Col key={classroom.id}>
                            <Card className="h-100 shadow-sm">
                                <Card.Header className="bg-primary text-white">
                                    <h5 className="mb-0">{classroom.className}</h5>
                                </Card.Header>
                                <Card.Body>
                                    <Card.Text>
                                        <strong>Hệ đào tạo:</strong> {classroom.majorId?.trainingTypeId
                                            ? classroom.majorId.trainingTypeId.trainingTypeName
                                            : 'Chưa có thông tin'}
                                    </Card.Text>
                                    <Card.Text>
                                        <strong>Ngành:</strong> {classroom.majorId ? classroom.majorId.majorName : 'Chưa có thông tin'}
                                    </Card.Text>
                                    <Card.Text className="d-flex align-items-center">
                                        <strong>Thời gian:</strong>
                                        {classroom.assignedSchoolYears && classroom.assignedSchoolYears.length > 0
                                            ? (
                                                <span className="ms-2 d-inline-flex flex-nowrap overflow-auto" style={{ maxWidth: '75%' }}>
                                                    {classroom.assignedSchoolYears.map(year => (
                                                        <Badge
                                                            bg={year.isCurrentSemester ? "primary" : "secondary"}
                                                            className="me-1"
                                                            key={year.id}
                                                        >
                                                            {year.nameYear} {year.semesterName}
                                                        </Badge>
                                                    ))}
                                                </span>
                                            )
                                            : ' Chưa có thông tin'}
                                    </Card.Text>
                                    <Card.Text>
                                        <strong>Số sinh viên:</strong>{' '}
                                        <Badge bg="info">{studentCounts[classroom.id] || 0}</Badge>
                                    </Card.Text>
                                    <div className="d-grid gap-2">
                                        <Button
                                            variant="primary"
                                            as={Link}
                                            to={`/teacher/classes/${classroom.id}${classroom.assignedSchoolYears && classroom.assignedSchoolYears.length > 0 ?
                                                `?schoolYearId=${classroom.assignedSchoolYears[0].id}` : ''}`}
                                        >
                                            <i className="fas fa-info-circle me-2"></i> Chi tiết
                                        </Button>
                                    </div>
                                </Card.Body>
                            </Card>
                        </Col>
                    ))}
                </Row>
            )}
        </Container>
    );
};

export default TeacherClassesList;