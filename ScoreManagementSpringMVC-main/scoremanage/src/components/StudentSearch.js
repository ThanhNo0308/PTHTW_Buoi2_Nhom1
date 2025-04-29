import React, { useState, useContext } from 'react';
import { Container, Card, Form, Button, Table, Alert, Spinner, InputGroup } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { scoreApis } from '../configs/Apis';
import { MyUserContext } from '../App';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faSearch, faUser, faChartBar, faExclamationTriangle } from '@fortawesome/free-solid-svg-icons';

const StudentSearch = () => {
  const [user] = useContext(MyUserContext);
  
  // State cho form tìm kiếm
  const [searchQuery, setSearchQuery] = useState('');
  const [searchType, setSearchType] = useState('name'); // 'name', 'code', or 'class'
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [students, setStudents] = useState([]);
  const [hasSearched, setHasSearched] = useState(false);
  
  // Xử lý tìm kiếm
  const handleSearch = async (e) => {
    e.preventDefault();
    
    if (!searchQuery.trim()) {
      setError('Vui lòng nhập từ khóa tìm kiếm');
      return;
    }
    
    try {
      setLoading(true);
      setError('');
      setHasSearched(true);
      
      const response = await scoreApis.searchStudents(searchQuery, searchType);
      
      if (response.data && response.data.success) {
        setStudents(response.data.students || []);
      } else {
        setError(response.data?.message || 'Có lỗi xảy ra khi tìm kiếm');
        setStudents([]);
      }
    } catch (err) {
      console.error("Search error:", err);
      setError(`Lỗi: ${err.response?.data?.message || err.message}`);
      setStudents([]);
    } finally {
      setLoading(false);
    }
  };
  
  return (
    <Container className="mt-4">
      <h2 className="mb-4">
        <FontAwesomeIcon icon={faSearch} className="me-2" /> 
        Tìm kiếm Sinh viên
      </h2>
      
      {error && (
        <Alert variant="danger" dismissible onClose={() => setError('')}>
          <FontAwesomeIcon icon={faExclamationTriangle} className="me-2" />
          {error}
        </Alert>
      )}
      
      <Card className="shadow-sm mb-4">
        <Card.Header className="bg-primary text-white">
          <Card.Title className="mb-0">Tìm kiếm</Card.Title>
        </Card.Header>
        <Card.Body>
          <Form onSubmit={handleSearch}>
            <div className="row">
              <div className="col-md-9">
                <InputGroup className="mb-3">
                  <InputGroup.Text>
                    <FontAwesomeIcon icon={faSearch} />
                  </InputGroup.Text>
                  <Form.Control
                    type="text"
                    placeholder="Nhập từ khóa tìm kiếm..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    required
                  />
                  <Button variant="primary" type="submit" disabled={loading}>
                    {loading ? <Spinner animation="border" size="sm" /> : 'Tìm kiếm'}
                  </Button>
                </InputGroup>
              </div>
              <div className="col-md-3">
                <Form.Group className="mb-3">
                  <Form.Select 
                    value={searchType}
                    onChange={(e) => setSearchType(e.target.value)}
                  >
                    <option value="name">Tìm theo họ tên</option>
                    <option value="code">Tìm theo mã SV</option>
                    <option value="class">Tìm theo lớp</option>
                  </Form.Select>
                </Form.Group>
              </div>
            </div>
          </Form>
        </Card.Body>
      </Card>
      
      {hasSearched && (
        <Card className="shadow-sm">
          <Card.Header className="bg-light">
            <Card.Title className="mb-0">Kết quả tìm kiếm</Card.Title>
          </Card.Header>
          <Card.Body>
            {loading ? (
              <div className="text-center p-4">
                <Spinner animation="border" variant="primary" />
                <p className="mt-2">Đang tìm kiếm...</p>
              </div>
            ) : students.length > 0 ? (
              <div className="table-responsive">
                <Table striped bordered hover>
                  <thead className="table-dark">
                    <tr>
                      <th>Mã SV</th>
                      <th>Họ và tên</th>
                      <th>Lớp</th>
                      <th>Email</th>
                      <th>Số điện thoại</th>
                      <th>Thao tác</th>
                    </tr>
                  </thead>
                  <tbody>
                    {students.map((student) => (
                      <tr key={student.id}>
                        <td>{student.studentCode}</td>
                        <td>{student.firstName} {student.lastName}</td>
                        <td>{student.classId.className}</td>
                        <td>{student.email}</td>
                        <td>{student.phone || '-'}</td>
                        <td>
                          <div className="btn-group">
                            <Link 
                              to={`/teacher/student/${student.studentCode}/detail`} 
                              className="btn btn-sm btn-info"
                            >
                              <FontAwesomeIcon icon={faUser} className="me-1" /> Chi tiết
                            </Link>
                            <Link 
                              to={`/teacher/student/${student.studentCode}/scores`} 
                              className="btn btn-sm btn-primary"
                            >
                              <FontAwesomeIcon icon={faChartBar} className="me-1" /> Xem điểm
                            </Link>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </Table>
              </div>
            ) : (
              <div className="text-center p-4">
                <p className="text-muted">Không tìm thấy sinh viên nào phù hợp với từ khóa tìm kiếm.</p>
              </div>
            )}
          </Card.Body>
        </Card>
      )}
    </Container>
  );
};

export default StudentSearch;