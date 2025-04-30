import React, { useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Table, Alert, Spinner, Row, Col } from 'react-bootstrap';
import { MyUserContext } from "../App";
import { studentApis } from '../configs/Apis';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faUsers, faExclamationTriangle, faUserGraduate, faSchool, faUserTie, faCheckCircle, faBan } from '@fortawesome/free-solid-svg-icons';

const StudentClassInfo = () => {
  const [user] = useContext(MyUserContext);
  const [classInfo, setClassInfo] = useState(null);
  const [classmates, setClassmates] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    if (!user) {
      navigate('/login');
      return;
    }

    if (user.role !== 'Student') {
      navigate('/');
      return;
    }

    const loadClassInfo = async () => {
      try {
        setLoading(true);
        setError(null);

        const response = await studentApis.getClassInfo();
        console.log("API response:", response.data);

        if (response.data) {
          setClassInfo(response.data.class || null);
          setClassmates(response.data.classmates || []);
        }
      } catch (err) {
        console.error("Error loading class info:", err);
        setError("Không thể tải thông tin lớp. Vui lòng thử lại sau.");
      } finally {
        setLoading(false);
      }
    };

    loadClassInfo();
  }, [user, navigate]);

  if (loading) {
    return (
      <div className="d-flex justify-content-center my-5">
        <Spinner animation="border" role="status" variant="primary">
          <span className="visually-hidden">Đang tải...</span>
        </Spinner>
      </div>
    );
  }

  if (!classInfo) {
    return (
      <div className="container mt-4">
        <Alert variant="warning">
          <FontAwesomeIcon icon={faExclamationTriangle} className="me-2" />
          Bạn chưa được phân lớp. Vui lòng liên hệ giáo vụ để được hỗ trợ.
        </Alert>
      </div>
    );
  }

  return (
    <div className="container mt-4">
      <h2 className="mb-4">
        <FontAwesomeIcon icon={faSchool} className="me-2" />
        Thông tin lớp học
      </h2>
      
      {error && (
        <Alert variant="danger">
          <FontAwesomeIcon icon={faExclamationTriangle} className="me-2" />
          {error}
        </Alert>
      )}
      
      <Row className="mb-4">
        <Col lg={12}>
          <Card className="shadow">
            <Card.Header className="bg-primary text-white">
              <h5 className="mb-0">
                <FontAwesomeIcon icon={faSchool} className="me-2" />
                Chi tiết lớp học
              </h5>
            </Card.Header>
            <Card.Body>
              <Row>
                <Col md={6}>
                  <p><strong>Mã lớp:</strong> {classInfo.id}</p>
                  <p><strong>Tên lớp:</strong> {classInfo.className}</p>
                  <p>
                    <strong>Giáo viên chủ nhiệm:</strong> {classInfo.teacherId ? classInfo.teacherId.teacherName : 'Chưa phân công'}
                  </p>
                </Col>
                <Col md={6}>
                  <p>
                    <strong>Ngành học:</strong> {classInfo.majorId ? classInfo.majorId.majorName : 'Không có thông tin'}
                  </p>
                  <p>
                    <strong>Loại đào tạo:</strong> {classInfo.majorId && classInfo.majorId.trainingTypeId ? 
                      classInfo.majorId.trainingTypeId.trainingTypeName : 'Không có thông tin'}
                  </p>
                  <p><strong>Số sinh viên:</strong> {classmates.length}</p>
                </Col>
              </Row>
            </Card.Body>
          </Card>
        </Col>
      </Row>

      <Card className="shadow">
        <Card.Header className="bg-info text-white">
          <h5 className="mb-0">
            <FontAwesomeIcon icon={faUsers} className="me-2" />
            Danh sách sinh viên trong lớp
          </h5>
        </Card.Header>
        <Card.Body>
          <Table striped bordered hover responsive className="text-center">
            <thead>
              <tr className="bg-light">
                <th width="5%">STT</th>
                <th width="15%">MSSV</th>
                <th width="30%">Họ và tên</th>
                <th width="15%">Giới tính</th>
                <th width="20%">Ngày sinh</th>
                <th width="15%">Trạng thái</th>
              </tr>
            </thead>
            <tbody>
              {classmates.map((student, index) => (
                <tr key={student.id}>
                  <td>{index + 1}</td>
                  <td>{student.studentCode}</td>
                  <td className="text-start">{student.lastName} {student.firstName}</td>
                  <td>{student.gender === 0 ? 'Nam' : 'Nữ'}</td>
                  <td>
                    {student.birthdate ? 
                      new Date(student.birthdate).toLocaleDateString('vi-VN') : 
                      'Không có thông tin'}
                  </td>
                  <td>
                    {student.status === 'Active' ? (
                      <span className="badge bg-success">
                        <FontAwesomeIcon icon={faCheckCircle} className="me-1" /> Hoạt động
                      </span>
                    ) : (
                      <span className="badge bg-danger">
                        <FontAwesomeIcon icon={faBan} className="me-1" /> Không hoạt động
                      </span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        </Card.Body>
      </Card>
    </div>
  );
};

export default StudentClassInfo;