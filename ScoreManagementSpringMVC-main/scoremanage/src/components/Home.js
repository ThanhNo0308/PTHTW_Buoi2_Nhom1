import React, { useContext } from "react";
import { Link } from "react-router-dom";
import { MyUserContext } from "../App";
import "../assets/css/base.css";
import "../assets/css/styles.css";
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { 
  faBolt, faSignInAlt, faUserPlus, faCalculator, faChartPie,
  faUserShield, faBell, faFileExport, faComments, faQuestionCircle,
  faBook, faEnvelope, faPhone
} from '@fortawesome/free-solid-svg-icons';

const Home = () => {
  const [user] = useContext(MyUserContext);

  return (
    <>
      {/* Hero Section */}
      <div className="hero-section">
        <div className="container">
          <div className="row align-items-center">
            <div className="col-md-7 hero-text">
              <h1 className="display-4 fw-bold mb-4">Hệ Thống Quản Lý Điểm Sinh Viên</h1>
              <p className="fs-5 mb-4">Công cụ toàn diện giúp quản lý, theo dõi và phân tích kết quả học tập một cách hiệu quả</p>
              <div className="d-flex gap-3">
                <a href="#features" className="btn btn-light btn-lg px-4">Tìm hiểu thêm</a>
                <a href="#quick-access" className="btn btn-outline-light btn-lg px-4">Truy cập nhanh</a>
              </div>
            </div>
            <div className="col-md-5 d-none d-md-block">
              <img src="https://cdn-icons-png.flaticon.com/512/2995/2995392.png" alt="Quản lý điểm" className="img-fluid" style={{ maxHeight: "300px" }} />
            </div>
          </div>
        </div>
      </div>

      {/* Features Section */}
      <div className="container mb-5" id="features">
        <div className="row mb-4">
          <div className="col text-center">
            <h2 className="fw-bold">Tính năng nổi bật</h2>
            <p className="text-muted">Hệ thống quản lý điểm hiện đại với đầy đủ chức năng cho mọi đối tượng</p>
          </div>
        </div>

        <div className="row g-4">
          <div className="col-md-4">
            <div className="card feature-card">
              <div className="card-body text-center py-4">
                <div className="feature-icon text-primary">
                  <FontAwesomeIcon icon={faCalculator} />
                </div>
                <h5 className="card-title">Quản lý điểm số</h5>
                <p className="card-text">Nhập, quản lý và theo dõi điểm số sinh viên theo nhiều tiêu chí, hệ số đánh giá.</p>
              </div>
            </div>
          </div>

          <div className="col-md-4">
            <div className="card feature-card">
              <div className="card-body text-center py-4">
                <div className="feature-icon text-success">
                  <FontAwesomeIcon icon={faChartPie} />
                </div>
                <h5 className="card-title">Thống kê & Báo cáo</h5>
                <p className="card-text">Xem thống kê, biểu đồ phân tích kết quả học tập theo lớp, môn học, khoa...</p>
              </div>
            </div>
          </div>

          <div className="col-md-4">
            <div className="card feature-card">
              <div className="card-body text-center py-4">
                <div className="feature-icon text-info">
                  <FontAwesomeIcon icon={faUserShield} />
                </div>
                <h5 className="card-title">Phân quyền chi tiết</h5>
                <p className="card-text">Phân quyền chi tiết theo vai trò: Sinh viên, Giảng viên, Quản trị viên.</p>
              </div>
            </div>
          </div>

          <div className="col-md-4">
            <div className="card feature-card">
              <div className="card-body text-center py-4">
                <div className="feature-icon text-warning">
                  <FontAwesomeIcon icon={faBell} />
                </div>
                <h5 className="card-title">Thông báo tự động</h5>
                <p className="card-text">Hệ thống tự động thông báo khi có điểm mới, thay đổi điểm hoặc các sự kiện quan trọng.</p>
              </div>
            </div>
          </div>

          <div className="col-md-4">
            <div className="card feature-card">
              <div className="card-body text-center py-4">
                <div className="feature-icon text-danger">
                  <FontAwesomeIcon icon={faFileExport} />
                </div>
                <h5 className="card-title">Xuất/nhập dữ liệu</h5>
                <p className="card-text">Hỗ trợ nhập điểm từ file Excel, xuất báo cáo dưới nhiều định dạng.</p>
              </div>
            </div>
          </div>

          <div className="col-md-4">
            <div className="card feature-card">
              <div className="card-body text-center py-4">
                <div className="feature-icon text-secondary">
                  <FontAwesomeIcon icon={faComments} />
                </div>
                <h5 className="card-title">Diễn đàn trao đổi</h5>
                <p className="card-text">Kênh trao đổi trực tiếp giữa sinh viên và giảng viên về học tập, điểm số.</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Quick Access Section */}
      <div className="container mb-5" id="quick-access">
        <div className="row mb-4">
          <div className="col">
            <h2 className="fw-bold">
              <FontAwesomeIcon icon={faBolt} className="me-2" />
              Truy cập nhanh
            </h2>
          </div>
        </div>

        <div className="row g-4">
          {/* Guest Quick Access - chỉ hiển thị khi chưa đăng nhập */}
          {!user && (
            <div className="col-md-4">
              <div className="quick-access">
                <h5 className="mb-3 text-primary">
                  <FontAwesomeIcon icon={faSignInAlt} className="me-2" />
                  Tham gia ngay
                </h5>
                <div className="card border-0 bg-light mb-3">
                  <div className="card-body text-center">
                    <p className="mb-3">Đăng nhập để truy cập hệ thống quản lý điểm của trường</p>
                    <Link to="/login" className="btn btn-primary mb-2 w-100">
                      <FontAwesomeIcon icon={faSignInAlt} className="me-2" />
                      Đăng nhập
                    </Link>
                    <Link to="/registerStudent" className="btn btn-outline-primary w-100">
                      <FontAwesomeIcon icon={faUserPlus} className="me-2" />
                      Đăng ký tài khoản
                    </Link>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Help & Support */}
          <div className="col-md-4">
            <div className="quick-access">
              <h5 className="mb-3 text-secondary">
                <FontAwesomeIcon icon={faQuestionCircle} className="me-2" />
                Hỗ trợ
              </h5>
              <div className="card border-0 bg-light">
                <div className="card-body">
                  <p className="mb-3">
                    <FontAwesomeIcon icon={faBook} className="me-2" />
                    Hướng dẫn sử dụng hệ thống
                  </p>
                  <p className="mb-3">
                    <FontAwesomeIcon icon={faEnvelope} className="me-2" />
                    Email: milkyway@dh.edu.vn
                  </p>
                  <p className="mb-0">
                    <FontAwesomeIcon icon={faPhone} className="me-2" />
                    Hotline: (028) 1234 5678
                  </p>
                </div>
              </div>
            </div>
          </div>

          {/* Latest Updates */}
          <div className="col-md-4">
            <div className="quick-access">
              <h5 className="mb-3 text-danger">
                <FontAwesomeIcon icon={faBell} className="me-2" />
                Thông báo mới nhất
              </h5>
              <div className="card border-0 bg-light">
                <div className="card-body p-0">
                  <ul className="list-group list-group-flush">
                    <li className="list-group-item bg-transparent d-flex align-items-center">
                      <span className="badge bg-danger me-2">Mới</span>
                      <small>Cập nhật hệ thống v2.5: Thêm tính năng thống kê theo khoa</small>
                    </li>
                    <li className="list-group-item bg-transparent d-flex align-items-center">
                      <span className="badge bg-danger me-2">Mới</span>
                      <small>Lịch thi cuối kỳ đã được cập nhật. Sinh viên xem tại mục Lịch thi.</small>
                    </li>
                  </ul>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default Home;