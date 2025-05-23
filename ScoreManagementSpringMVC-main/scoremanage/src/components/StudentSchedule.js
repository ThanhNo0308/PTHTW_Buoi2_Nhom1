import React, { useState, useEffect, useContext } from 'react';
import { Container, Table, Card, Row, Col, Form, Alert, Spinner, Badge } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faCalendarAlt, faInfoCircle, faBook, faClock, faChalkboardTeacher, faDoorOpen } from '@fortawesome/free-solid-svg-icons';
import { scheduleApis, API, endpoints } from '../configs/Apis';
import { MyUserContext } from "../App";
import { useNavigate } from 'react-router-dom';

const StudentSchedule = () => {
    const [user] = useContext(MyUserContext);
    const navigate = useNavigate();

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [schedule, setSchedule] = useState([]);
    const [student, setStudent] = useState(null);
    const [schoolYears, setSchoolYears] = useState([]);
    const [selectedSchoolYear, setSelectedSchoolYear] = useState(null);
    const [currentSchoolYear, setCurrentSchoolYear] = useState(null);

    const LookupData = {
        getSchoolYears: async () => {
            try {
                // Thay đổi từ scores-available-school-years sang school-years endpoint
                const response = await API.get(endpoints["school-years"]);
                if (response.data && response.data.success && response.data.schoolYears) {
                    return response.data.schoolYears;
                } else {
                    console.error("Invalid school years data format", response.data);
                    return [];
                }
            } catch (error) {
                console.error("Failed to fetch school years", error);
                return [];
            }
        },
    };

    // Lấy dữ liệu lịch học
    useEffect(() => {
        const fetchData = async () => {
            if (!user) {
                navigate("/login");
                return;
            }

            try {
                setLoading(true);
                setError(null);

                // Lấy danh sách học kỳ
                const schoolYearsRes = await LookupData.getSchoolYears();
                if (schoolYearsRes && schoolYearsRes.length > 0) {
                    setSchoolYears(schoolYearsRes);

                    // Tìm học kỳ hiện tại (đã có flag isCurrent từ API)
                    const currentSY = schoolYearsRes.find(sy => sy.isCurrent === true);
                    if (currentSY) {
                        setCurrentSchoolYear(currentSY);
                        setSelectedSchoolYear(currentSY.id);
                    } else {
                        // Nếu không có học kỳ nào được đánh dấu là hiện tại, sử dụng học kỳ đầu tiên
                        setSelectedSchoolYear(schoolYearsRes[0].id);
                    }
                }

                // Lấy lịch học
                await loadSchedule(selectedSchoolYear);
            } catch (err) {
                console.error("Error loading schedule:", err);
                setError(err.response?.data?.error || "Không thể tải lịch học. Vui lòng thử lại sau.");
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [user, navigate]);

    // Load lịch học khi thay đổi học kỳ
    const loadSchedule = async (schoolYearId) => {
        try {
            setLoading(true);
            setError(null);

            const response = await scheduleApis.getStudentSchedule(schoolYearId);

            if (response.data.success) {
                // Nhóm các buổi học theo ngày trong tuần
                const sortedSessions = groupSessionsByDayOfWeek(response.data.classSessions);
                setSchedule(sortedSessions);
                setStudent(response.data.student);
            } else {
                setError(response.data.error || "Không thể tải lịch học");
            }
        } catch (err) {
            console.error("Error loading schedule:", err);
            setError(err.response?.data?.error || "Không thể tải lịch học. Vui lòng thử lại sau.");
        } finally {
            setLoading(false);
        }
    };

    // Xử lý khi thay đổi học kỳ
    const handleSchoolYearChange = (e) => {
        const schoolYearId = e.target.value;
        setSelectedSchoolYear(schoolYearId);
        loadSchedule(schoolYearId);
    };

    // Nhóm các buổi học theo ngày trong tuần
    const groupSessionsByDayOfWeek = (sessions) => {
        const grouped = {};

        // Khởi tạo các ngày trong tuần (1-7)
        for (let i = 1; i <= 7; i++) {
            grouped[i] = [];
        }

        // Nhóm các buổi học
        sessions.forEach(session => {
            if (grouped[session.dayOfWeek]) {
                grouped[session.dayOfWeek].push(session);
            }
        });

        return grouped;
    };

    const formatDate = (dateString) => {
        if (!dateString) return "Chưa cập nhật";

        try {
            const date = new Date(dateString);
            if (isNaN(date.getTime())) return "Định dạng không hợp lệ";

            return `${date.getDate().toString().padStart(2, '0')}/${(date.getMonth() + 1).toString().padStart(2, '0')}/${date.getFullYear()}`;
        } catch (error) {
            console.error("Lỗi định dạng ngày:", error);
            return "Chưa cập nhật";
        }
    };

    return (
        <Container className="mt-4 mb-5">
            <h2 className="mb-4">
                <FontAwesomeIcon icon={faCalendarAlt} className="me-2" />
                Lịch Học
            </h2>

            {error && (
                <Alert variant="danger">
                    <FontAwesomeIcon icon={faInfoCircle} className="me-2" />
                    {error}
                </Alert>
            )}

            <Card className="shadow-sm mb-4">
                <Card.Header className="bg-primary text-white">
                    <div className="d-flex justify-content-between align-items-center">
                        <Card.Title className="mb-0">Thông tin lịch học</Card.Title>
                        <Form.Select
                            style={{ width: '300px' }}
                            value={selectedSchoolYear || ''}
                            onChange={handleSchoolYearChange}
                            disabled={loading}
                        >
                            {schoolYears.map(sy => (
                                <option key={sy.id} value={sy.id}>
                                    {sy.nameYear} {sy.semesterName} {sy.isCurrent ? "(Hiện tại)" : ""}
                                </option>
                            ))}
                        </Form.Select>
                    </div>
                </Card.Header>
                <Card.Body>
                    {loading ? (
                        <div className="text-center py-4">
                            <Spinner animation="border" variant="primary" />
                            <p className="mt-2">Đang tải lịch học...</p>
                        </div>
                    ) : (
                        <>
                            {student && (
                                <Row className="mb-4">
                                    <Col md={6}>
                                        <Card className="shadow-sm">
                                            <Card.Body>
                                                <h5>Thông tin sinh viên</h5>
                                                <p><strong>Mã sinh viên:</strong> {student.studentCode}</p>
                                                <p><strong>Họ và tên:</strong> {student.fullName}</p>
                                                <p><strong>Lớp:</strong> {student.className}</p>
                                            </Card.Body>
                                        </Card>
                                    </Col>
                                    <Col md={6}>
                                        <Card className="shadow-sm">
                                            <Card.Body>
                                                <h5>Học kỳ</h5>
                                                {currentSchoolYear ? (
                                                    <>
                                                        <p><strong>Học kỳ hiện tại:</strong> {currentSchoolYear.nameYear} {currentSchoolYear.semesterName}</p>
                                                        <p><strong>Học kỳ đang xem:</strong> {
                                                            schoolYears.find(sy => sy.id === parseInt(selectedSchoolYear))?.nameYear
                                                        } {
                                                                schoolYears.find(sy => sy.id === parseInt(selectedSchoolYear))?.semesterName
                                                            }</p>
                                                        {(() => {
                                                            const selectedYear = schoolYears.find(sy => sy.id === parseInt(selectedSchoolYear));

                                                            // Sử dụng cả hai tên trường có thể có để đảm bảo tương thích
                                                            if (selectedYear) {
                                                                const startDate = selectedYear.startDate || selectedYear.yearStart;
                                                                const endDate = selectedYear.endDate || selectedYear.yearEnd;

                                                                if (startDate || endDate) {
                                                                    return (
                                                                        <p><strong>Thời gian học kỳ:</strong> {formatDate(startDate)} - {formatDate(endDate)}</p>
                                                                    );
                                                                }
                                                            }
                                                            return null;
                                                        })()}
                                                    </>
                                                ) : (
                                                    <p>Không có thông tin học kỳ</p>
                                                )}
                                            </Card.Body>
                                        </Card>
                                    </Col>
                                </Row>
                            )}

                            <div className="table-responsive">
                                <Table bordered hover className="schedule-table">
                                    <thead className="table-light">
                                        <tr>
                                            <th style={{ width: '15%' }}>Thời gian</th>
                                            <th>Thứ 2</th>
                                            <th>Thứ 3</th>
                                            <th>Thứ 4</th>
                                            <th>Thứ 5</th>
                                            <th>Thứ 6</th>
                                            <th>Thứ 7</th>
                                            <th>Chủ nhật</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <tr>
                                            <td className="text-center align-middle">
                                                <strong>Sáng</strong><br />
                                                <small>(7:30 - 11:45)</small>
                                            </td>
                                            {[1, 2, 3, 4, 5, 6, 7].map(day => (
                                                <td key={day} className="align-top p-1">
                                                    {schedule[day]?.filter(s => {
                                                        const startHour = parseInt(s.startTime.split(':')[0]);
                                                        return startHour >= 7 && startHour < 12;
                                                    }).map((session, index) => (
                                                        <ScheduleItem key={index} session={session} />
                                                    ))}
                                                </td>
                                            ))}
                                        </tr>
                                        <tr>
                                            <td className="text-center align-middle">
                                                <strong>Chiều</strong><br />
                                                <small>(13:00 - 17:15)</small>
                                            </td>
                                            {[1, 2, 3, 4, 5, 6, 7].map(day => (
                                                <td key={day} className="align-top p-1">
                                                    {schedule[day]?.filter(s => {
                                                        const startHour = parseInt(s.startTime.split(':')[0]);
                                                        return startHour >= 12 && startHour < 18;
                                                    }).map((session, index) => (
                                                        <ScheduleItem key={index} session={session} />
                                                    ))}
                                                </td>
                                            ))}
                                        </tr>
                                        <tr>
                                            <td className="text-center align-middle">
                                                <strong>Tối</strong><br />
                                                <small>(18:00 - 21:30)</small>
                                            </td>
                                            {[1, 2, 3, 4, 5, 6, 7].map(day => (
                                                <td key={day} className="align-top p-1">
                                                    {schedule[day]?.filter(s => {
                                                        const startHour = parseInt(s.startTime.split(':')[0]);
                                                        return startHour >= 18;
                                                    }).map((session, index) => (
                                                        <ScheduleItem key={index} session={session} />
                                                    ))}
                                                </td>
                                            ))}
                                        </tr>
                                    </tbody>
                                </Table>
                            </div>

                            <Alert variant="info" className="mt-3">
                                <FontAwesomeIcon icon={faInfoCircle} className="me-2" />
                                <strong>Lưu ý:</strong> Lịch học có thể thay đổi. Vui lòng kiểm tra thường xuyên.
                            </Alert>
                        </>
                    )}
                </Card.Body>
            </Card>
        </Container>
    );
};

// Component hiển thị một buổi học
const ScheduleItem = ({ session }) => {
    return (
        <div className="schedule-item mb-2 p-2" style={{
            backgroundColor: '#f8f9fa',
            borderLeft: '3px solid #007bff',
            borderRadius: '4px'
        }}>
            <div className="fw-bold text-primary">{session.subject.subjectName}</div>
            <div>
                <FontAwesomeIcon icon={faClock} className="me-1 text-secondary" />
                {session.startTime} - {session.endTime}
            </div>
            <div>
                <FontAwesomeIcon icon={faChalkboardTeacher} className="me-1 text-secondary" />
                {session.subject.teacherName || "Chưa có GV"}
            </div>
            <div>
                <FontAwesomeIcon icon={faDoorOpen} className="me-1 text-secondary" />
                Phòng: {session.roomId}
            </div>
            {session.notes && (
                <div className="mt-1">
                    <Badge bg="warning" text="dark" className="small">
                        {session.notes}
                    </Badge>
                </div>
            )}
        </div>
    );
};

export default StudentSchedule;