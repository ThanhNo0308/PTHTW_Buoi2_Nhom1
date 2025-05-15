import React, { useState, useEffect, useContext } from 'react';
import { Container, Card, Row, Col, Table, Alert, Spinner, Button, Form, Badge, Modal, ProgressBar } from 'react-bootstrap';
import { MyUserContext } from '../App';
import { studentApis } from '../configs/Apis';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {
  faGraduationCap, faCheck, faTimes, faInfoCircle, faCalendarAlt,
  faExclamationTriangle, faPlusCircle, faMinusCircle, faFilter,
  faSearch, faCircleCheck, faCircleXmark, faClock
} from '@fortawesome/free-solid-svg-icons';
import moment from 'moment';
import 'moment/locale/vi';
import { useNavigate } from 'react-router-dom';

moment.locale('vi');

const StudentCourseRegistration = () => {
  const [user] = useContext(MyUserContext);
  const navigate = useNavigate();

  // State variables
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [student, setStudent] = useState(null);
  const [availableCourses, setAvailableCourses] = useState([]);
  const [registeredCourses, setRegisteredCourses] = useState([]);
  const [registeredCredits, setRegisteredCredits] = useState(0);
  const [remainingCredits, setRemainingCredits] = useState(17);
  const [department, setDepartment] = useState(null);
  const [major, setMajor] = useState(null);
  const [nextSemester, setNextSemester] = useState(null);
  const [currentSemester, setCurrentSemester] = useState(null);
  const [registrationPeriod, setRegistrationPeriod] = useState({});
  const [filterKeyword, setFilterKeyword] = useState('');
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const [selectedCourse, setSelectedCourse] = useState(null);
  const [actionType, setActionType] = useState(null); // 'register' or 'drop'
  const [processingAction, setProcessingAction] = useState(false);
  const [successMessage, setSuccessMessage] = useState(null);

  // Load data
  useEffect(() => {
    if (!user) {
      navigate('/login');
      return;
    }

    const loadData = async () => {
      try {
        setLoading(true);
        setError(null);

        const response = await studentApis.getAvailableCourses();
        const data = response.data;

        if (data.error) {
          setError(data.error);
          return;
        }

        setStudent(data.student);
        setMajor(data.studentMajor);
        setDepartment(data.studentDepartment);
        setNextSemester(data.nextSemester);
        setCurrentSemester(data.currentSemester);
        setAvailableCourses(data.availableCourses || []);
        setRegisteredCourses(data.registeredCourses || []);
        setRegisteredCredits(data.registeredCredits || 0);
        setRemainingCredits(data.remainingCredits || 17);
        setRegistrationPeriod(data.registrationPeriod || {});
      } catch (err) {
        console.error("Error loading data:", err);
        setError(err.response?.data?.error || "Không thể tải dữ liệu. Vui lòng thử lại sau.");
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, [user, navigate]);

  // Filter available courses
  const filteredCourses = availableCourses.filter(course => {
    if (!filterKeyword) return true;
    
    const keyword = filterKeyword.toLowerCase();
    return (
      course.subjectName.toLowerCase().includes(keyword) || 
      course.teacherName.toLowerCase().includes(keyword) || 
      course.className.toLowerCase().includes(keyword)
    );
  });

  // Handle registration
  const handleRegisterCourse = (course) => {
    if (!registrationPeriod.canRegister) {
      setError("Không trong thời gian đăng ký môn học");
      return;
    }

    if (course.credits > remainingCredits) {
      setError(`Không thể đăng ký vì vượt quá giới hạn tín chỉ. Còn lại ${remainingCredits} tín chỉ.`);
      return;
    }

    setSelectedCourse(course);
    setActionType('register');
    setShowConfirmModal(true);
  };

  // Handle drop course
  const handleDropCourse = (course) => {
    if (!registrationPeriod.canDrop) {
      setError("Không trong thời gian hủy đăng ký môn học");
      return;
    }
    
    setSelectedCourse(course);
    setActionType('drop');
    setShowConfirmModal(true);
  };

  // Process register/drop action
  const processAction = async () => {
    try {
      setProcessingAction(true);
      setError(null);
      setSuccessMessage(null);

      if (actionType === 'register') {
        const response = await studentApis.registerCourse(selectedCourse.id);
        const data = response.data;

        if (data.error) {
          setError(data.error);
          return;
        }

        setRegisteredCourses(data.registeredCourses || []);
        setRegisteredCredits(data.registeredCredits || 0);
        setRemainingCredits(data.remainingCredits || 0);
        
        // Cập nhật danh sách khóa học có sẵn
        setAvailableCourses(prevCourses => 
          prevCourses.filter(course => course.id !== selectedCourse.id)
        );
        
        setSuccessMessage(`Đăng ký môn ${selectedCourse.subjectName} thành công!`);
      } 
      else if (actionType === 'drop') {
        const response = await studentApis.dropCourse(selectedCourse.enrollmentId);
        const data = response.data;

        if (data.error) {
          setError(data.error);
          return;
        }

        setRegisteredCourses(data.registeredCourses || []);
        setRegisteredCredits(data.registeredCredits || 0);
        setRemainingCredits(data.remainingCredits || 17);
        
        // Cập nhật lại danh sách khóa học có sẵn bằng cách load lại dữ liệu
        const refreshResponse = await studentApis.getAvailableCourses();
        setAvailableCourses(refreshResponse.data.availableCourses || []);
        
        setSuccessMessage(`Hủy đăng ký môn ${selectedCourse.subjectName} thành công!`);
      }
    } catch (err) {
      console.error("Error processing action:", err);
      setError(err.response?.data?.error || "Đã xảy ra lỗi. Vui lòng thử lại.");
    } finally {
      setProcessingAction(false);
      setShowConfirmModal(false);
    }
  };

  // Format date display
  const formatDate = (date) => {
    if (!date) return '';
    return moment(date).format('DD/MM/YYYY');
  };

  // Registration period display message
  const getRegistrationMessage = () => {
    if (!registrationPeriod) return '';
    
    const startDate = formatDate(registrationPeriod.registrationStart);
    const endDate = formatDate(registrationPeriod.registrationEnd);
    const dropEndDate = formatDate(registrationPeriod.dropEnd);
    
    return (
      <div>
        <p>Thời gian đăng ký: <strong>{startDate}</strong> đến <strong>{endDate}</strong></p>
        <p>Thời gian hủy đăng ký: <strong>{startDate}</strong> đến <strong>{dropEndDate}</strong></p>
        <p className="text-info">
          <FontAwesomeIcon icon={faClock} className="me-2" />
          {registrationPeriod.message}
        </p>
      </div>
    );
  };

  if (loading) {
    return (
      <Container className="mt-4 text-center">
        <Spinner animation="border" variant="primary" />
        <p className="mt-2">Đang tải dữ liệu đăng ký học...</p>
      </Container>
    );
  }

  return (
    <Container className="mt-4 mb-5">
      <h2 className="mb-4">
        <FontAwesomeIcon icon={faGraduationCap} className="me-2" />
        Đăng Ký Học Phần
      </h2>

      {error && (
        <Alert variant="danger" dismissible onClose={() => setError(null)}>
          <FontAwesomeIcon icon={faExclamationTriangle} className="me-2" />
          {error}
        </Alert>
      )}

      {successMessage && (
        <Alert variant="success" dismissible onClose={() => setSuccessMessage(null)}>
          <FontAwesomeIcon icon={faCheck} className="me-2" />
          {successMessage}
        </Alert>
      )}

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
              <p><strong>Khoa:</strong> {department?.departmentName || '-'}</p>
              <p><strong>Ngành:</strong> {major?.majorName || '-'}</p>
            </Card.Body>
          </Card>
        </Col>

        <Col md={8}>
          <Card className="shadow-sm">
            <Card.Header className="bg-info text-white">
              <Card.Title className="mb-0 d-flex justify-content-between align-items-center">
                <span>
                  <FontAwesomeIcon icon={faCalendarAlt} className="me-2" />
                  Thông tin đăng ký học phần
                </span>
              </Card.Title>
            </Card.Header>
            <Card.Body>
              {currentSemester && (
                <Alert variant="secondary">
                  <FontAwesomeIcon icon={faInfoCircle} className="me-2" />
                  Học kỳ hiện tại: <strong>{currentSemester.nameYear} {currentSemester.semesterName}</strong>
                </Alert>
              )}
              
              <h5>Đăng ký cho học kỳ: <Badge bg="primary">{nextSemester?.nameYear} {nextSemester?.semesterName}</Badge></h5>
              
              <Alert variant="info" className="mt-3">
                <FontAwesomeIcon icon={faInfoCircle} className="me-2" />
                {getRegistrationMessage()}
              </Alert>
              
              <div className="mt-3">
                <p className="mb-2">Tiến độ đăng ký tín chỉ:</p>
                <div className="d-flex justify-content-between align-items-center">
                  <div className="flex-grow-1 me-3">
                    <ProgressBar 
                      now={(registeredCredits / 17) * 100} 
                      label={`${registeredCredits}/17 tín chỉ`}
                      variant={registeredCredits > 17 ? "danger" : "success"} 
                    />
                  </div>
                  <div>
                    <Badge bg={remainingCredits < 0 ? "danger" : "success"} style={{fontSize: "1rem"}}>
                      Còn lại: {remainingCredits} tín chỉ
                    </Badge>
                  </div>
                </div>
              </div>
            </Card.Body>
          </Card>
        </Col>
      </Row>

      <Card className="shadow-sm mb-4">
        <Card.Header className="bg-success text-white">
          <Card.Title className="mb-0">
            <FontAwesomeIcon icon={faCheck} className="me-2" />
            Môn học đã đăng ký ({registeredCourses.length})
          </Card.Title>
        </Card.Header>
        <Card.Body>
          {registeredCourses.length === 0 ? (
            <Alert variant="info">
              <FontAwesomeIcon icon={faInfoCircle} className="me-2" />
              Bạn chưa đăng ký môn học nào cho học kỳ này.
            </Alert>
          ) : (
            <div className="table-responsive">
              <Table hover bordered>
                <thead className="table-light">
                  <tr>
                    <th style={{width: '50px'}}>STT</th>
                    <th>Mã MH</th>
                    <th>Tên môn học</th>
                    <th style={{width: '80px'}}>Tín chỉ</th>
                    <th>Giảng viên</th>
                    <th>Lớp học</th>
                    <th style={{width: '120px'}}>Thao tác</th>
                  </tr>
                </thead>
                <tbody>
                  {registeredCourses.map((course, index) => (
                    <tr key={course.enrollmentId}>
                      <td className="text-center">{index + 1}</td>
                      <td>{course.subjectId}</td>
                      <td>{course.subjectName}</td>
                      <td className="text-center">{course.credits}</td>
                      <td>{course.teacherName}</td>
                      <td>{course.className}</td>
                      <td className="text-center">
                        <Button 
                          variant="danger" 
                          size="sm"
                          disabled={!registrationPeriod.canDrop}
                          onClick={() => handleDropCourse(course)}
                        >
                          <FontAwesomeIcon icon={faMinusCircle} className="me-1" />
                          Hủy đăng ký
                        </Button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </Table>
            </div>
          )}
        </Card.Body>
      </Card>

      <Card className="shadow-sm">
        <Card.Header className="bg-primary text-white">
          <div className="d-flex justify-content-between align-items-center">
            <Card.Title className="mb-0">
              <FontAwesomeIcon icon={faGraduationCap} className="me-2" />
              Danh sách môn học có thể đăng ký ({filteredCourses.length})
            </Card.Title>
            <div style={{width: '300px'}}>
              <Form.Control
                type="search"
                placeholder="Tìm kiếm môn học..."
                value={filterKeyword}
                onChange={(e) => setFilterKeyword(e.target.value)}
                className="form-control-sm"
                style={{background: 'white', border: '1px solid white'}}
              />
            </div>
          </div>
        </Card.Header>
        <Card.Body>
          {availableCourses.length === 0 ? (
            <Alert variant="info">
              <FontAwesomeIcon icon={faInfoCircle} className="me-2" />
              Không có môn học nào phù hợp với khoa của bạn hoặc bạn đã đăng ký đầy đủ các môn học.
            </Alert>
          ) : filteredCourses.length === 0 ? (
            <Alert variant="info">
              <FontAwesomeIcon icon={faInfoCircle} className="me-2" />
              Không tìm thấy môn học nào phù hợp với từ khóa tìm kiếm.
            </Alert>
          ) : (
            <div className="table-responsive">
              <Table hover bordered>
                <thead className="table-light">
                  <tr>
                    <th style={{width: '50px'}}>STT</th>
                    <th>Mã MH</th>
                    <th>Tên môn học</th>
                    <th style={{width: '80px'}}>Tín chỉ</th>
                    <th>Giảng viên</th>
                    <th>Lớp học</th>
                    <th style={{width: '120px'}}>Thao tác</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredCourses.map((course, index) => (
                    <tr key={course.id}>
                      <td className="text-center">{index + 1}</td>
                      <td>{course.subjectId}</td>
                      <td>{course.subjectName}</td>
                      <td className="text-center">
                        {course.credits}
                        {course.credits > remainingCredits && (
                          <FontAwesomeIcon 
                            icon={faExclamationTriangle} 
                            className="ms-1 text-warning" 
                            title="Vượt quá số tín chỉ còn lại" 
                          />
                        )}
                      </td>
                      <td>{course.teacherName}</td>
                      <td>{course.className}</td>
                      <td className="text-center">
                        <Button 
                          variant="primary" 
                          size="sm"
                          disabled={!registrationPeriod.canRegister || course.credits > remainingCredits}
                          onClick={() => handleRegisterCourse(course)}
                          title={
                            !registrationPeriod.canRegister 
                              ? "Ngoài thời gian đăng ký" 
                              : course.credits > remainingCredits 
                                ? "Vượt quá số tín chỉ cho phép" 
                                : "Đăng ký môn học"
                          }
                        >
                          <FontAwesomeIcon icon={faPlusCircle} className="me-1" />
                          Đăng ký
                        </Button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </Table>
            </div>
          )}
          
          <Alert variant="secondary" className="mt-4">
            <FontAwesomeIcon icon={faInfoCircle} className="me-2" />
            <strong>Lưu ý:</strong> 
            <ul className="mb-0 mt-2">
              <li>Chỉ hiển thị các môn học thuộc khoa của bạn cho học kỳ tiếp theo.</li>
              <li>Mỗi học kỳ chỉ được đăng ký tối đa 17 tín chỉ.</li>
              <li>Thời gian đăng ký: từ 1 tháng đến 1 tuần trước khi học kỳ bắt đầu.</li>
              <li>Thời gian hủy đăng ký: từ khi bắt đầu đăng ký đến trước ngày học kỳ bắt đầu.</li>
            </ul>
          </Alert>
        </Card.Body>
      </Card>

      {/* Confirmation Modal */}
      <Modal show={showConfirmModal} onHide={() => setShowConfirmModal(false)}>
        <Modal.Header closeButton className={actionType === 'register' ? 'bg-primary text-white' : 'bg-danger text-white'}>
          <Modal.Title>
            {actionType === 'register' ? (
              <><FontAwesomeIcon icon={faCircleCheck} className="me-2" />Xác nhận đăng ký môn học</>
            ) : (
              <><FontAwesomeIcon icon={faCircleXmark} className="me-2" />Xác nhận hủy đăng ký</>
            )}
          </Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {selectedCourse && (
            <>
              <p>Bạn có chắc chắn muốn {actionType === 'register' ? 'đăng ký' : 'hủy đăng ký'} môn học:</p>
              <Card className="mb-3">
                <Card.Body className="py-2">
                  <h5>{selectedCourse.subjectName}</h5>
                  <p className="mb-1"><strong>Mã môn học:</strong> {selectedCourse.subjectId}</p>
                  <p className="mb-1"><strong>Giảng viên:</strong> {selectedCourse.teacherName}</p>
                  <p className="mb-1"><strong>Số tín chỉ:</strong> {selectedCourse.credits}</p>
                  <p className="mb-0"><strong>Lớp:</strong> {selectedCourse.className}</p>
                </Card.Body>
              </Card>
              
              {actionType === 'register' && (
                <Alert variant="info">
                  <FontAwesomeIcon icon={faInfoCircle} className="me-2" />
                  Sau khi đăng ký, bạn vẫn có thể hủy đăng ký trước ngày {formatDate(registrationPeriod.dropEnd)}.
                </Alert>
              )}
              
              {actionType === 'drop' && (
                <Alert variant="warning">
                  <FontAwesomeIcon icon={faExclamationTriangle} className="me-2" />
                  Lưu ý: Sau khi hủy đăng ký, bạn có thể không đăng ký lại được nếu lớp đã đầy.
                </Alert>
              )}
            </>
          )}
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShowConfirmModal(false)} disabled={processingAction}>
            <FontAwesomeIcon icon={faTimes} className="me-1" />
            Hủy
          </Button>
          <Button 
            variant={actionType === 'register' ? "primary" : "danger"} 
            onClick={processAction}
            disabled={processingAction}
          >
            {processingAction ? (
              <>
                <Spinner as="span" animation="border" size="sm" className="me-2" />
                Đang xử lý...
              </>
            ) : actionType === 'register' ? (
              <>
                <FontAwesomeIcon icon={faPlusCircle} className="me-2" />
                Đăng ký
              </>
            ) : (
              <>
                <FontAwesomeIcon icon={faMinusCircle} className="me-2" />
                Hủy đăng ký
              </>
            )}
          </Button>
        </Modal.Footer>
      </Modal>
    </Container>
  );
};

export default StudentCourseRegistration;