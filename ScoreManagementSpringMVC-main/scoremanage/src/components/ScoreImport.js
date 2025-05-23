import React, { useState, useEffect, useContext } from 'react';
import { Container, Card, Form, Button, Alert, Spinner, Table } from 'react-bootstrap';
import { scoreApis } from '../configs/Apis';
import { MyUserContext } from '../App';
import { useNavigate } from 'react-router-dom';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faDownload, faUpload, faInfoCircle, faCheck, faExclamationTriangle} from '@fortawesome/free-solid-svg-icons';

const ScoreImport = () => {
  const [user] = useContext(MyUserContext);
  const navigate = useNavigate();
  // State cho form và dữ liệu
  const [loading, setLoading] = useState(true);
  const [subjectTeachers, setSubjectTeachers] = useState([]);
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
  const [invalidStudents, setInvalidStudents] = useState([]);
  const [showMissingScoreTypesAlert, setShowMissingScoreTypesAlert] = useState(true);
  const [showInvalidStudentsAlert, setShowInvalidStudentsAlert] = useState(true);
  const [invalidScores, setInvalidScores] = useState([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [missingScoreTypes, setMissingScoreTypes] = useState([]);
  const [originalAssignments, setOriginalAssignments] = useState([]);
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
          // Lấy tất cả assignments nhưng giữ nguyên dữ liệu gốc
          const allAssignments = response.data.subjectTeachers.map(assignment => ({
            id: assignment.id,  // SubjectTeacherId thực
            subjectId: assignment.subjectId.id,
            subjectName: assignment.subjectId.subjectName,
            classId: assignment.classId?.id,
            className: assignment.classId?.className,
            schoolYearId: assignment.schoolYearId?.id
          }));

          // Gom nhóm theo cặp subjectId và classId để lấy đúng phân công
          const assignmentMap = {};
          allAssignments.forEach(assignment => {
            const key = `${assignment.subjectId}_${assignment.classId}`;
            assignmentMap[key] = assignment;
          });

          // Tạo mảng các môn học (không trùng lặp) kèm theo subjectTeacherId đúng
          setSubjectTeachers(allAssignments);

          // Lưu tất cả assignments để sử dụng sau này
          setOriginalAssignments(allAssignments);

          setAllClasses(response.data.classes || []);
          // Không lọc classes ban đầu - để người dùng chọn môn học trước
          setFilteredClasses([]);
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
        setFilteredClasses(allClasses);
        return;
      }

      try {
        setLoading(true);

        // Tìm môn học được chọn
        const selectedSubject = subjectTeachers.find(
          s => s.id.toString() === selectedSubjectTeacher
        );

        // Lọc các lớp có phân công với môn học này
        const classesForSubject = originalAssignments
          .filter(a => a.subjectId === selectedSubject.subjectId)
          .map(a => ({
            id: a.classId,
            className: a.className,
            assignment: a  // Lưu lại để dùng sau
          }))
          .filter(c => c.id && c.className); // Loại bỏ các giá trị null

        setFilteredClasses(classesForSubject);

        // Reset selected class
        if (selectedClass) {
          const classExists = classesForSubject.some(
            c => c.id.toString() === selectedClass
          );
          if (!classExists) {
            setSelectedClass('');
          }
        }
      } catch (error) {
        console.error("Error filtering classes:", error);
        setFilteredClasses(allClasses);
      } finally {
        setLoading(false);
      }
    };

    fetchClassesBySubject();
  }, [selectedSubjectTeacher, allClasses, originalAssignments]);

  // Thêm useEffect để lọc học kỳ khi chọn môn và lớp
  useEffect(() => {
    if (selectedSubjectTeacher) {  // Chỉ kiểm tra selectedSubjectTeacher
      // Tìm assignment tương ứng để lấy classId
      const selected = subjectTeachers.find(st => st.id.toString() === selectedSubjectTeacher);
      const classId = selected?.classId; // Lấy classId từ phân công giảng dạy

      if (classId) {
        // Tải danh sách học kỳ cho phân công giảng dạy này
        const fetchSchoolYears = async () => {
          try {
            setLoading(true);
            const response = await scoreApis.getAvailableSchoolYears(
              parseInt(selectedSubjectTeacher),
              parseInt(classId)  // Sử dụng classId từ phân công giảng dạy
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
    } else {
      setFilteredSchoolYears([]);
      setSelectedSchoolYear('');
    }
  }, [selectedSubjectTeacher, subjectTeachers]);

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

      // Reset thông báo
      setError('');
      setSuccess('');
      setMissingScoreTypes([]);
      setInvalidStudents([]);
      setShowMissingScoreTypesAlert(true);
      setShowInvalidStudentsAlert(true);

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

        // Kiểm tra header của CSV
        if (csvData.length > 0) {
          const headers = csvData[0].map(header => header.trim());

          // Tìm vị trí của cột MSSV
          const mssvIndex = headers.findIndex(
            h => h === 'MSSV' || h === 'Mã SV' || h.toLowerCase().includes('mssv') || h.toLowerCase().includes('mã sv')
          );

          // Tìm vị trí của cột Cuối kỳ
          const finalExamIndex = headers.findIndex(
            h => h === 'Cuối kỳ' || h.toLowerCase().includes('cuối kỳ') || h.toLowerCase().includes('cuoi ky')
          );

          // Kiểm tra xem có cột STT trước MSSV không
          let hasSTTColumn = false;
          if (mssvIndex > 0) {
            for (let i = 0; i < mssvIndex; i++) {
              if (headers[i].toLowerCase().includes('stt') ||
                headers[i].toLowerCase() === 'tt' ||
                headers[i].toLowerCase() === 'STT') {
                hasSTTColumn = true;
                break;
              }
            }
          }

          // Kiểm tra xem có cột Điểm TB sau Cuối kỳ không
          let hasAvgColumn = false;
          if (finalExamIndex >= 0 && finalExamIndex < headers.length - 1) {
            for (let i = finalExamIndex + 1; i < headers.length; i++) {
              if (headers[i].toLowerCase().includes('tb') ||
                headers[i].toLowerCase().includes('trung bình') ||
                headers[i].toLowerCase().includes('Điểm TB')) {
                hasAvgColumn = true;
                break;
              }
            }
          }

          // Hiển thị cảnh báo nếu phát hiện cột STT hoặc Điểm TB
          if (hasSTTColumn || hasAvgColumn) {
            let warningMsg = 'Vui lòng xóa ';
            if (hasSTTColumn) warningMsg += 'cột STT trước cột MSSV';
            if (hasSTTColumn && hasAvgColumn) warningMsg += ' và ';
            if (hasAvgColumn) warningMsg += 'cột Điểm TB sau cột Cuối kỳ';
            warningMsg += ' trong file CSV trước khi import. Hệ thống sẽ tự động bỏ qua các cột này, nhưng có thể gây nhầm lẫn cho quá trình import.';

            setError(warningMsg);
          }
        }
      };

      reader.readAsText(selectedFile);
    } else {
      setFile(null);
      setFileName('');
      setShowCsvPreview(false);
    }
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
      setInvalidStudents([]);
      setShowMissingScoreTypesAlert(true);
      setShowInvalidStudentsAlert(true);

      if (csvPreview && csvPreview.length > 0) {
        const headers = csvPreview[0];
        console.log("CSV Headers:", headers);

        // Log từng header và mã Unicode của nó để debug vấn đề encoding
        headers.forEach(header => {
          console.log(`Header: "${header}" - Unicode chars:`,
            [...header].map(c => c.charCodeAt(0).toString(16)));
        });
      }

      const response = await scoreApis.importScores(
        file,
        parseInt(selectedSubjectTeacher),
        parseInt(selectedClass),
        parseInt(selectedSchoolYear)
      );

      if (response.data && response.data.success) {
        setSuccess(response.data.message || 'Import điểm thành công');

        // Chuyển hướng đến trang chi tiết lớp sau khi import thành công
        setTimeout(() => {
          navigate(`/teacher/classes/${selectedClass}/scores?subjectTeacherId=${selectedSubjectTeacher}&schoolYearId=${selectedSchoolYear}`);
        }, 2000); // Đợi 2 giây để người dùng có thể thấy thông báo thành công
      } else {
        setError(response.data?.message || 'Có lỗi xảy ra khi import điểm');

        // Hiển thị các điểm không hợp lệ nếu có
        if (response.data?.invalidScores && response.data.invalidScores.length > 0) {
          setInvalidScores(response.data.invalidScores);
        }

        // Xử lý trường hợp sinh viên không hợp lệ
        if (response.data?.invalidStudents && response.data.invalidStudents.length > 0) {
          setInvalidStudents(response.data.invalidStudents);
        }

        // Nếu có các loại điểm chưa được cấu hình
        if (response.data?.missingScoreTypes) {
          setMissingScoreTypes(response.data.missingScoreTypes);
        }
      }
    } catch (err) {
      console.error("Error importing scores:", err);
      // Trường hợp lỗi từ API
      if (err.response && err.response.data) {
        const errorMessage = err.response.data.message || 'Có lỗi xảy ra khi import điểm';

        // Kiểm tra nếu là lỗi định dạng CSV
        if (err.response.data.error === "csv_format_error") {
          // Hiển thị thông báo lỗi định dạng CSV với định dạng đặc biệt
          setError(
            <div>
              <p className="mb-2">{errorMessage.split('\n\n')[0]}</p>
              {errorMessage.includes('\n\n') && (
                <div className="mt-2 p-2 bg-light border rounded">
                  <p className="fw-bold mb-1">Gợi ý:</p>
                  <ul className="mb-0 ps-3">
                    {errorMessage
                      .split('\n\n')[1]
                      .replace('Gợi ý:', '')
                      .split('\n')
                      .filter(line => line.trim())
                      .map((suggestion, index) => (
                        <li key={index}>{suggestion.replace(/^\d+\.\s/, '')}</li>
                      ))}
                  </ul>
                </div>
              )}
            </div>
          );
        } else {
          // Lỗi thông thường
          setError(errorMessage);
        }

        // Xử lý các sinh viên không hợp lệ từ response error
        if (err.response.data.invalidStudents && err.response.data.invalidStudents.length > 0) {
          setInvalidStudents(err.response.data.invalidStudents);
        }

        // Xử lý thiếu loại điểm
        if (err.response.data.missingScoreTypes) {
          setMissingScoreTypes(err.response.data.missingScoreTypes);
        }
      } else {
        setError('Có lỗi xảy ra khi kết nối đến máy chủ');
      }
    } finally {
      setLoading(false);
    }
  };

  // Chuyển về trang cấu hình loại điểm
  const goToScoreTypeConfig = () => {
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

      {invalidScores.length > 0 && (
        <Alert
          variant="danger"
          dismissible
          onClose={() => setInvalidScores([])}
        >
          <FontAwesomeIcon icon={faExclamationTriangle} className="me-2" />
          <p>File CSV chứa điểm không hợp lệ:</p>
          <div className="table-responsive">
            <Table striped bordered hover size="sm">
              <thead>
                <tr>
                  <th>Dòng</th>
                  <th>MSSV</th>
                  <th>Họ tên</th>
                  <th>Loại điểm</th>
                  <th>Giá trị</th>
                  <th>Lỗi</th>
                </tr>
              </thead>
              <tbody>
                {invalidScores.map((score, index) => (
                  <tr key={index}>
                    <td>{score.row}</td>
                    <td>{score.studentCode}</td>
                    <td>{score.fullName}</td>
                    <td>{score.scoreType}</td>
                    <td className="text-danger fw-bold">{score.value}</td>
                    <td>{score.error}</td>
                  </tr>
                ))}
              </tbody>
            </Table>
          </div>
          <p className="mt-2">Vui lòng sửa các giá trị không hợp lệ trong file CSV và thử lại.</p>
        </Alert>
      )}

      {/* Thông báo thiếu loại điểm */}
      {missingScoreTypes.length > 0 && showMissingScoreTypesAlert && (
        <Alert
          variant="warning"
          dismissible
          onClose={() => setShowMissingScoreTypesAlert(false)}
        >
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

      {invalidStudents.length > 0 && showInvalidStudentsAlert && (
        <Alert
          variant="warning"
          dismissible
          onClose={() => setShowInvalidStudentsAlert(false)}
        >
          <FontAwesomeIcon icon={faExclamationTriangle} className="me-2" />
          <p>File CSV chứa sinh viên không tồn tại trong danh sách lớp này:</p>
          <div className="table-responsive">
            <Table striped bordered hover size="sm">
              <thead>
                <tr>
                  <th>STT</th>
                  <th>MSSV</th>
                  <th>Họ tên trong CSV</th>
                  <th>Lỗi</th>
                </tr>
              </thead>
              <tbody>
                {invalidStudents.map((student, index) => (
                  <tr key={index}>
                    <td>{index + 1}</td>
                    <td>{student.studentCode}</td>
                    <td>{student.fullName}</td>
                    <td>{student.error}</td>
                  </tr>
                ))}
              </tbody>
            </Table>
          </div>
          <p className="mt-2">Vui lòng kiểm tra lại file CSV hoặc danh sách sinh viên trong hệ thống.</p>
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
                    <Form.Label>Phân công giảng dạy</Form.Label>
                    <Form.Control
                      as="select"
                      value={selectedSubjectTeacher}
                      onChange={(e) => {
                        setSelectedSubjectTeacher(e.target.value);
                        // Tìm assignment tương ứng và tự động chọn lớp học
                        const selected = subjectTeachers.find(st => st.id.toString() === e.target.value);
                        if (selected && selected.classId) {
                          setSelectedClass(selected.classId.toString());

                          // Log để xác nhận đã chọn đúng lớp học
                          console.log("Auto-selected class:", {
                            subjectTeacherId: selected.id,
                            classId: selected.classId,
                            className: selected.className
                          });
                        }
                      }}
                      required
                    >
                      <option value="">-- Chọn phân công giảng dạy --</option>
                      {subjectTeachers.map(subj => (
                        <option key={subj.id} value={subj.id}>
                          {subj.subjectName} - Lớp: {subj.className || 'Chưa phân lớp'}
                        </option>
                      ))}
                    </Form.Control>
                  </Form.Group>
                </div>
                <div className="col-md-6">
                  <Form.Group>
                    <Form.Label>Năm học</Form.Label>
                    <Form.Control
                      as="select"
                      value={selectedSchoolYear}
                      onChange={(e) => setSelectedSchoolYear(e.target.value)}
                      required
                      disabled={!selectedSubjectTeacher || filteredSchoolYears.length === 0}
                    >
                      <option value="">-- Chọn học kỳ --</option>
                      {filteredSchoolYears.map(year => (
                        <option key={year.id} value={year.id}>
                          {year.nameYear} {year.semesterName}
                        </option>
                      ))}
                    </Form.Control>
                    {filteredSchoolYears.length === 0 && selectedSubjectTeacher && !loading && (
                      <small className="text-danger">
                        Không tìm thấy phân công năm học nào cho môn và lớp này.
                      </small>
                    )}
                  </Form.Group>
                </div>
              </div>
              <div className="row mb-3">

                <div className="col-md-12">
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
                  <li>File CSV phải có định dạng: MSSV, Họ tên, [Điểm bổ sung 1], [Điểm bổ sung 2], [Điểm bổ sung 3], Giữa kỳ, Cuối kỳ</li>
                  <li>File không được chứa cột STT và Điểm TB - Hệ thống sẽ tự động tính điểm dựa trên loại điểm đã cấu hình của môn học</li>
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
            <div className="accordion-item">
              <h2 className="accordion-header">
                <button className="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#collapseThree">
                  Làm thế nào để khắc phục lỗi định dạng CSV?
                </button>
              </h2>
              <div id="collapseThree" className="accordion-collapse collapse" data-bs-parent="#faqAccordion">
                <div className="accordion-body">
                  <p>Nếu bạn gặp lỗi định dạng CSV, hãy thử các cách sau:</p>
                  <ul>
                    <li><strong>Kiểm tra Encoding</strong>: Lưu file với encoding UTF-8</li>
                    <li><strong>Dấu phẩy trong nội dung</strong>: Nếu dữ liệu có chứa dấu phẩy, phải đặt trong dấu ngoặc kép</li>
                    <li><strong>Dấu ngoặc kép trong nội dung</strong>: Nếu dữ liệu có chứa dấu ngoặc kép, phải escape bằng cách thêm một dấu ngoặc kép nữa (ví dụ: "" thay vì ")</li>
                    <li><strong>Kiểm tra bằng Notepad</strong>: Mở file CSV bằng Notepad để kiểm tra định dạng thô</li>
                    <li><strong>Xuất lại từ Excel</strong>: Khi xuất từ Excel, chọn "CSV (Comma delimited) (*.csv)" và kiểm tra cài đặt vùng miền</li>
                    <li><strong>Tải file mẫu</strong>: Sử dụng file mẫu từ hệ thống để đảm bảo định dạng chuẩn</li>
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