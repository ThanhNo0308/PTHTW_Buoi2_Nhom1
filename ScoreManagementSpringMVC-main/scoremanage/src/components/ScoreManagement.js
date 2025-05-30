import React, { useState, useEffect, useContext } from 'react';
import { useParams, useLocation, useNavigate } from 'react-router-dom';
import { Container, Card, Form, Table, Button, Modal, Alert, Spinner } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {faArrowLeft, faPlus, faCog, faSave, faEdit, faLock,
  faExclamationCircle, faCheckCircle, faTimes, faFilePdf, faFileCsv} from '@fortawesome/free-solid-svg-icons';
import { scoreApis, teacherClassApis } from '../configs/Apis';
import { MyUserContext } from '../App';
import "../assets/css/styles.css";

const ScoreManagement = () => {
  const { classId } = useParams();
  const location = useLocation();
  const navigate = useNavigate();
  const [user] = useContext(MyUserContext);

  // State
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [classroom, setClassroom] = useState(null);
  const [subject, setSubject] = useState(null);
  const [schoolYear, setSchoolYear] = useState(null);
  const [students, setStudents] = useState([]);
  const [scoreTypes, setScoreTypes] = useState(['Giữa kỳ', 'Cuối kỳ']);
  const [scoreWeights, setScoreWeights] = useState({
    'Giữa kỳ': 0.4,
    'Cuối kỳ': 0.6
  });
  const [studentScores, setStudentScores] = useState({});
  const [subjectTeacherId, setSubjectTeacherId] = useState(null);
  const [schoolYearId, setSchoolYearId] = useState(null);

  const [showAddScoreTypeModal, setShowAddScoreTypeModal] = useState(false);
  const [showWeightsModal, setShowWeightsModal] = useState(false);
  const [newScoreType, setNewScoreType] = useState("");
  const [selectedScoreType, setSelectedScoreType] = useState("");
  const [scoreTypesList, setScoreTypesList] = useState([]);
  const [tempWeights, setTempWeights] = useState({});
  const [exportLoading, setExportLoading] = useState(false);
  const [sendingEmail, setSendingEmail] = useState(false);

  // Maximum score types
  const MAX_SCORE_TYPES = 5;

  // Parse query parameters
  useEffect(() => {
    const params = new URLSearchParams(location.search);
    setSubjectTeacherId(params.get('subjectTeacherId'));
    setSchoolYearId(params.get('schoolYearId'));
  }, [location]);

  useEffect(() => {
    // Gọi API khi modal được mở để có dữ liệu mới nhất
    if (showAddScoreTypeModal) {
      loadScoreTypesList();
    }
  }, [showAddScoreTypeModal]);

  // Load data when parameters are available
  useEffect(() => {
    if (classId && subjectTeacherId && schoolYearId && user) {
      loadClassDetails();
      loadWeightConfiguration();
    }
  }, [classId, subjectTeacherId, schoolYearId, user]);

  const loadClassDetails = async () => {
    try {
      setLoading(true);
      setError("");

      // Load class details
      const classResponse = await teacherClassApis.getClassDetail(classId, user.username);
      if (!classResponse.data) {
        console.error("Empty response from class details API");
        setError("Không nhận được dữ liệu từ máy chủ");
        return;
      }

      if (classResponse.data && classResponse.data.classroom) {
        setClassroom(classResponse.data.classroom);
        setStudents(classResponse.data.students || []);
      }

      // Load scores data
      const scoresResponse = await teacherClassApis.getClassScores(
        classId, subjectTeacherId, schoolYearId, user.username
      );

      if (scoresResponse.data) {
        setSubject(scoresResponse.data.subject);
        setSchoolYear(scoresResponse.data.schoolYear);

        if (scoresResponse.data.scoreTypes) {
          const loadedTypes = scoresResponse.data.scoreTypes;
          // Sắp xếp để loại điểm tùy chỉnh nằm trước "Giữa kỳ" và "Cuối kỳ"
          const customTypes = loadedTypes.filter(type => type !== 'Giữa kỳ' && type !== 'Cuối kỳ');
          const defaultTypes = loadedTypes.filter(type => type === 'Giữa kỳ' || type === 'Cuối kỳ');
          // Đảm bảo "Giữa kỳ" luôn đứng trước "Cuối kỳ" trong mảng
          const sortedDefaultTypes = defaultTypes.sort((a, b) => {
            if (a === 'Giữa kỳ' && b === 'Cuối kỳ') return -1;
            if (a === 'Cuối kỳ' && b === 'Giữa kỳ') return 1;
            return 0;
          });
          setScoreTypes([...customTypes, ...sortedDefaultTypes]);
        }

        if (scoresResponse.data.scoreWeights) {
          setScoreWeights(scoresResponse.data.scoreWeights);
        }

        if (scoresResponse.data.studentScores) {
          // Chuyển đổi định dạng dữ liệu từ backend sang định dạng mà frontend cần
          const formattedScores = {};

          Object.keys(scoresResponse.data.studentScores).forEach(studentId => {
            formattedScores[studentId] = {};

            Object.keys(scoresResponse.data.studentScores[studentId] || {}).forEach(scoreType => {
              const score = scoresResponse.data.studentScores[studentId][scoreType];
              formattedScores[studentId][scoreType] = {
                id: score.id,
                value: score.scoreValue,
                isLocked: score.isLocked
              };
            });
          });

          setStudentScores(formattedScores);
        } else {
          console.warn("No student scores found in API response");
        }

      }
    } catch (err) {
      console.error("Error loading class details:", err);
      setError(`Không thể tải thông tin: ${err.response?.data?.error || err.message}`);
    } finally {
      setLoading(false);
    }
  };

  const [loadingScoreTypes, setLoadingScoreTypes] = useState(false);

  const loadScoreTypesList = async () => {
    try {
      setLoadingScoreTypes(true);
      setError("");
      // Thay thế API call từ getAvailableScoreTypes sang getScoreTypeList
      const response = await scoreApis.getScoreTypeList();

      if (response.data) {
        // Chỉ lọc bỏ 'Giữa kỳ' và 'Cuối kỳ', không lọc các loại điểm đã sử dụng
        const availableTypes = response.data.filter(
          type => type !== 'Giữa kỳ' && type !== 'Cuối kỳ'
        );

        setScoreTypesList(availableTypes);
      }
    } catch (err) {
      console.error("Error loading score types:", err);
      setError(`Không thể tải danh sách loại điểm: ${err.message}`);
    } finally {
      setLoadingScoreTypes(false);
    }
  };

  const loadWeightConfiguration = async () => {
    try {
      const response = await scoreApis.getScoreWeights(
        classId, subjectTeacherId, schoolYearId
      );

      if (response.data && response.data.success && response.data.weights) {
        setScoreWeights(response.data.weights);
        setTempWeights(response.data.weights);
        recalculateAllAverages();
      }
    } catch (err) {
      console.error("Error loading weight configuration:", err);
    }
  };

  const calculateAverage = (studentId) => {
    const studentData = studentScores[studentId] || {};
    let totalWeightedScore = 0;
    let totalWeight = 0;
    let hasScores = false;

    for (const type in studentData) {
      if (studentData[type]?.value && scoreWeights[type]) {
        const scoreValue = parseFloat(studentData[type].value);
        const weight = parseFloat(scoreWeights[type]);
        if (!isNaN(scoreValue) && !isNaN(weight)) {
          totalWeightedScore += scoreValue * weight;
          totalWeight += weight;
          hasScores = true;
        }
      }
    }

    if (totalWeight > 0) {
      return (totalWeightedScore / totalWeight).toFixed(2);
    }
    return "-";
  };

  const recalculateAllAverages = () => {
    // This function is called whenever weights are updated
    // We don't need to do anything here as averages are calculated on demand in the render
  };

  const handleScoreChange = (studentId, scoreType, value) => {
    // Validate score value (0-10)
    let validatedValue = value;
    if (value !== '') {
      const numValue = parseFloat(value);
      if (numValue < 0) validatedValue = '0';
      if (numValue > 10) validatedValue = '10';
    }

    setStudentScores(prev => ({
      ...prev,
      [studentId]: {
        ...prev[studentId],
        [scoreType]: {
          ...(prev[studentId]?.[scoreType] || {}),
          value: validatedValue
        }
      }
    }));
  };

  const handleAddScoreType = async () => {
    try {
      if (!newScoreType && !selectedScoreType) {
        setError("Vui lòng chọn hoặc nhập tên loại điểm");
        return;
      }

      const scoreTypeName = selectedScoreType || newScoreType;

      // Check if this type already exists
      if (scoreTypes.includes(scoreTypeName)) {
        setError("Loại điểm này đã tồn tại");
        return;
      }

      // Check max number of score types
      if (scoreTypes.length >= MAX_SCORE_TYPES) {
        setError(`Không thể thêm quá ${MAX_SCORE_TYPES} loại điểm!`);
        return;
      }

      const response = await scoreApis.addScoreType(
        scoreTypeName, 0, classId, subjectTeacherId, schoolYearId
      );

      if (response.data && response.data.success) {
        // Thay đổi ở đây: thêm loại điểm mới vào trước "Giữa kỳ"
        setScoreTypes(prev => {
          const giuaKyIndex = prev.indexOf('Giữa kỳ');
          if (giuaKyIndex === -1) {
            return [...prev, scoreTypeName]; // Nếu không tìm thấy "Giữa kỳ", thêm vào cuối
          } else {
            // Chèn vào trước "Giữa kỳ"
            const newTypes = [...prev];
            newTypes.splice(giuaKyIndex, 0, scoreTypeName);
            return newTypes;
          }
        });

        // Tiếp tục các bước khác như cũ
        setScoreWeights(prev => ({
          ...prev,
          [scoreTypeName]: 0
        }));

        // Reset form
        setNewScoreType("");
        setSelectedScoreType("");
        setShowAddScoreTypeModal(false);

        // Update temp weights for configuration
        setTempWeights({ ...scoreWeights, [scoreTypeName]: 0 });
        setShowWeightsModal(true);

        setSuccess("Thêm loại điểm thành công! Vui lòng cấu hình trọng số điểm.");
      } else {
        setError(response.data?.message || "Không thể thêm loại điểm");
      }
    } catch (err) {
      console.error("Error adding score type:", err);
      setError(`Lỗi khi thêm loại điểm: ${err.message}`);
    }
  };

  const handleRemoveScoreType = async (scoreType) => {
    if (scoreType === 'Giữa kỳ' || scoreType === 'Cuối kỳ') {
      setError("Không thể xóa loại điểm mặc định!");
      return;
    }

    if (window.confirm(`Bạn có chắc chắn muốn xóa loại điểm "${scoreType}"? Tất cả điểm của loại này sẽ bị xóa vĩnh viễn.`)) {
      try {
        const response = await scoreApis.removeScoreType(
          scoreType, classId, subjectTeacherId, schoolYearId
        );

        if (response.data && response.data.success) {
          // Remove from score types
          setScoreTypes(prev => prev.filter(type => type !== scoreType));

          // Remove from weights
          const newWeights = { ...scoreWeights };
          delete newWeights[scoreType];
          setScoreWeights(newWeights);

          // Remove from student scores
          const newStudentScores = { ...studentScores };
          Object.keys(newStudentScores).forEach(studentId => {
            if (newStudentScores[studentId][scoreType]) {
              delete newStudentScores[studentId][scoreType];
            }
          });
          setStudentScores(newStudentScores);

          setSuccess(`Đã xóa loại điểm ${scoreType} và tất cả điểm liên quan. Vui lòng cấu hình lại trọng số điểm.`);

          // Update temp weights for configuration
          setTempWeights(newWeights);
          setShowWeightsModal(true);
        } else {
          setError(response.data?.message || "Không thể xóa loại điểm");
        }
      } catch (err) {
        console.error("Error removing score type:", err);
        setError(`Lỗi khi xóa loại điểm: ${err.message}`);
      }
    }
  };

  const handleWeightChange = (scoreType, value) => {
    setTempWeights(prev => ({
      ...prev,
      [scoreType]: parseFloat(value) / 100
    }));
  };

  const calculateTotalWeight = () => {
    return Object.values(tempWeights)
      .reduce((sum, weight) => sum + parseFloat(weight || 0) * 100, 0);
  };

  const saveWeightConfiguration = async () => {
    try {
      const total = calculateTotalWeight();
      if (Math.abs(total - 100) > 0.01) {
        setError(`Tổng trọng số phải bằng 100%. Hiện tại: ${total.toFixed(0)}%`);
        return;
      }

      const response = await scoreApis.configureWeights(
        classId, subjectTeacherId, schoolYearId, tempWeights
      );

      if (response.data && response.data.success) {
        setScoreWeights(tempWeights);
        setShowWeightsModal(false);
        setSuccess("Cấu hình trọng số đã được lưu thành công");
      } else {
        setError(response.data?.message || "Có lỗi khi lưu cấu hình trọng số");
      }
    } catch (err) {
      console.error("Error saving weights:", err);
      setError(`Lỗi khi lưu cấu hình: ${err.message}`);
    }
  };

  const validateScores = () => {
    // Check if any score is entered
    let hasInputs = false;
    for (const studentId in studentScores) {
      for (const type in studentScores[studentId] || {}) {
        if (studentScores[studentId][type]?.value) {
          hasInputs = true;
          break;
        }
      }
      if (hasInputs) break;
    }

    if (!hasInputs) {
      setError('Vui lòng nhập ít nhất một điểm trước khi lưu!');
      return false;
    }

    // Validate score values (0-10)
    for (const studentId in studentScores) {
      for (const type in studentScores[studentId] || {}) {
        const value = studentScores[studentId][type]?.value;
        if (value) {
          const numValue = parseFloat(value);
          if (isNaN(numValue) || numValue < 0 || numValue > 10) {
            setError('Điểm phải là số từ 0-10');
            return false;
          }
        }
      }
    }

    return true;
  };

  const saveScores = async (saveMode) => {
    if (!validateScores()) return;

    try {
      // Format scores data
      const formattedScores = [];
      for (const studentId in studentScores) {
        for (const type in studentScores[studentId] || {}) {
          if (studentScores[studentId][type]?.value) {
            formattedScores.push({
              studentId: parseInt(studentId),
              scoreType: type,
              scoreValue: parseFloat(studentScores[studentId][type].value),
              id: studentScores[studentId][type].id || null,
              isLocked: studentScores[studentId][type].isLocked || false
            });
          }
        }
      }

      let response;

      if (saveMode === 'draft') {
        // Sử dụng API lưu nháp - không gửi email
        response = await scoreApis.saveScoresDraft(
          subjectTeacherId,
          schoolYearId,
          formattedScores
        );
      } else {
        // Sử dụng API lưu điểm chính thức - có gửi email
        setSendingEmail(true);
        response = await scoreApis.saveScores(
          subjectTeacherId,
          schoolYearId,
          formattedScores,
          true
        );
      }


      if (response.data && response.data.success) {
        // Nếu lưu chính thức thành công và có thông báo gửi email
        if (saveMode === 'final' && response.data.emailsTriggered) {
          const emailCount = response.data.emailCount || "tất cả";
          setSuccess(`Lưu điểm chính thức thành công và đã gửi email thông báo tới ${emailCount} sinh viên!`);
        } else {
          setSuccess(saveMode === 'final' ? 'Lưu điểm chính thức thành công!' : 'Lưu điểm nháp thành công!');
        }

        // Update scores with new IDs and lock status
        if (response.data.scores) {
          const updatedScores = { ...studentScores };

          response.data.scores.forEach(score => {
            if (updatedScores[score.studentId] && updatedScores[score.studentId][score.scoreType]) {
              updatedScores[score.studentId][score.scoreType].id = score.id;

              // Luôn cập nhật trạng thái khóa từ kết quả backend, bất kể chế độ lưu là gì
              updatedScores[score.studentId][score.scoreType].isLocked = score.isLocked;
            }
          });

          setStudentScores(updatedScores);
        } else {
          loadClassDetails();
        }
      } else {
        setError(response.data?.message || 'Có lỗi khi lưu điểm');
      }
    } catch (err) {
      console.error("Error saving scores:", err);
      setError(`Lỗi khi lưu điểm: ${err.message}`);
    } finally {
      setSendingEmail(false);
    }
  };

  // Thêm hàm xuất PDF - đặt trước return statement trong component ScoreManagement
  const handleExportPDF = async () => {
    try {
      setExportLoading(true);

      const response = await scoreApis.exportScoresToPdf(
        classId,
        subjectTeacherId,
        schoolYearId
      );

      if (response.data && response.data.success && response.data.fileContent) {
        // Tạo blob từ Base64
        const byteCharacters = atob(response.data.fileContent);
        const byteNumbers = new Array(byteCharacters.length);

        for (let i = 0; i < byteCharacters.length; i++) {
          byteNumbers[i] = byteCharacters.charCodeAt(i);
        }

        const byteArray = new Uint8Array(byteNumbers);
        const blob = new Blob([byteArray], { type: 'application/pdf' });

        // Tạo URL và link tải xuống
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', response.data.fileName || 'scores.pdf');
        document.body.appendChild(link);
        link.click();
        link.remove();

        setSuccess("Xuất bảng điểm PDF thành công");
      } else {
        setError(response.data?.message || "Không thể xuất bảng điểm PDF");
      }
    } catch (err) {
      console.error("Error exporting PDF:", err);
      setError(`Lỗi khi xuất PDF: ${err.message}`);
    } finally {
      setExportLoading(false);
    }
  };

  // Xuất CSV
  const handleExportCSV = async () => {
    try {
      setExportLoading(true);

      const response = await scoreApis.exportScoresToCsv(
        classId,
        subjectTeacherId,
        schoolYearId
      );

      if (response.data && response.data.success && response.data.fileContent) {
        // Tạo blob từ Base64
        const byteCharacters = atob(response.data.fileContent);
        const byteNumbers = new Array(byteCharacters.length);

        for (let i = 0; i < byteCharacters.length; i++) {
          byteNumbers[i] = byteCharacters.charCodeAt(i);
        }

        const byteArray = new Uint8Array(byteNumbers);
        const blob = new Blob([byteArray], { type: 'text/csv;charset=utf-8;' });

        // Tạo URL và link tải xuống
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', response.data.fileName || 'scores.csv');
        document.body.appendChild(link);
        link.click();
        link.remove();

        setSuccess("Xuất bảng điểm CSV thành công");
      } else {
        setError(response.data?.message || "Không thể xuất bảng điểm CSV");
      }
    } catch (err) {
      console.error("Error exporting CSV:", err);
      setError(`Lỗi khi xuất CSV: ${err.message}`);
    } finally {
      setExportLoading(false);
    }
  };

  if (loading) {
    return (
      <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: '300px' }}>
        <Spinner animation="border" variant="primary" />
        <span className="ms-2">Đang tải dữ liệu...</span>
      </Container>
    );
  }

  return (
    <Container className="mt-4">
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h2><FontAwesomeIcon icon={faSave} className="me-2" /> Quản lý Điểm Lớp</h2>
        <Button
          variant="secondary"
          onClick={() => navigate(`/teacher/classes/${classId}`)}
        >
          <FontAwesomeIcon icon={faArrowLeft} className="me-2" /> Quay lại
        </Button>
      </div>

      {exportLoading && (
        <div className="position-fixed top-50 start-50 translate-middle bg-white p-4 rounded shadow text-center" style={{ zIndex: 9999 }}>
          <Spinner animation="border" variant="primary" />
          <p className="mt-2 mb-0">Đang xuất bảng điểm...</p>
        </div>
      )}

      {sendingEmail && (
        <div className="position-fixed top-50 start-50 translate-middle bg-white p-4 rounded shadow text-center" style={{ zIndex: 9999 }}>
          <Spinner animation="border" variant="primary" />
          <p className="mt-2 mb-0">Đang gửi email thông báo điểm tới sinh viên...</p>
        </div>
      )}

      {error && (
        <Alert variant="danger" dismissible onClose={() => setError("")}>
          <FontAwesomeIcon icon={faExclamationCircle} className="me-2" />
          {error}
        </Alert>
      )}

      {success && (
        <Alert variant="success" dismissible onClose={() => setSuccess("")}>
          <FontAwesomeIcon icon={faCheckCircle} className="me-2" />
          {success}
        </Alert>
      )}

      <Card className="shadow-sm mb-4">
        <Card.Header className="bg-primary text-white">
          <Card.Title className="mb-0">
            <FontAwesomeIcon icon={faLock} className="me-2" />
            Thông tin: {classroom?.className} - {subject?.subjectName} - {schoolYear?.nameYear} {schoolYear?.semesterName}
          </Card.Title>
        </Card.Header>
        <Card.Body>
          <div className="mb-3 d-flex">
            <Button
              variant="success"
              className="me-2"
              onClick={() => setShowAddScoreTypeModal(true)}
            >
              <FontAwesomeIcon icon={faPlus} className="me-2" /> Thêm loại điểm
            </Button>
            <Button
              variant="primary"
              onClick={() => {
                setTempWeights({ ...scoreWeights });
                setShowWeightsModal(true);
              }}
            >
              <FontAwesomeIcon icon={faCog} className="me-2" /> Cấu hình trọng số điểm
            </Button>
          </div>

          <div className="table-responsive text-center">
            <Table bordered hover>
              <thead className="table-light">
                <tr>
                  <th>STT</th>
                  <th>MSSV</th>
                  <th>Họ tên</th>
                  {scoreTypes.map((type) => (
                    <th key={type}>
                      {type} ({(scoreWeights[type] * 100).toFixed(0)}%)
                      {type !== 'Giữa kỳ' && type !== 'Cuối kỳ' && (
                        <Button
                          variant="link"
                          className="text-danger p-0 ms-2"
                          onClick={() => handleRemoveScoreType(type)}
                        >
                          <FontAwesomeIcon icon={faTimes} />
                        </Button>
                      )}
                    </th>
                  ))}
                  <th>Điểm TB</th>
                </tr>
              </thead>
              <tbody>
                {students.map((student, index) => {
                  const isLocked = studentScores[student.id]?.['Giữa kỳ']?.isLocked;
                  return (
                    <tr key={student.id}>
                      <td>{index + 1}</td>
                      <td>{student.studentCode}</td>
                      <td className="text-start">{student.lastName} {student.firstName} </td>
                      {scoreTypes.map((type) => (
                        <td key={`${student.id}-${type}`}>
                          <Form.Control
                            type="number"
                            step="0.1"
                            min="0"
                            max="10"
                            value={studentScores[student.id]?.[type]?.value || ''}
                            onChange={(e) => handleScoreChange(student.id, type, e.target.value)}
                            disabled={studentScores[student.id]?.[type]?.isLocked}
                          />
                        </td>
                      ))}
                      <td>
                        <span>{calculateAverage(student.id)}</span>
                      </td>

                    </tr>
                  );
                })}
              </tbody>
            </Table>
          </div>

          <div className="d-flex justify-content-end mt-3">

            <Button
              variant="secondary"
              className="me-2"
              onClick={() => saveScores('draft')}
            >
              <FontAwesomeIcon icon={faEdit} className="me-2" /> Lưu nháp
            </Button>
            <Button
              variant="primary"
              className="me-2"
              onClick={() => saveScores('final')}
              disabled={sendingEmail}
            >
              <FontAwesomeIcon icon={faSave} className="me-2" /> {sendingEmail ? 'Đang lưu...' : 'Lưu chính thức'}
            </Button>
            <Button
              variant="success"
              className="me-2"
              onClick={handleExportPDF}
            >
              <FontAwesomeIcon icon={faFilePdf} className="me-2" /> Xuất PDF
            </Button>
            <Button
              variant="warning"
              onClick={handleExportCSV}
            >
              <FontAwesomeIcon icon={faFileCsv} className="me-2" /> Xuất CSV
            </Button>
          </div>
        </Card.Body>
      </Card>

      {/* Add Score Type Modal */}
      <Modal show={showAddScoreTypeModal} onHide={() => setShowAddScoreTypeModal(false)}>
        <Modal.Header closeButton>
          <Modal.Title>Thêm loại điểm mới</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Form>
            <Form.Group className="mb-3">
              <Form.Label>Chọn loại điểm có sẵn</Form.Label>
              <Form.Select
                value={selectedScoreType}
                onChange={(e) => {
                  setSelectedScoreType(e.target.value);
                  if (e.target.value) setNewScoreType("");
                }}
                disabled={loadingScoreTypes}
              >
                <option value="">-- Chọn loại điểm có sẵn hoặc thêm mới --</option>
                {loadingScoreTypes ? (
                  <option value="" disabled>Đang tải danh sách...</option>
                ) : scoreTypesList.length > 0 ? (
                  scoreTypesList.map(type => (
                    <option key={type} value={type}>{type}</option>
                  ))
                ) : (
                  <option value="" disabled>Không có loại điểm mới</option>
                )}
              </Form.Select>
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Tên loại điểm mới</Form.Label>
              <Form.Control
                type="text"
                value={newScoreType}
                onChange={(e) => {
                  setNewScoreType(e.target.value);
                  if (e.target.value) setSelectedScoreType("");
                }}
                placeholder="Nhập tên mới hoặc để trống nếu đã chọn loại điểm có sẵn"
              />
              <Form.Text className="text-muted">
                Nhập tên mới hoặc để trống nếu đã chọn loại điểm có sẵn.
              </Form.Text>
            </Form.Group>
          </Form>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShowAddScoreTypeModal(false)}>
            Hủy
          </Button>
          <Button variant="primary" onClick={handleAddScoreType}>
            Lưu
          </Button>
        </Modal.Footer>
      </Modal>

      {/* Configure Weights Modal */}
      <Modal show={showWeightsModal} onHide={() => setShowWeightsModal(false)}>
        <Modal.Header closeButton>
          <Modal.Title>Cấu hình trọng số điểm</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Alert variant="info">
            Tổng trọng số các loại điểm phải bằng 100%
            <div className={`fw-bold mt-2 ${Math.abs(calculateTotalWeight() - 100) > 0.01 ? 'text-danger' : 'text-success'}`}>
              Tổng hiện tại: <span>{calculateTotalWeight().toFixed(0)}%</span>
            </div>
          </Alert>
          <Form>
            {Object.keys(tempWeights).map(type => (
              <Form.Group className="mb-3" key={type}>
                <Form.Label>Điểm {type} (%)</Form.Label>
                <Form.Control
                  type="number"
                  min="0"
                  max="100"
                  value={(tempWeights[type] * 100).toFixed(0)}
                  onChange={(e) => handleWeightChange(type, e.target.value)}
                />
              </Form.Group>
            ))}
          </Form>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShowWeightsModal(false)}>
            Hủy
          </Button>
          <Button variant="primary" onClick={saveWeightConfiguration}>
            Lưu cấu hình
          </Button>
        </Modal.Footer>
      </Modal>
    </Container>
  );
};

export default ScoreManagement;