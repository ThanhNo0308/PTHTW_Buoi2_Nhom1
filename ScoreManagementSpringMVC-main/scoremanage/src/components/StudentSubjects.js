import React, { useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Table, Alert, Spinner, Form } from 'react-bootstrap';
import { MyUserContext } from "../App";
import { studentApis } from '../configs/Apis';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faBook, faExclamationTriangle, faCalendarAlt, faUser, faCreditCard } from '@fortawesome/free-solid-svg-icons';

const StudentSubjects = () => {
  const [user] = useContext(MyUserContext);
  const [subjects, setSubjects] = useState([]);
  const [schoolYears, setSchoolYears] = useState([]);
  const [currentSchoolYear, setCurrentSchoolYear] = useState(null);
  const [selectedSchoolYear, setSelectedSchoolYear] = useState(null);
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

    const loadSubjects = async () => {
      try {
        setLoading(true);
        setError(null);

        const response = await studentApis.getEnrolledSubjects(selectedSchoolYear);
        console.log("API response:", response.data);

        if (response.data) {
          setSubjects(response.data.subjects || []);
          setSchoolYears(response.data.schoolYears || []);
          setCurrentSchoolYear(response.data.schoolYear);
          
          if (!selectedSchoolYear && response.data.schoolYear) {
            setSelectedSchoolYear(response.data.schoolYear.id);
          }
        }
      } catch (err) {
        console.error("Error loading subjects:", err);
        setError("Không thể tải danh sách môn học. Vui lòng thử lại sau.");
      } finally {
        setLoading(false);
      }
    };

    loadSubjects();
  }, [user, navigate, selectedSchoolYear]);

  const handleSchoolYearChange = (e) => {
    setSelectedSchoolYear(e.target.value ? parseInt(e.target.value) : null);
  };

  // Calculate total credits
  const totalCredits = subjects.reduce((total, subject) => total + subject.credits, 0);

  if (loading) {
    return (
      <div className="d-flex justify-content-center my-5">
        <Spinner animation="border" role="status" variant="primary">
          <span className="visually-hidden">Đang tải...</span>
        </Spinner>
      </div>
    );
  }

  return (
    <div className="container mt-4">
      <h2 className="mb-4">
        <FontAwesomeIcon icon={faBook} className="me-2" />
        Môn học đã đăng ký
      </h2>
      
      {error && (
        <Alert variant="danger">
          <FontAwesomeIcon icon={faExclamationTriangle} className="me-2" />
          {error}
        </Alert>
      )}

      <Card className="mb-4 shadow">
        <Card.Header className="bg-primary text-white d-flex justify-content-between align-items-center">
          <div>
            <FontAwesomeIcon icon={faCalendarAlt} className="me-2" />
            Chọn học kỳ
          </div>
        </Card.Header>
        <Card.Body>
          <Form.Select
            value={selectedSchoolYear || ''}
            onChange={handleSchoolYearChange}
            className="mb-3"
          >
            <option value="">Chọn học kỳ</option>
            {schoolYears.map(schoolYear => (
              <option key={schoolYear.id} value={schoolYear.id}>
                {schoolYear.year} - {schoolYear.semesterName}
              </option>
            ))}
          </Form.Select>
          
          {currentSchoolYear && (
            <div className="alert alert-info">
              Đang xem môn học học kỳ: <strong>{currentSchoolYear.year} - {currentSchoolYear.semesterName}</strong>
            </div>
          )}
        </Card.Body>
      </Card>

      <Card className="shadow">
        <Card.Header className="bg-success text-white">
          <h5 className="mb-0">
            <FontAwesomeIcon icon={faBook} className="me-2" />
            Danh sách môn học
          </h5>
        </Card.Header>
        <Card.Body>
          {subjects.length > 0 ? (
            <>
              <div className="alert alert-primary mb-3">
                <FontAwesomeIcon icon={faCreditCard} className="me-2" />
                Tổng số tín chỉ đăng ký: <strong>{totalCredits}</strong>
              </div>
              
              <Table striped bordered hover responsive>
                <thead>
                  <tr className="bg-light">
                    <th width="5%">STT</th>
                    <th width="40%">Tên môn học</th>
                    <th width="10%">Số tín chỉ</th>
                    <th width="45%">Giảng viên</th>
                  </tr>
                </thead>
                <tbody>
                  {subjects.map((subject, index) => (
                    <tr key={index}>
                      <td>{index + 1}</td>
                      <td>{subject.subjectName}</td>
                      <td>{subject.credits}</td>
                      <td>
                        <FontAwesomeIcon icon={faUser} className="me-1" />
                        {subject.teacherName}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </Table>
            </>
          ) : (
            <div className="alert alert-warning">
              <FontAwesomeIcon icon={faExclamationTriangle} className="me-2" />
              Không có môn học nào được đăng ký trong học kỳ này
            </div>
          )}
        </Card.Body>
      </Card>
    </div>
  );
};

export default StudentSubjects;