import React, { useState, useEffect, useContext } from 'react';
import { Container, Card, Form, Button, Alert, Spinner, Table } from 'react-bootstrap';
import { scoreApis } from '../configs/Apis';
import { MyUserContext } from '../App';
import { useNavigate } from 'react-router-dom';
import cookie from 'react-cookies';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faDownload, faUpload, faInfoCircle, faCheck, faExclamationTriangle, faSignInAlt } from '@fortawesome/free-solid-svg-icons';

const ScoreImport = () => {
  const [user] = useContext(MyUserContext);
  const navigate = useNavigate();
  // State cho form và dữ liệu
  const [loading, setLoading] = useState(true);
  const [subjectTeachers, setSubjectTeachers] = useState([]);
  const [classes, setClasses] = useState([]);
  const [schoolYears, setSchoolYears] = useState([]);
  const [selectedSubjectTeacher, setSelectedSubjectTeacher] = useState('');
  const [selectedClass, setSelectedClass] = useState('');
  const [selectedSchoolYear, setSelectedSchoolYear] = useState('');
  const [file, setFile] = useState(null);
  const [fileName, setFileName] = useState('');
  const [csvPreview, setCsvPreview] = useState([]);
  const [showCsvPreview, setShowCsvPreview] = useState(false);
  const [allClasses, setAllClasses] = useState([]);
  const [filteredClasses, setFilteredClasses] = useState([]);
  const [filteredSchoolYears, setFilteredSchoolYears] = useState([]);
  // State cho thông báo
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [missingScoreTypes, setMissingScoreTypes] = useState([]);

  // Tải dữ liệu ban đầu
  useEffect(() => {
    if (!user || !user.token) {
      setError('Bạn cần đăng nhập để sử dụng tính năng này');
      return;
    }

    const loadFormData = async () => {
      try {
        setLoading(true);
        const response = await scoreApis.getImportScoresFormData();

        if (response.data && response.data.success) {
          // Thay đổi: Nhóm các phân công theo môn học thay vì lọc
          const subjectTeachersMap = {};

          response.data.subjectTeachers.forEach(subject => {
            const subjectId = subject.subjectId.id;
            const subjectName = subject.subjectId.subjectName;
            const subjectCode = subject.subjectId.subjectCode;

            // Tạo key duy nhất cho mỗi môn học
            const key = `${subjectId}-${subjectCode}-${subjectName}`;

            if (!subjectTeachersMap[key]) {
              subjectTeachersMap[key] = {
                id: subjectId,
                subjectId: subject.subjectId,
                // Lưu tất cả các assignments liên quan đến môn học này
                assignments: []
              };
            }

            // Thêm assignment này vào danh sách
            subjectTeachersMap[key].assignments.push(subject);
          });

          // Chuyển đổi map thành array
          const uniqueSubjects = Object.values(subjectTeachersMap);

          console.log("Grouped subjects with assignments:", uniqueSubjects);
          setSubjectTeachers(uniqueSubjects);

          setAllClasses(response.data.classes || []);
          setFilteredClasses(response.data.classes || []);
          setSchoolYears(response.data.schoolYears || []);
          const assignedYears = response.data.schoolYears.filter(year =>
            year.assignedToTeacher === true || year.isCurrent === true
          );
          setFilteredSchoolYears(assignedYears);
          // Mặc định là năm học hiện tại
          const currentYear = response.data.schoolYears.find(year => year.isCurrent);
          if (currentYear) {
            setSelectedSchoolYear(currentYear.id);
          }
        } else {
          setError('Không thể tải dữ liệu form');
        }
      } catch (err) {
        setError(`Lỗi: ${err.response?.data?.message || err.message}`);
      } finally {
        setLoading(false);
      }
    };

    if (user) {
      loadFormData();
    }
  }, [user]);

  // Thêm useEffect để lọc lớp khi chọn môn
  useEffect(() => {
    const fetchClassesBySubject = async () => {
      if (!selectedSubjectTeacher) {
        // Nếu chưa chọn môn, hiển thị tất cả lớp
        setFilteredClasses(allClasses);
        return;
      }

      try {
        setLoading(true);

        // Gọi API mới để lấy danh sách lớp theo môn học
        const response = await scoreApis.getClassesBySubject(selectedSubjectTeacher);

        if (response.data && response.data.success) {
          console.log("Classes by subject:", response.data.classes);

          // Kiểm tra xem có dữ liệu trả về không
          if (response.data.classes && response.data.classes.length > 0) {
            setFilteredClasses(response.data.classes);

            // Reset selected class nếu đã chọn nhưng không còn trong danh sách mới
            const classExists = response.data.classes.some(c => c.id.toString() === selectedClass);
            if (selectedClass && !classExists) {
              setSelectedClass('');
            }
          } else {
            // Nếu không có lớp nào, hiển thị thông báo
            setFilteredClasses([]);
            setError("Không tìm thấy lớp nào cho môn học này");
          }
        } else {
          setFilteredClasses(allClasses);
        }
      } catch (error) {
        console.error("Error fetching classes by subject:", error);
        setFilteredClasses(allClasses);
        setError(`Lỗi khi lấy danh sách lớp: ${error.message}`);
      } finally {
        setLoading(false);
      }
    };

    fetchClassesBySubject();
  }, [selectedSubjectTeacher, allClasses]);

  // Thêm useEffect để lọc học kỳ khi chọn môn và lớp
  useEffect(() => {
    if (selectedSubjectTeacher && selectedClass) {
      // Tải danh sách học kỳ khi chọn môn học và lớp
      const fetchSchoolYears = async () => {
        try {
          setLoading(true);
          const response = await scoreApis.getAvailableSchoolYears(
            parseInt(selectedSubjectTeacher),
            parseInt(selectedClass)
          );

          if (response.data && response.data.success) {
            setFilteredSchoolYears(response.data.schoolYears || []);

            // Nếu có năm học, chọn năm đầu tiên
            if (response.data.schoolYears && response.data.schoolYears.length > 0) {
              setSelectedSchoolYear(response.data.schoolYears[0].id.toString());
            } else {
              setSelectedSchoolYear('');
            }
          } else {
            setFilteredSchoolYears([]);
            setSelectedSchoolYear('');
          }
        } catch (error) {
          console.error("Error fetching school years:", error);
          setFilteredSchoolYears([]);
          setSelectedSchoolYear('');
        } finally {
          setLoading(false);
        }
      };

      fetchSchoolYears();
    } else {
      setFilteredSchoolYears([]);
      setSelectedSchoolYear('');
    }
  }, [selectedSubjectTeacher, selectedClass]);

  // Tải file mẫu
  const handleDownloadTemplate = async () => {
    try {
      const response = await scoreApis.getScoreTemplate();

      if (response.data && response.data.success) {
        // Chuyển Base64 thành Blob
        const byteCharacters = atob(response.data.fileContent);
        const byteNumbers = new Array(byteCharacters.length);
        for (let i = 0; i < byteCharacters.length; i++) {
          byteNumbers[i] = byteCharacters.charCodeAt(i);
        }
        const byteArray = new Uint8Array(byteNumbers);
        const blob = new Blob([byteArray], { type: 'text/csv;charset=utf-8;' });

        // Tạo link tải xuống
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', response.data.fileName || 'score_template.csv');
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
      } else {
        setError('Không thể tải file mẫu');
      }
    } catch (err) {
      setError(`Lỗi tải file mẫu: ${err.response?.data?.message || err.message}`);
    }
  };

  // Xử lý khi chọn file
  const handleFileChange = (e) => {
    const selectedFile = e.target.files[0];
    if (selectedFile) {
      setFile(selectedFile);
      setFileName(selectedFile.name);

      // Preview nội dung CSV
      const reader = new FileReader();
      reader.onload = (event) => {
        const csvContent = event.target.result;
        const rows = csvContent.split('\n');
        const csvData = [];

        // Chỉ lấy tối đa 5 dòng để preview
        for (let i = 0; i < Math.min(rows.length, 6); i++) {
          if (rows[i].trim()) {
            csvData.push(rows[i].split(','));
          }
        }

        setCsvPreview(csvData);
        setShowCsvPreview(true);
      };

      reader.readAsText(selectedFile);
    } else {
      setFile(null);
      setFileName('');
      setShowCsvPreview(false);
    }

    // Reset thông báo
    setError('');
    setSuccess('');
    setMissingScoreTypes([]);
  };

  const refreshUserToken = async () => {
    try {
      // Kiểm tra lại token hiện tại - đây có thể là API gọi về backend để kiểm tra token
      const response = await scoreApis.getImportScoresFormData(); // Dùng một API đơn giản để kiểm tra token
      return true; // Nếu API thành công, token vẫn hợp lệ
    } catch (error) {
      if (error.response && error.response.status === 401) {
        setError('Phiên làm việc đã hết hạn, vui lòng đăng nhập lại');
        return false;
      }
      // Các lỗi khác không liên quan đến token
      return true;
    }
  };

  // Xử lý submit form
  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!selectedSubjectTeacher || !selectedClass || !selectedSchoolYear || !file) {
      setError('Vui lòng điền đầy đủ thông tin và chọn file');
      return;
    }
    
    // Làm mới token trước khi upload
    const tokenValid = await refreshUserToken();
    if (!tokenValid) {
      return; // Dừng lại nếu token không hợp lệ
    }

    try {
      setLoading(true);
      setError('');
      setSuccess('');
      setMissingScoreTypes([]);

      console.log("Sending import request for:", {
        file: file.name,
        subjectTeacherId: selectedSubjectTeacher,
        classId: selectedClass,
        schoolYearId: selectedSchoolYear
      });

      const response = await scoreApis.importScores(
        file,
        parseInt(selectedSubjectTeacher),
        parseInt(selectedClass),
        parseInt(selectedSchoolYear)
      );

      if (response.data && response.data.success) {
        setSuccess(response.data.message || 'Import điểm thành công');
        // Reset form sau khi import thành công
        setFile(null);
        setFileName('');
        setShowCsvPreview(false);
      } else {
        setError(response.data?.message || 'Có lỗi xảy ra khi import điểm');

        // Nếu có các loại điểm chưa được cấu hình
        if (response.data?.missingScoreTypes) {
          setMissingScoreTypes(response.data.missingScoreTypes);
        }
      }
    } catch (err) {
      console.error("Import error:", err);
      
      if (err.response && err.response.status === 401) {
        setError('Phiên làm việc đã hết hạn hoặc bạn không có quyền truy cập chức năng này');
        
        // Hiển thị nút đăng nhập lại thay vì tự động chuyển hướng
      } else if (err.response && err.response.data && err.response.data.message) {
        setError(`Lỗi: ${err.response.data.message}`);
        
        // Xử lý lỗi thiếu loại điểm
        if (err.response.data.missingScoreTypes) {
          setMissingScoreTypes(err.response.data.missingScoreTypes);
        }
      } else {
        setError(`Lỗi không xác định: ${err.message}`);
      }
    } finally {
      setLoading(false);
    }
  };

  // Chuyển về trang cấu hình loại điểm
  const goToScoreTypeConfig = () => {
    console.log("Navigation params:", {
      classId: selectedClass,
      subjectTeacherId: selectedSubjectTeacher,
      schoolYearId: selectedSchoolYear
    });
    
    // Kiểm tra dữ liệu trước khi điều hướng
    if (!selectedClass || !selectedSubjectTeacher || !selectedSchoolYear) {
      setError("Thiếu thông tin để cấu hình loại điểm. Vui lòng chọn đầy đủ môn học, lớp và năm học.");
      return;
    }
    
    // Sử dụng navigate thay vì window.location để tránh reload trang
    navigate(`/teacher/classes/${selectedClass}/scores?subjectTeacherId=${selectedSubjectTeacher}&schoolYearId=${selectedSchoolYear}`);
  };

  return (
    <Container className="mt-4">
      <h2 className="mb-4">
        <FontAwesomeIcon icon={faUpload} className="me-2" /> Import Điểm từ File CSV
      </h2>

      {/* Thông báo */}
      {error && (
        <Alert variant="danger" dismissible onClose={() => setError('')}>
          <FontAwesomeIcon icon={faExclamationTriangle} className="me-2" />
          {error}
        </Alert>
      )}

      {success && (
        <Alert variant="success" dismissible onClose={() => setSuccess('')}>
          <FontAwesomeIcon icon={faCheck} className="me-2" />
          {success}
        </Alert>
      )}

      {/* Thông báo thiếu loại điểm */}
      {missingScoreTypes.length > 0 && (
        <Alert variant="warning">
          <FontAwesomeIcon icon={faInfoCircle} className="me-2" />
          <p>File CSV chứa các loại điểm chưa được cấu hình trong hệ thống:</p>
          <ul>
            {missingScoreTypes.map((type, index) => (
              <li key={index}><strong>{type}</strong></li>
            ))}
          </ul>
          <p>Vui lòng thêm các loại điểm này vào hệ thống trước khi import.</p>
          <Button variant="primary" onClick={goToScoreTypeConfig}>
            Cấu hình loại điểm
          </Button>
        </Alert>
      )}

      <Card className="shadow-sm mb-4">
        <Card.Header className="bg-primary text-white">
          <Card.Title className="mb-0">Form Import Điểm</Card.Title>
        </Card.Header>
        <Card.Body>
          {loading && !error ? (
            <div className="text-center p-4">
              <Spinner animation="border" variant="primary" />
              <p className="mt-2">Đang tải dữ liệu...</p>
            </div>
          ) : (
            <Form onSubmit={handleSubmit}>
              <div className="row mb-3">
                <div className="col-md-6">
                  <Form.Group>
                    <Form.Label>Môn học</Form.Label>
                    <Form.Control
                      as="select"
                      value={selectedSubjectTeacher}
                      onChange={(e) => setSelectedSubjectTeacher(e.target.value)}
                      required
                    >
                      <option value="">-- Chọn môn học --</option>
                      {subjectTeachers.map(subj => (
                        <option key={subj.id} value={subj.id}>
                          {subj.subjectId.subjectCode} - {subj.subjectId.subjectName}
                        </option>
                      ))}
                    </Form.Control>
                  </Form.Group>
                </div>
                <div className="col-md-6">
                  <Form.Group>
                    <Form.Label>Lớp học</Form.Label>
                    <Form.Control
                      as="select"
                      value={selectedClass}
                      onChange={(e) => setSelectedClass(e.target.value)}
                      required
                      disabled={!selectedSubjectTeacher || loading} // Disable nếu chưa chọn môn hoặc đang tải
                    >
                      <option value="">-- Chọn lớp học --</option>
                      {filteredClasses.length > 0 ? (
                        filteredClasses.map(cls => (
                          <option key={cls.id} value={cls.id}>
                            {cls.className}
                          </option>
                        ))
                      ) : (
                        <option value="" disabled>Không có lớp học phù hợp</option>
                      )}
                    </Form.Control>
                    {filteredClasses.length === 0 && selectedSubjectTeacher && !loading && (
                      <small className="text-danger">
                        Không tìm thấy lớp học nào cho môn học này. Vui lòng liên hệ admin để kiểm tra phân công giảng dạy.
                      </small>
                    )}
                  </Form.Group>
                </div>
              </div>
              <div className="row mb-3">
                <div className="col-md-6">
                  <Form.Group>
                    <Form.Label>Năm học</Form.Label>
                    <Form.Control
                      as="select"
                      value={selectedSchoolYear}
                      onChange={(e) => setSelectedSchoolYear(e.target.value)}
                      required
                      disabled={!selectedClass || filteredSchoolYears.length === 0}
                    >
                      <option value="">-- Chọn học kỳ --</option>
                      {filteredSchoolYears.map(year => (
                        <option key={year.id} value={year.id}>
                          {year.nameYear} {year.semesterName}
                        </option>
                      ))}
                    </Form.Control>
                    {filteredSchoolYears.length === 0 && selectedClass && !loading && (
                      <small className="text-danger">
                        Không tìm thấy phân công năm học nào cho môn và lớp này.
                      </small>
                    )}
                  </Form.Group>
                </div>
                <div className="col-md-6">
                  <Form.Group>
                    <Form.Label>File CSV</Form.Label>
                    <div className="input-group">
                      <Form.Control
                        type="file"
                        accept=".csv"
                        onChange={handleFileChange}
                        required
                      />
                      <Button
                        variant="outline-secondary"
                        onClick={handleDownloadTemplate}
                      >
                        <FontAwesomeIcon icon={faDownload} className="me-1" /> Tải mẫu
                      </Button>
                    </div>
                    {fileName && <small className="text-muted">File đã chọn: {fileName}</small>}
                  </Form.Group>
                </div>
              </div>

              {/* Xem trước nội dung CSV */}
              {showCsvPreview && csvPreview.length > 0 && (
                <div className="mt-4">
                  <h5>Xem trước nội dung CSV:</h5>
                  <div className="table-responsive">
                    <Table striped bordered hover size="sm">
                      <tbody>
                        {csvPreview.map((row, rowIndex) => (
                          <tr key={rowIndex}>
                            {row.map((cell, cellIndex) => (
                              <td key={cellIndex}>{cell}</td>
                            ))}
                          </tr>
                        ))}
                      </tbody>
                    </Table>
                    {csvPreview.length > 5 && <p className="text-muted">* Chỉ hiển thị 5 dòng đầu tiên</p>}
                  </div>
                </div>
              )}

              <Alert variant="info" className="mt-3">
                <h6 className="alert-heading">
                  <FontAwesomeIcon icon={faInfoCircle} className="me-2" />
                  Hướng dẫn:
                </h6>
                <ul className="mb-0">
                  <li>File CSV phải có định dạng: MSSV, Họ tên, Giữa kỳ, Cuối kỳ, [Điểm bổ sung 1], [Điểm bổ sung 2], [Điểm bổ sung 3]</li>
                  <li>Dòng đầu tiên nên là tiêu đề các cột</li>
                  <li>Điểm số phải trong khoảng từ 0 đến 10</li>
                  <li>Nếu điểm trống, để trống ô đó trong CSV</li>
                  <li>Đảm bảo các loại điểm bổ sung đã được cấu hình trong hệ thống trước khi import</li>
                </ul>
              </Alert>

              <div className="d-flex justify-content-between mt-4">
                <Button variant="outline-secondary" onClick={handleDownloadTemplate}>
                  <FontAwesomeIcon icon={faDownload} className="me-1" />
                  Tải file mẫu
                </Button>

                <Button variant="primary" type="submit" disabled={loading || !file}>
                  {loading ? (
                    <>
                      <Spinner as="span" animation="border" size="sm" className="me-2" />
                      Đang xử lý...
                    </>
                  ) : (
                    <>
                      <FontAwesomeIcon icon={faUpload} className="me-2" />
                      Import điểm
                    </>
                  )}
                </Button>
              </div>
            </Form>
          )}
        </Card.Body>
      </Card>

      <Card className="shadow-sm">
        <Card.Header className="bg-info text-white">
          <Card.Title className="mb-0">FAQ - Câu hỏi thường gặp</Card.Title>
        </Card.Header>
        <Card.Body>
          <div className="accordion" id="faqAccordion">
            <div className="accordion-item">
              <h2 className="accordion-header">
                <button className="accordion-button" type="button" data-bs-toggle="collapse" data-bs-target="#collapseOne">
                  Làm thế nào để tạo file CSV?
                </button>
              </h2>
              <div id="collapseOne" className="accordion-collapse collapse show" data-bs-parent="#faqAccordion">
                <div className="accordion-body">
                  <p>Bạn có thể tạo file CSV bằng Excel hoặc các trình soạn thảo văn bản. Trong Excel, sau khi nhập dữ liệu, chọn "Lưu dưới dạng" và chọn định dạng "CSV (Comma delimited)".</p>
                </div>
              </div>
            </div>
            <div className="accordion-item">
              <h2 className="accordion-header">
                <button className="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#collapseTwo">
                  Làm thế nào để xử lý lỗi khi nhập điểm?
                </button>
              </h2>
              <div id="collapseTwo" className="accordion-collapse collapse" data-bs-parent="#faqAccordion">
                <div className="accordion-body">
                  <p>Nếu gặp lỗi, hệ thống sẽ hiển thị thông báo. Vui lòng kiểm tra:</p>
                  <ul>
                    <li>Mã sinh viên có đúng không</li>
                    <li>Định dạng điểm có phù hợp không (số từ 0-10)</li>
                    <li>File CSV có đúng định dạng không</li>
                    <li>Các loại điểm trong file CSV đã được cấu hình trong hệ thống chưa</li>
                  </ul>
                </div>
              </div>
            </div>
          </div>
        </Card.Body>
      </Card>
    </Container>
  );
};

export default ScoreImport;