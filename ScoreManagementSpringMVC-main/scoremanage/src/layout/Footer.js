import React from 'react';
import "../assets/css/base.css";
import "../assets/css/styles.css";
import logo from '../assets/images/logo.png';
import { Container, Row, Col } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { 
  faFacebookF, faTwitter, faYoutube, faInstagram, faLinkedinIn 
} from '@fortawesome/free-brands-svg-icons';
import { 
  faMapMarkerAlt, faPhoneAlt, faEnvelope, faClock, faAngleRight 
} from '@fortawesome/free-solid-svg-icons';

const Footer = () => {
  return (
    <footer className="footer-section">
      <div className="footer-top bg-dark text-white py-5">
        <Container>
          <Row>
            {/* Thông tin trường học */}
            <Col lg={4} className="mb-4">
              <div className="footer-info">
                <div className="d-flex align-items-center mb-3">
                  <img 
                    src={logo}
                    alt="Logo" 
                    width="70" 
                    height="54" 
                    className="me-3" 
                  />
                  <div>
                    <h5 className="mb-0 text-white">Trường Đại học</h5>
                    <h4 className="mb-0 text-warning">MilkyWay</h4>
                  </div>
                </div>
                <p className="mb-3 text-light">
                  Đào tạo nhân tài, phát triển tri thức, nghiên cứu khoa học 
                  và chuyển giao công nghệ để phụng sự cộng đồng.
                </p>
                <div className="social-links">
                  <a href="#" className="social-icon">
                    <FontAwesomeIcon icon={faFacebookF} />
                  </a>
                  <a href="#" className="social-icon">
                    <FontAwesomeIcon icon={faTwitter} />
                  </a>
                  <a href="#" className="social-icon">
                    <FontAwesomeIcon icon={faYoutube} />
                  </a>
                  <a href="#" className="social-icon">
                    <FontAwesomeIcon icon={faInstagram} />
                  </a>
                  <a href="#" className="social-icon">
                    <FontAwesomeIcon icon={faLinkedinIn} />
                  </a>
                </div>
              </div>
            </Col>

            {/* Liên kết nhanh */}
            <Col lg={2} md={6} className="mb-4">
              <h5 className="text-uppercase text-white mb-4">Liên kết nhanh</h5>
              <ul className="footer-links">
                <li>
                  <a href="#">
                    <FontAwesomeIcon icon={faAngleRight} className="me-2" />
                    Trang chủ
                  </a>
                </li>
                <li>
                  <a href="#">
                    <FontAwesomeIcon icon={faAngleRight} className="me-2" />
                    Giới thiệu
                  </a>
                </li>
                <li>
                  <a href="#">
                    <FontAwesomeIcon icon={faAngleRight} className="me-2" />
                    Khoa & Ngành
                  </a>
                </li>
                <li>
                  <a href="#">
                    <FontAwesomeIcon icon={faAngleRight} className="me-2" />
                    Tuyển sinh
                  </a>
                </li>
                <li>
                  <a href="#">
                    <FontAwesomeIcon icon={faAngleRight} className="me-2" />
                    Nghiên cứu
                  </a>
                </li>
                <li>
                  <a href="#">
                    <FontAwesomeIcon icon={faAngleRight} className="me-2" />
                    Hợp tác quốc tế
                  </a>
                </li>
              </ul>
            </Col>

            {/* Dịch vụ sinh viên */}
            <Col lg={3} md={6} className="mb-4">
              <h5 className="text-uppercase text-white mb-4">Dịch vụ sinh viên</h5>
              <ul className="footer-links">
                <li>
                  <a href="#">
                    <FontAwesomeIcon icon={faAngleRight} className="me-2" />
                    Thời khóa biểu
                  </a>
                </li>
                <li>
                  <a href="#">
                    <FontAwesomeIcon icon={faAngleRight} className="me-2" />
                    Lịch thi
                  </a>
                </li>
                <li>
                  <a href="#">
                    <FontAwesomeIcon icon={faAngleRight} className="me-2" />
                    Học bổng
                  </a>
                </li>
                <li>
                  <a href="#">
                    <FontAwesomeIcon icon={faAngleRight} className="me-2" />
                    Hoạt động sinh viên
                  </a>
                </li>
                <li>
                  <a href="#">
                    <FontAwesomeIcon icon={faAngleRight} className="me-2" />
                    Cơ hội việc làm
                  </a>
                </li>
                <li>
                  <a href="#">
                    <FontAwesomeIcon icon={faAngleRight} className="me-2" />
                    Thư viện số
                  </a>
                </li>
              </ul>
            </Col>

            {/* Thông tin liên hệ */}
            <Col lg={3} className="mb-4">
              <h5 className="text-uppercase text-white mb-4">Liên hệ</h5>
              <div className="contact-info">
                <p>
                  <FontAwesomeIcon icon={faMapMarkerAlt} className="me-2" />
                  Nhà Bè, TP. Hồ Chí Minh
                </p>
                <p>
                  <FontAwesomeIcon icon={faPhoneAlt} className="me-2" />
                  (028) 1234 5678
                </p>
                <p>
                  <FontAwesomeIcon icon={faEnvelope} className="me-2" />
                  milkyway@dh.edu.vn
                </p>
                <p>
                  <FontAwesomeIcon icon={faClock} className="me-2" />
                  Thứ 2 - Thứ 7: 7:30 - 17:15
                </p>
              </div>
            </Col>
          </Row>
        </Container>
      </div>

      {/* Footer Bottom */}
      <div className="footer-bottom py-3 bg-darker text-center">
        <Container>
          <Row>
            <Col md={6} className="text-md-start">
              <p className="mb-0 text-light">© 2025 Trường Đại học MilkyWay. Tất cả các quyền được bảo lưu.</p>
            </Col>
            <Col md={6} className="text-md-end">
              <div className="footer-links-bottom">
                <a href="#">Chính sách bảo mật</a>
                <span className="mx-2">|</span>
                <a href="#">Điều khoản sử dụng</a>
                <span className="mx-2">|</span>
                <a href="#">Sơ đồ trang</a>
              </div>
            </Col>
          </Row>
        </Container>
      </div>
    </footer>
  );
};

export default Footer;