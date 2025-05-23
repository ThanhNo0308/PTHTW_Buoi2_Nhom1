import React, { useState, useEffect, useContext } from 'react';
import { Link, Navigate } from 'react-router-dom';
import { MyUserContext } from "../App";
import { endpoints, API, studentApis } from '../configs/Apis';
import { Bar } from 'react-chartjs-2';
import { Chart as ChartJS, CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend } from 'chart.js';
import { Alert, Spinner, Card, Form, Dropdown, DropdownButton, Row, Col } from 'react-bootstrap';
import "../assets/css/base.css";
import "../assets/css/styles.css";
import "../assets/css/dashboard.css";
import defaultAvatar from '../assets/images/logo.png';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faEdit } from '@fortawesome/free-solid-svg-icons/faEdit';
import {
  faComments, faCheckCircle, faExclamationCircle, faCalendarAlt,
  faChartLine, faUsers, faUserEdit, faBook, faGraduationCap, faPaperPlane
} from '@fortawesome/free-solid-svg-icons';

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend);
const StudentDashboard = () => {
  const [user] = useContext(MyUserContext);
  const [student, setStudent] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  const [scoreData, setScoreData] = useState([]);
  const [schoolYears, setSchoolYears] = useState([]);
  const [selectedSchoolYear, setSelectedSchoolYear] = useState(null);
  const [chartLoading, setChartLoading] = useState(false);
  const [semesterAverage, setSemesterAverage] = useState(0);

  useEffect(() => {
    if (!user) return;
    const loadStudentInfo = async () => {
      try {
        setLoading(true);
        // Gọi API student current thay vì current-user để lấy đầy đủ thông tin sinh viên
        const res = await API.get(endpoints["student-current"]);

        if (res.data && res.data.student) {
          // Lưu thông tin sinh viên từ API trả về
          setStudent(res.data.student);

          // Nếu có query param success, hiển thị thông báo
          const urlParams = new URLSearchParams(window.location.search);
          if (urlParams.get('success')) {
            setSuccessMessage("Đăng nhập thành công!");
          }

          await loadSchoolYears();
        }
      } catch (err) {
        console.error("Lỗi khi tải thông tin sinh viên:", err);
        setError("Không thể tải thông tin sinh viên. Vui lòng thử lại sau.");
      } finally {
        setLoading(false);
      }
    };

    loadStudentInfo();
  }, [user]);

  // Hàm tải danh sách học kỳ
  const loadSchoolYears = async () => {
  try {
    const res = await studentApis.getStudentScores();

    if (res.data && res.data.schoolYears) {
      setSchoolYears(res.data.schoolYears);

      // Mặc định chọn học kỳ hiện tại nếu có
      if (res.data.currentSchoolYear) {
        setSelectedSchoolYear(res.data.currentSchoolYear);
        
        await loadScoresBySchoolYear(res.data.currentSchoolYear.id);
      } else if (res.data.schoolYears.length > 0) {
        setSelectedSchoolYear(res.data.schoolYears[0]);
        await loadScoresBySchoolYear(res.data.schoolYears[0].id);
      }
    }
  } catch (err) {
    console.error("Lỗi khi tải danh sách học kỳ:", err);
  }
}

  // Hàm tải điểm theo học kỳ
  const loadScoresBySchoolYear = async (schoolYearId) => {
    try {
      setChartLoading(true);
      const res = await studentApis.getStudentScores(schoolYearId);

      if (res.data) {
        // Kiểm tra nếu API trả về subjectAverages
        if (res.data.subjectAverages && Object.keys(res.data.subjectAverages).length > 0) {
          // Format data từ subjectAverages
          const formattedData = [];
          for (const [subjectId, avgScore] of Object.entries(res.data.subjectAverages)) {
            // Tìm môn học tương ứng trong danh sách scores
            const subjectScore = res.data.scores.find(
              score => score.subjectTeacherID?.subjectId?.id == subjectId
            );

            if (subjectScore) {
              formattedData.push({
                subjectId: subjectId,
                subjectName: subjectScore.subjectTeacherID.subjectId.subjectName,
                subjectCode: subjectScore.subjectTeacherID.subjectId.subjectCode || `SUBJ${subjectId}`,
                credits: subjectScore.subjectTeacherID.subjectId.credits || 0,
                averageScore: parseFloat(avgScore.toFixed(2))
              });
            }
          }

          setScoreData(formattedData);
          // Cập nhật điểm trung bình học kỳ
          setSemesterAverage(res.data.semesterAverage || 0);
        } else {
          // Nếu không có sẵn điểm trung bình, tính toán từ scores
          const formattedScores = processScoreData(res.data.scores || []);
          setScoreData(formattedScores);
        }
      } else {
        setScoreData([]);
      }
    } catch (err) {
      console.error("Lỗi khi tải điểm:", err);
      setScoreData([]);
    } finally {
      setChartLoading(false);
    }
  };

  // Hàm xử lý dữ liệu điểm để hiển thị trên biểu đồ
  const processScoreData = (scores) => {
    // Tạo đối tượng để nhóm điểm theo môn học
    const subjectScores = {};
    const subjectCredits = {};
    const subjectWeights = {};

    // Nhóm điểm theo môn học
    scores.forEach(score => {
      if (!score.subjectTeacherID?.subjectId) return;

      const subjectId = score.subjectTeacherID.subjectId.id;
      const subjectName = score.subjectTeacherID.subjectId.subjectName || "Không tên";
      const credits = score.subjectTeacherID.subjectId.credits || 0;

      if (!subjectScores[subjectId]) {
        subjectScores[subjectId] = [];
        subjectCredits[subjectId] = credits;
      }

      // Thêm điểm và loại điểm vào mảng
      if (score.scoreValue !== undefined && score.scoreType) {
        subjectScores[subjectId].push({
          value: score.scoreValue,
          type: score.scoreType.scoreType,
          weight: score.scoreType.weight || 0
        });

        // Lưu trọng số của loại điểm
        if (!subjectWeights[subjectId]) {
          subjectWeights[subjectId] = {};
        }
        subjectWeights[subjectId][score.scoreType.scoreType] = score.scoreType.weight || 0;
      }
    });

    // Tính điểm trung bình cho mỗi môn học
    const result = [];
    Object.keys(subjectScores).forEach(subjectId => {
      const scores = subjectScores[subjectId];
      if (scores.length === 0) return;

      // Kiểm tra xem có đủ điểm giữa kỳ và cuối kỳ không
      const hasGiuaKy = scores.some(s => s.type === 'Giữa kỳ');
      const hasCuoiKy = scores.some(s => s.type === 'Cuối kỳ');

      if (!hasGiuaKy || !hasCuoiKy) return; // Bỏ qua môn học không đủ điểm

      // Tính điểm trung bình có trọng số
      let totalWeightedScore = 0;
      let totalWeight = 0;

      scores.forEach(score => {
        totalWeightedScore += score.value * score.weight;
        totalWeight += score.weight;
      });

      if (totalWeight === 0) return;

      const avgScore = totalWeightedScore / totalWeight;

      // Lấy thông tin môn học từ điểm đầu tiên trong mảng
      const subjectInfo = scores[0]?.subjectInfo || {};

      // Thêm vào kết quả
      result.push({
        subjectId: subjectId,
        subjectCode: scores[0]?.subjectCode || `SUBJ${subjectId}`,
        subjectName: scores.length > 0 && scores[0].subjectTeacherID ?
          scores[0].subjectTeacherID.subjectId.subjectName :
          subjectInfo.subjectName || "Không tên",
        credits: subjectCredits[subjectId],
        averageScore: parseFloat(avgScore.toFixed(2))
      });
    });

    return result;
  };

  // Hàm xử lý khi chọn học kỳ
  const handleSchoolYearChange = (schoolYearId) => {
    const selectedYear = schoolYears.find(year => year.id === parseInt(schoolYearId));
    setSelectedSchoolYear(selectedYear);
    loadScoresBySchoolYear(schoolYearId);
  }

  // Cấu hình cho biểu đồ
  const chartData = {
    labels: scoreData.map(item => item.subjectName), // Thay đổi từ subjectCode sang subjectName
    datasets: [
      {
        label: 'Điểm trung bình',
        data: scoreData.map(item => item.averageScore),
        backgroundColor: scoreData.map(item => {
          // Màu sắc dựa trên điểm
          if (item.averageScore >= 8) return 'rgba(75, 192, 92, 0.7)'; // Xanh lá
          if (item.averageScore >= 6.5) return 'rgba(54, 162, 235, 0.7)'; // Xanh dương
          if (item.averageScore >= 5) return 'rgba(255, 206, 86, 0.7)'; // Vàng
          return 'rgba(255, 99, 132, 0.7)'; // Đỏ
        }),
        borderColor: scoreData.map(item => {
          if (item.averageScore >= 8) return 'rgba(75, 192, 92, 1)';
          if (item.averageScore >= 6.5) return 'rgba(54, 162, 235, 1)';
          if (item.averageScore >= 5) return 'rgba(255, 206, 86, 1)';
          return 'rgba(255, 99, 132, 1)';
        }),
        borderWidth: 1,
      }
    ]
  };

  const chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    scales: {
      y: {
        beginAtZero: true,
        max: 10,
        title: {
          display: true,
          text: 'Điểm'
        }
      },
      x: {
        title: {
          display: true,
          text: 'Môn học'
        }
      }
    },
    plugins: {
      tooltip: {
        callbacks: {
          title: (items) => {
            if (!items.length) return '';
            const index = items[0].dataIndex;
            return scoreData[index].subjectName;
          },
          label: (item) => {
            return `Điểm: ${item.formattedValue}`;
          }
        }
      }
    }
  };

  if (!user) {
    return <Navigate to="/login" />;
  }

  if (loading) {
    return (
      <div className="container d-flex justify-content-center align-items-center" style={{ minHeight: "60vh" }}>
        <Spinner animation="border" role="status" variant="primary">
          <span className="visually-hidden">Đang tải...</span>
        </Spinner>
      </div>
    );
  }

  return (
    <div className="teacher-dashboard"> 
      <div className="dashboard-header">
        <div className="container">
          <div className="teacher-profile">
            <img src={student?.image || user?.image || defaultAvatar} alt="Profile" className="profile-avatar" />
            <div className="profile-info">
              <div className="welcome-text">Xin chào, Sinh viên</div>
              <h2>{student?.lastName || user?.name} {student?.firstName || ""}</h2>
              <div className="d-flex gap-2">
                <span className="badge bg-light text-dark">
                  MSSV: {student?.studentCode || "Chưa cập nhật"}
                </span>
                <span className="badge bg-light text-dark">
                  Lớp: {student?.classId?.className || "Chưa cập nhật"}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="container">
        {successMessage && (
          <Alert variant="success" dismissible onClose={() => setSuccessMessage("")} className="animate-fade-in">
            <FontAwesomeIcon icon={faCheckCircle} className="me-2" />
            {successMessage}
          </Alert>
        )}

        {error && (
          <Alert variant="danger" dismissible onClose={() => setError("")} className="animate-fade-in">
            <FontAwesomeIcon icon={faExclamationCircle} className="me-2" />
            {error}
          </Alert>
        )}

        <div className="row">
          <div className="col-lg-8">
            <div className="row mb-4">
              <div className="col-md-6 mb-3">
                <div className="feature-card">
                  <div className="card-header-custom bg-soft-primary">
                    <FontAwesomeIcon icon={faChartLine} className="card-header-icon" />
                    Điểm số
                  </div>
                  <Card.Body>
                    <Card.Text>
                      Xem bảng điểm của tất cả các môn học và theo dõi kết quả học tập.
                    </Card.Text>
                    <Link to="/student/scores" className="btn btn-primary feature-btn w-100">
                      <FontAwesomeIcon icon={faChartLine} className="me-2" />
                      Xem điểm của tôi
                    </Link>
                  </Card.Body>
                </div>
              </div>

              <div className="col-md-6 mb-3">
                <div className="feature-card">
                  <div className="card-header-custom bg-soft-info">
                    <FontAwesomeIcon icon={faUsers} className="card-header-icon" />
                    Lớp học
                  </div>
                  <Card.Body>
                    <Card.Text>
                      Xem thông tin về lớp học và danh sách các bạn cùng lớp.
                    </Card.Text>
                    <Link to="/student/class-info" className="btn btn-info feature-btn w-100 text-white">
                      <FontAwesomeIcon icon={faUsers} className="me-2" />
                      Thông tin lớp
                    </Link>
                  </Card.Body>
                </div>
              </div>
            </div>

            <div className="row mb-4">
              <div className="col-md-6 mb-3">
                <div className="feature-card">
                  <div className="card-header-custom bg-soft-warning">
                    <FontAwesomeIcon icon={faEdit} className="card-header-icon" />
                    Đăng ký môn học
                  </div>
                  <Card.Body>
                    <Card.Text>
                      Đăng ký các môn học cho học kỳ tiếp theo. Xem danh sách môn học có thể đăng ký.
                    </Card.Text>
                    <Link to="/student/course-registration" className="btn btn-warning feature-btn w-100">
                      <FontAwesomeIcon icon={faEdit} className="me-2" />
                      Đăng ký môn học
                    </Link>
                  </Card.Body>
                </div>
              </div>

              <div className="col-md-6 mb-3">
                <div className="feature-card">
                  <div className="card-header-custom bg-soft-success">
                    <FontAwesomeIcon icon={faBook} className="card-header-icon" />
                    Môn học
                  </div>
                  <Card.Body>
                    <Card.Text>
                      Xem danh sách môn học bạn đã đăng ký trong học kỳ hiện tại và trước đây.
                    </Card.Text>
                    <Link to="/student/subjects" className="btn btn-success feature-btn w-100">
                      <FontAwesomeIcon icon={faBook} className="me-2" />
                      Môn học đã đăng ký
                    </Link>
                  </Card.Body>
                </div>
              </div>

              <div className="col-md-6 mb-3">
                <div className="feature-card">
                  <div className="card-header-custom bg-soft-danger">
                    <FontAwesomeIcon icon={faPaperPlane} className="card-header-icon" />
                    Tin nhắn
                  </div>
                  <Card.Body>
                    <Card.Text>
                      Gửi và nhận tin nhắn với giảng viên và đồng nghiệp. Trao đổi thông tin nhanh chóng.
                    </Card.Text>
                    <Link to="/chat" className="btn btn-outline-danger feature-btn w-100">
                      <FontAwesomeIcon icon={faPaperPlane} className="me-2" />
                      Truy cập tin nhắn
                    </Link>
                  </Card.Body>
                </div>

              </div>

              <div className="col-md-6 mb-3">
                <div className="feature-card">
                  <div className="card-header-custom bg-soft-warning">
                    <FontAwesomeIcon icon={faComments} className="card-header-icon" />
                    Diễn đàn học tập
                  </div>
                  <Card.Body>
                    <Card.Text>
                      Tham gia các diễn đàn học tập để xem thảo luận và đặt câu hỏi về các môn học.
                    </Card.Text>
                    <Link to="/forums" className="btn btn-warning feature-btn w-100">
                      <FontAwesomeIcon icon={faComments} className="me-2" />
                      Truy cập diễn đàn
                    </Link>
                  </Card.Body>
                </div>
              </div>

              <div className="col-md-6 mb-3">
                <div className="feature-card">
                  <div className="card-header-custom bg-soft-info">
                    <FontAwesomeIcon icon={faCalendarAlt} className="card-header-icon" />
                    Lịch Học
                  </div>
                  <Card.Body>
                    <Card.Text>
                      Xem lịch học của bạn theo tuần, tháng. Theo dõi thời gian và phòng học của các môn học đã đăng ký.
                    </Card.Text>
                    <Link to="/student/schedule" className="btn btn-info feature-btn w-100 text-white">
                      <FontAwesomeIcon icon={faCalendarAlt} className="me-2" />
                      Xem lịch học
                    </Link>
                  </Card.Body>
                </div>
              </div>
            </div>
          </div>

          <div className="col-lg-4">
            <Row>
              {/* Card thông tin sinh viên */}
              <Col xs={12}>
                <div className="feature-card mb-4">
                  <div className="card-header-custom bg-soft-dark">
                    <FontAwesomeIcon icon={faGraduationCap} className="card-header-icon" />
                    Thông tin cá nhân
                  </div>
                  <Card.Body className="p-3" style={{ maxHeight: "320px", overflowY: "auto" }}>
                    <div className="d-flex align-items-center mb-3">
                      <img
                        src={student?.image || user?.image || defaultAvatar}
                        alt="Profile"
                        className="rounded-circle me-3"
                        style={{ width: "60px", height: "60px", objectFit: "cover", border: "2px solid #f8f9fa" }}
                      />
                      <div>
                        <h5 className="mb-0">{student?.lastName || user?.name} {student?.firstName}</h5>
                        <span className="badge bg-primary px-2 py-1">{user?.role || "Sinh viên"}</span>
                      </div>
                    </div>
                    <div className="small text-start mb-3">
                      <p className="mb-1"><strong>Email:</strong> {student?.email || user?.email || "Chưa cập nhật"}</p>
                      <p className="mb-1"><strong>Lớp:</strong> {student?.classId?.className || "Chưa cập nhật"}</p>
                      <p className="mb-1"><strong>Ngành:</strong> {student?.classId?.majorId?.majorName || "Chưa cập nhật"}</p>
                      <p className="mb-1"><strong>Khoa:</strong> {student?.classId?.majorId?.departmentId?.departmentName || "Chưa cập nhật"}</p>
                      <p className="mb-1"><strong>Loại đào tạo:</strong> {student?.classId?.majorId?.trainingTypeId?.trainingTypeName || "Chưa cập nhật"}</p>
                    </div>
                    <Link to="/profile" className="btn btn-outline-dark w-100">
                      <FontAwesomeIcon icon={faUserEdit} className="me-2" />
                      Cập nhật thông tin
                    </Link>
                  </Card.Body>
                </div>
              </Col>

              {/* Card kết quả học tập - ngay bên dưới, không bị đẩy xuống */}
              <Col xs={12}>
                <div className="feature-card mb-4 mt-4">
                  <div className="card-header-custom bg-soft-primary">
                    <FontAwesomeIcon icon={faChartLine} className="card-header-icon" />
                    Kết quả học tập
                  </div>
                  <Card.Body>
                    <div className="d-flex justify-content-between align-items-center mb-2">
                      <h6 className="mb-0">Điểm trung bình các môn</h6>
                      <Dropdown>
                        <Dropdown.Toggle variant="outline-secondary" size="sm" id="dropdown-school-year">
                          {selectedSchoolYear ?
                            `${selectedSchoolYear.semesterName} ${selectedSchoolYear.nameYear}` :
                            'Chọn học kỳ'}
                        </Dropdown.Toggle>

                        <Dropdown.Menu>
                          {schoolYears.map(year => (
                            <Dropdown.Item
                              key={year.id}
                              onClick={() => handleSchoolYearChange(year.id)}
                            >
                              {year.semesterName} {year.nameYear}
                            </Dropdown.Item>
                          ))}
                        </Dropdown.Menu>
                      </Dropdown>
                    </div>

                    <div style={{ height: '300px', position: 'relative' }}>
                      {chartLoading ? (
                        <div className="d-flex justify-content-center align-items-center h-100">
                          <Spinner animation="border" variant="primary" />
                        </div>
                      ) : scoreData.length > 0 ? (
                        <Bar data={chartData} options={chartOptions} />
                      ) : (
                        <div className="d-flex justify-content-center align-items-center h-100 text-center">
                          <p className="text-muted mb-0">Không có dữ liệu điểm<br />cho học kỳ đã chọn</p>
                        </div>
                      )}
                    </div>

                    {/* Thêm: Hiển thị điểm trung bình học kỳ nếu có */}
                    {scoreData.length > 0 && (
                      <div className="mt-3 text-end">
                        <span className="badge bg-primary p-2">
                          Điểm trung bình học kỳ: <strong>{semesterAverage > 0 ? semesterAverage.toFixed(2) : calculateAverageScore()}</strong>
                        </span>
                      </div>
                    )}
                  </Card.Body>
                </div>
              </Col>
            </Row>
          </div>
        </div>
      </div>
    </div >
  );

  function calculateAverageScore() {
    if (scoreData.length === 0) return 0;

    let totalWeightedScore = 0;
    let totalCredits = 0;

    scoreData.forEach(item => {
      totalWeightedScore += item.averageScore * item.credits;
      totalCredits += item.credits;
    });

    return totalCredits > 0 ? (totalWeightedScore / totalCredits).toFixed(2) : "0.00";
  }
};

export default StudentDashboard;