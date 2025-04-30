import React, { useState, useContext, useEffect } from 'react';
import { Container, Card, Form, Button, Table, Alert, Spinner, InputGroup, Badge } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { scoreApis } from '../configs/Apis';
import { MyUserContext } from '../App';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faSearch, faUser, faChartBar, faExclamationTriangle, faUsers } from '@fortawesome/free-solid-svg-icons';

const StudentSearch = () => {
  const [user] = useContext(MyUserContext);
  
  // State cho dữ liệu và tìm kiếm
  const [allStudents, setAllStudents] = useState([]); // Danh sách tất cả sinh viên
  const [displayedStudents, setDisplayedStudents] = useState([]); // Danh sách hiển thị
  const [searchQuery, setSearchQuery] = useState('');
  const [searchType, setSearchType] = useState('name'); // 'name', 'code', or 'class'
  const [loading, setLoading] = useState(true);
  const [searchLoading, setSearchLoading] = useState(false);
  const [error, setError] = useState('');
  const [classCount, setClassCount] = useState(0);
  
  // Tải tất cả sinh viên khi component được mount
  useEffect(() => {
    const loadAssignedStudents = async () => {
      try {
        setLoading(true);
        const response = await scoreApis.getAssignedStudents();
        
        if (response.data && response.data.success) {
          setAllStudents(response.data.students || []);
          setDisplayedStudents(response.data.students || []);
          setClassCount(response.data.classCount || 0);
        } else {
          setError('Không thể tải danh sách sinh viên');
        }
      } catch (err) {
        console.error("Error loading students:", err);
        setError(`Lỗi: ${err.response?.data?.message || err.message}`);
      } finally {
        setLoading(false);
      }
    };
    
    loadAssignedStudents();
  }, []);
  
  // Xử lý tìm kiếm
  const handleSearch = async (e) => {
    e.preventDefault();
    
    if (!searchQuery.trim()) {
      // Nếu ô tìm kiếm rỗng, hiển thị lại tất cả sinh viên
      setDisplayedStudents(allStudents);
      return;
    }
    
    try {
      setSearchLoading(true);
      setError('');
      
      // Tìm kiếm trên server
      const response = await scoreApis.searchStudents(searchQuery, searchType);
      
      if (response.data && response.data.success) {
        // Lọc kết quả tìm kiếm để chỉ hiển thị sinh viên thuộc lớp được phân công
        const searchResults = response.data.students || [];
        const assignedStudentIds = new Set(allStudents.map(student => student.id));
        
        const filteredResults = searchResults.filter(student => 
          assignedStudentIds.has(student.id)
        );
        
        setDisplayedStudents(filteredResults);
      } else {
        setError(response.data?.message || 'Có lỗi xảy ra khi tìm kiếm');
        setDisplayedStudents([]);
      }
    } catch (err) {
      console.error("Search error:", err);
      setError(`Lỗi: ${err.response?.data?.message || err.message}`);
      setDisplayedStudents([]);
    } finally {
      setSearchLoading(false);
    }
  };
  
  // Xử lý xóa tìm kiếm
  const handleClearSearch = () => {
    setSearchQuery('');
    setDisplayedStudents(allStudents);
  };
  
  return (
    <Container className="mt-4">
      <h2 className="mb-4">
        <FontAwesomeIcon icon={faUsers} className="me-2" /> 
        Danh sách sinh viên
      </h2>
      
      {error && (
        <Alert variant="danger" dismissible onClose={() => setError('')}>
          <FontAwesomeIcon icon={faExclamationTriangle} className="me-2" />
          {error}
        </Alert>
      )}
      
      <Card className="shadow-sm mb-4">
        <Card.Header className="bg-primary text-white d-flex justify-content-between align-items-center">
          <Card.Title className="mb-0">Tìm kiếm sinh viên</Card.Title>
          <Badge bg="light" text="dark">
            {classCount} lớp | {allStudents.length} sinh viên
          </Badge>
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
                  />
                  <Button variant="primary" type="submit" disabled={searchLoading}>
                    {searchLoading ? <Spinner animation="border" size="sm" /> : 'Tìm kiếm'}
                  </Button>
                  {searchQuery && (
                    <Button variant="secondary" onClick={handleClearSearch}>
                      Xóa
                    </Button>
                  )}
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
      
      <Card className="shadow-sm">
        <Card.Header className="bg-light">
          <Card.Title className="mb-0">Danh sách sinh viên {searchQuery ? `(Kết quả tìm kiếm: ${displayedStudents.length})` : ''}</Card.Title>
        </Card.Header>
        <Card.Body>
          {loading ? (
            <div className="text-center p-4">
              <Spinner animation="border" variant="primary" />
              <p className="mt-2">Đang tải danh sách sinh viên...</p>
            </div>
          ) : displayedStudents.length > 0 ? (
            <div className="table-responsive">
              <Table striped bordered hover>
                <thead className="table-dark">
                  <tr>
                    <th>STT</th>
                    <th>Mã SV</th>
                    <th>Họ và tên</th>
                    <th>Lớp</th>
                    <th>Email</th>
                    <th>Số điện thoại</th>
                    <th>Thao tác</th>
                  </tr>
                </thead>
                <tbody>
                  {displayedStudents.map((student, index) => (
                    <tr key={student.id}>
                      <td className="text-center">{index + 1}</td>
                      <td>{student.studentCode}</td>
                      <td>{student.lastName} {student.firstName}</td>
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
              <p className="text-muted">
                {searchQuery 
                  ? 'Không tìm thấy sinh viên nào phù hợp với từ khóa tìm kiếm.' 
                  : 'Không có sinh viên nào trong các lớp được phân công.'}
              </p>
            </div>
          )}
        </Card.Body>
      </Card>
    </Container>
  );
};

export default StudentSearch;